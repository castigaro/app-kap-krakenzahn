package com.castigaro.kapkrakenzahn

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.castigaro.common.llm.ProviderSettings
import com.castigaro.kapkrakenzahn.api.DialogApi
import com.castigaro.kapkrakenzahn.api.DuelApi
import com.castigaro.kapkrakenzahn.api.LlmClient
import com.castigaro.kapkrakenzahn.audio.Ambience
import com.castigaro.kapkrakenzahn.audio.Music
import com.castigaro.kapkrakenzahn.audio.Sfx
import com.castigaro.kapkrakenzahn.audio.Speaker
import com.castigaro.kapkrakenzahn.data.AppSettings
import com.castigaro.kapkrakenzahn.data.DialogMessage
import com.castigaro.kapkrakenzahn.data.GameState
import com.castigaro.kapkrakenzahn.data.SaveStore
import com.castigaro.kapkrakenzahn.databinding.ActivityGameBinding
import com.castigaro.kapkrakenzahn.databinding.DialogVolumeBinding
import com.castigaro.kapkrakenzahn.game.ActionResult
import com.castigaro.kapkrakenzahn.game.DuelController
import com.castigaro.kapkrakenzahn.game.GameEngine
import com.castigaro.kapkrakenzahn.world.Exit
import com.castigaro.kapkrakenzahn.world.FallbackDialogs
import com.castigaro.kapkrakenzahn.world.FallbackDuel
import com.castigaro.kapkrakenzahn.world.GameWorld
import com.castigaro.kapkrakenzahn.world.Hotspot
import com.castigaro.kapkrakenzahn.world.ItemId
import com.castigaro.kapkrakenzahn.world.NpcId
import com.castigaro.kapkrakenzahn.world.Riddles
import com.castigaro.kapkrakenzahn.world.VerbId
import com.castigaro.kapkrakenzahn.render.SceneView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import kotlin.math.roundToInt

/**
 * Der Spielbildschirm: Szene (SceneView) plus HUD, Inventar und die
 * Dialog-/Duell-Overlays. Hier laufen Interaktion, Regelanwendung,
 * KI-Gespräche, Wortduell, Wächter-Rätsel, Autosave und Audio zusammen.
 */
class GameActivity : AppCompatActivity(), SceneView.Listener {

    private lateinit var binding: ActivityGameBinding
    private lateinit var state: GameState
    private lateinit var inventoryAdapter: InventoryAdapter
    private lateinit var speaker: Speaker
    private lateinit var ambience: Ambience
    private lateinit var music: Music

    /** Gerade zum „Benutzen mit …" ausgewählter Gegenstand. */
    private var activeItem: ItemId? = null

    /** Aktuell gewähltes Verb der Bedienleiste; null = „Gehe zu" (nur Laufen). */
    private var currentVerb: VerbId? = null

    /** Verb-Schaltflächen der Leiste, gekoppelt an ihr Verb (null = Gehe zu). */
    private lateinit var verbButtons: List<Pair<VerbId?, TextView>>

    /** Ob die Verbtafel über der schmalen Dauer-Leiste aufgeklappt ist. */
    private var panelExpanded = true

    /** Die letzten Meldungen für den „Verlauf"-Dialog (neueste zuerst). */
    private val recentComments = ArrayDeque<String>()

    /** Die Werkbank zum Kombinieren von Inventar-Gegenständen. */
    private lateinit var crafting: CraftingController

    /**
     * Ziel und Zeitpunkt der zuletzt ausgeführten Hotspot-Aktion. Stand die
     * Heldin beim Doppeltipp schon am Ziel, führt bereits der erste Tipp die
     * Aktion aus — der zweite darf dann nicht noch die Standard-Aktion feuern.
     */
    private var lastActionHotspotId: String? = null
    private var lastActionAt = 0L

    /** Sobald die KI einmal ausfällt (oder kein Key), bleiben wir offline. */
    private var offlineDialog = false

    private var resumedAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getStringExtra(EXTRA_SAVE_ID)
        val loaded = id?.let { SaveStore.get(this, it) }
        if (loaded == null) {
            finish()
            return
        }
        state = loaded
        offlineDialog = !ProviderSettings.isConfigured(this)

        ambience = Ambience(this)
        music = Music(this)
        speaker = Speaker(this) { speaking ->
            ambience.duck(speaking)
            music.duck(speaking)
        }

        inventoryAdapter = InventoryAdapter(state.inventory.toList()) { onInventoryTap(it) }
        binding.inventoryBar.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.inventoryBar.adapter = inventoryAdapter

        crafting = CraftingController(
            binding.craftOverlay,
            inventory = { state.inventory.toList() },
            onCombine = { onCraftCombine(it) },
        )

        verbButtons = listOf(
            null to binding.verbWalk,
            VerbId.SCHAU to binding.verbLook,
            VerbId.NIMM to binding.verbTake,
            VerbId.BENUTZE to binding.verbUse,
            VerbId.REDE to binding.verbTalk,
        )
        verbButtons.forEach { (verb, view) -> view.setOnClickListener { selectVerb(verb) } }
        selectVerb(null) // Default: „Gehe zu"

        binding.sceneView.listener = this
        binding.buttonMenu.setOnClickListener { showMenu() }
        binding.buttonPanelToggle.setOnClickListener { togglePanel() }
        binding.buttonCraft.setOnClickListener { crafting.open() }
        binding.buttonHistory.setOnClickListener { showRecentComments() }
        setPanelExpanded(AppSettings.isPanelExpanded(this))

        enterLocation(rebindOnly = true)
    }

    override fun onResume() {
        super.onResume()
        resumedAt = System.currentTimeMillis()
        binding.sceneView.animate = true
        applyFullscreen(AppSettings.isFullscreen(this))
        ambience.switchTo(state.locationId)
        music.start()
    }

    override fun onPause() {
        super.onPause()
        state.playtimeMs += System.currentTimeMillis() - resumedAt
        binding.sceneView.animate = false
        ambience.stop()
        music.stop()
        autosave()
    }

    override fun onDestroy() {
        super.onDestroy()
        speaker.shutdown()
        ambience.stop()
        music.stop()
    }

    // ---- SceneView.Listener ------------------------------------------------

    override fun onHotspotTargeted(hotspot: Hotspot) {
        // Held läuft noch — Satzzeile schon mit dem Ziel vervollständigen.
        val item = activeItem
        binding.sentenceLine.text = if (item != null) {
            getString(R.string.game_use_with_target, GameWorld.itemName(item), hotspot.name)
        } else {
            getString(R.string.sentence_verb_target, verbLabel(currentVerb), hotspot.name)
        }
    }

    override fun onHotspotReached(hotspot: Hotspot) {
        val item = activeItem
        if (item != null) {
            clearActiveItem()
            apply(GameEngine.performAction(state, VerbId.BENUTZE, hotspot.id, item))
            markHotspotAction(hotspot)
            resetVerb()
            return
        }
        when (val verb = currentVerb) {
            null -> Unit // „Gehe zu": Held ist nur hingelaufen.
            VerbId.REDE ->
                if (hotspot.npc != null) openDialog(hotspot.npc)
                else apply(GameEngine.performAction(state, VerbId.REDE, hotspot.id, null))
            else -> apply(GameEngine.performAction(state, verb, hotspot.id, null))
        }
        if (currentVerb != null) markHotspotAction(hotspot)
        resetVerb()
    }

    override fun onHotspotDefaultAction(hotspot: Hotspot) {
        // Explizite Wahl (Verb oder Gegenstand) gewinnt über den Doppeltipp.
        if (activeItem != null || currentVerb != null) {
            onHotspotReached(hotspot)
            return
        }
        // Hat der erste Tipp die Aktion schon ausgeführt (Ankunft lag vor dem
        // zweiten Tipp), ist der zweite Tipp nur noch eine gewöhnliche Ankunft.
        val now = android.os.SystemClock.uptimeMillis()
        val window = android.view.ViewConfiguration.getDoubleTapTimeout().toLong()
        if (hotspot.id == lastActionHotspotId && now - lastActionAt <= window) return

        when (val verb = GameEngine.defaultVerb(hotspot)) {
            VerbId.REDE ->
                if (hotspot.npc != null) openDialog(hotspot.npc)
                else apply(GameEngine.performAction(state, VerbId.REDE, hotspot.id, null))
            else -> apply(GameEngine.performAction(state, verb, hotspot.id, null))
        }
        markHotspotAction(hotspot)
        resetVerb()
    }

    private fun markHotspotAction(hotspot: Hotspot) {
        lastActionHotspotId = hotspot.id
        lastActionAt = android.os.SystemClock.uptimeMillis()
    }

    override fun onExitReached(exit: Exit) {
        if (!exit.condition(state)) {
            comment(exit.lockedText ?: "Da geht es gerade nicht weiter.")
            return
        }
        val eventText = GameEngine.changeLocation(state, exit.target)
        enterLocation()
        eventText?.let { comment(it) }
        autosave()
    }

    override fun onWalkOnly() {
        resetVerb()
    }

    // ---- Ort betreten / Rendering ------------------------------------------

    private fun enterLocation(rebindOnly: Boolean = false) {
        binding.sceneView.bind(state)
        if (!rebindOnly) binding.sceneView.placeHero(state.heroX, state.heroY)
        binding.locationLabel.text = GameWorld.location(state.locationId).name
        ambience.switchTo(state.locationId)
        refreshInventory()
        resetVerb()
    }

    private fun refreshInventory() {
        inventoryAdapter.submit(state.inventory.toList(), selected = activeItem)
        binding.buttonCraft.isEnabled = state.inventory.size >= 2
        if (crafting.isOpen) crafting.refreshInventory()
    }

    /** Klappt die Verbtafel über der Dauer-Leiste auf/zu (persistiert). */
    private fun togglePanel() {
        setPanelExpanded(!panelExpanded)
        AppSettings.setPanelExpanded(this, panelExpanded)
    }

    private fun setPanelExpanded(expanded: Boolean) {
        panelExpanded = expanded
        binding.expandedPanel.visibility = if (expanded) View.VISIBLE else View.GONE
        binding.buttonPanelToggle.text = if (expanded) "▾" else "▴"
        // Zugeklappt gibt es keine Verb-Wahl mehr — zurück auf „Gehe zu".
        if (!expanded) resetVerb()
    }

    // ---- Verben & Inventar (SCUMM-Bedienleiste) ----------------------------

    /** Ein Verb der Leiste wählen; null = „Gehe zu". */
    private fun selectVerb(verb: VerbId?) {
        currentVerb = verb
        activeItem = null // neues Verb bricht ein angefangenes „Benutze … mit" ab
        highlightVerbs()
        updateSentence()
        refreshInventory()
    }

    /** Nach einer Handlung zurück auf „Gehe zu". */
    private fun resetVerb() {
        currentVerb = null
        activeItem = null
        highlightVerbs()
        updateSentence()
        refreshInventory()
    }

    private fun highlightVerbs() {
        val usingItem = activeItem != null
        verbButtons.forEach { (verb, view) ->
            val active = if (usingItem) verb == VerbId.BENUTZE else verb == currentVerb
            val color = if (active) R.color.scumm_verb_active else R.color.scumm_verb
            view.setTextColor(ContextCompat.getColor(this, color))
        }
    }

    /** Satzzeile aus aktuellem Verb und (optional) gewähltem Gegenstand. */
    private fun updateSentence() {
        binding.sentenceLine.text = activeItem?.let {
            getString(R.string.game_use_with, GameWorld.itemName(it))
        } ?: verbLabel(currentVerb)
    }

    private fun verbLabel(verb: VerbId?): String = when (verb) {
        null -> getString(R.string.verb_walk)
        VerbId.SCHAU -> getString(R.string.verb_look)
        VerbId.NIMM -> getString(R.string.verb_take)
        VerbId.BENUTZE -> getString(R.string.verb_use)
        VerbId.REDE -> getString(R.string.verb_talk)
    }

    private fun onInventoryTap(item: ItemId) {
        when {
            activeItem == item -> resetVerb() // Auswahl aufheben
            currentVerb == VerbId.SCHAU -> { comment(GameWorld.itemDescription(item)); resetVerb() }
            currentVerb == VerbId.REDE -> { comment(getString(R.string.item_no_talk, GameWorld.itemName(item))); resetVerb() }
            currentVerb == VerbId.NIMM -> { comment(getString(R.string.item_already_have, GameWorld.itemName(item))); resetVerb() }
            else -> {
                // „Gehe zu" oder „Benutze": Gegenstand zum Anwenden wählen
                // (bzw. die Auswahl wechseln). Kombiniert wird in der Werkbank.
                activeItem = item
                highlightVerbs()
                updateSentence()
                refreshInventory()
            }
        }
    }

    private fun clearActiveItem() {
        activeItem = null
    }

    // ---- Ergebnis anwenden -------------------------------------------------

    private fun apply(result: ActionResult) {
        if (result.text.isNotBlank()) comment(result.text)
        result.sfx.forEach { Sfx.play(this, it) }
        result.moveTo?.let { target ->
            // Regel-gesteuerter Ortswechsel (z. B. Boot, Abseilen): erst die
            // Regel-Erzählung stehen lassen, dann in die neue Szene wechseln.
            GameEngine.changeLocation(state, target)
            enterLocation()
        }
        result.startDialog?.let { openDialog(it) }
        result.startDuel?.let { openDuel(it) }
        if (result.startRiddle) startRiddle()
        refreshInventory()
        if (result.stateChanged || result.moveTo != null) autosave()
        if (result.endGame) showEnding()
    }

    private fun comment(text: String) {
        binding.commentBar.visibility = View.VISIBLE
        binding.commentBar.text = text
        recentComments.addFirst(text)
        while (recentComments.size > RECENT_COMMENTS) recentComments.removeLast()
        speaker.speak(this, text, npc = null)
    }

    /** Zeigt die letzten Meldungen (neueste zuerst) — falls eine Zeile überlesen wurde. */
    private fun showRecentComments() {
        val text = if (recentComments.isEmpty()) {
            getString(R.string.history_empty)
        } else {
            recentComments.joinToString("\n\n") { "• $it" }
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.history_title)
            .setMessage(text)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    /** „Verbinden" der Werkbank — synchron über die statischen Kombos. */
    private fun onCraftCombine(items: List<ItemId>) {
        val result = GameEngine.combineAll(state, items)
        if (result.stateChanged) {
            crafting.close()
            apply(result)
        } else {
            // Fehlversuch: Slots bleiben belegt, das Urteil erscheint im Overlay.
            crafting.showResult(result.text)
        }
    }

    private fun autosave() {
        SaveStore.update(this, state)
    }

    // ---- KI-Dialog ---------------------------------------------------------

    private var dialogNpc: NpcId? = null

    private fun openDialog(npcId: NpcId) {
        dialogNpc = npcId
        val npc = GameWorld.npc(npcId)
        binding.dialog.root.visibility = View.VISIBLE
        binding.dialog.dialogNpcName.text = npc.name
        binding.dialog.dialogNpcText.text = ""
        if (offlineDialog) {
            showFallbackNode(FallbackDialogs.build(npcId, state))
        } else {
            loadDialogTurn(npcId, gewaehlteOption = null)
        }
    }

    private fun loadDialogTurn(npcId: NpcId, gewaehlteOption: String?) {
        setDialogLoading(true)
        gewaehlteOption?.let {
            state.historyFor(npcId).add(DialogMessage(DialogMessage.ROLE_PLAYER, it))
        }
        lifecycleScope.launch {
            try {
                val turn = DialogApi.turn(this@GameActivity, npcId, state, gewaehlteOption)
                state.historyFor(npcId).add(DialogMessage(DialogMessage.ROLE_NPC, turn.sag))
                if (turn.effects.isNotEmpty()) {
                    apply(GameEngine.applyEffects(state, turn.effects, ""))
                }
                setDialogLoading(false)
                renderDialog(npcId, turn.sag, turn.optionen, closeIndexIsLast = true)
                autosave()
            } catch (e: LlmClient.ApiException) {
                // Ab jetzt offline weiter — Spiel bleibt lösbar.
                offlineDialog = true
                Snackbar.make(binding.root, R.string.dialog_offline_hint, Snackbar.LENGTH_LONG).show()
                setDialogLoading(false)
                showFallbackNode(FallbackDialogs.build(npcId, state))
            }
        }
    }

    private fun renderDialog(
        npcId: NpcId, text: String, options: List<String>, closeIndexIsLast: Boolean,
    ) {
        binding.dialog.dialogNpcText.text = text
        speaker.speak(this, text, npc = npcId)
        binding.dialog.dialogOptions.removeAllViews()
        options.forEachIndexed { index, label ->
            addOptionButton(binding.dialog.dialogOptions, label) {
                val istAbschied = closeIndexIsLast && index == options.lastIndex
                if (istAbschied) {
                    state.historyFor(npcId).add(DialogMessage(DialogMessage.ROLE_PLAYER, label))
                    closeDialog()
                } else {
                    loadDialogTurn(npcId, gewaehlteOption = label)
                }
            }
        }
    }

    /** Geskripteter Ersatz-Dialog (ohne KI). */
    private fun showFallbackNode(node: com.castigaro.kapkrakenzahn.world.FallbackNode) {
        val npcId = dialogNpc ?: return
        binding.dialog.dialogNpcText.text = node.text
        speaker.speak(this, node.text, npc = npcId)
        binding.dialog.dialogOptions.removeAllViews()
        node.options.forEach { option ->
            addOptionButton(binding.dialog.dialogOptions, option.label) {
                state.historyFor(npcId).add(DialogMessage(DialogMessage.ROLE_PLAYER, option.label))
                if (option.effects.isNotEmpty()) {
                    apply(GameEngine.applyEffects(state, option.effects, ""))
                }
                state.historyFor(npcId).add(DialogMessage(DialogMessage.ROLE_NPC, option.antwort))
                if (option.beendet) {
                    closeDialog()
                } else {
                    showFallbackNode(FallbackDialogs.build(npcId, state, option.antwort))
                }
                autosave()
            }
        }
    }

    private fun setDialogLoading(loading: Boolean) {
        binding.dialog.dialogSpinner.visibility = if (loading) View.VISIBLE else View.GONE
        binding.dialog.dialogOptions.visibility = if (loading) View.GONE else View.VISIBLE
    }

    private fun closeDialog() {
        dialogNpc = null
        binding.dialog.root.visibility = View.GONE
        refreshInventory()
    }

    // ---- Wächter-Rätsel ----------------------------------------------------

    private fun startRiddle() {
        askRiddle(0)
    }

    private fun askRiddle(index: Int) {
        val fragen = Riddles.questions(GameWorld.variation(state))
        if (index >= fragen.size) {
            apply(GameEngine.solveRiddle(state))
            return
        }
        val frage = fragen[index]
        val optionen = (listOf(frage.antwort) + frage.koeder).shuffled()
        binding.dialog.root.visibility = View.VISIBLE
        binding.dialog.dialogNpcName.text = GameWorld.npc(NpcId.ANSGAR).name
        setDialogLoading(false)
        binding.dialog.dialogNpcText.text = "Frage ${index + 1}: ${frage.frage}"
        speaker.speak(this, frage.frage, npc = NpcId.ANSGAR)
        binding.dialog.dialogOptions.removeAllViews()
        optionen.forEach { option ->
            addOptionButton(binding.dialog.dialogOptions, option) {
                if (option == frage.antwort) {
                    askRiddle(index + 1)
                } else {
                    binding.dialog.root.visibility = View.GONE
                    comment("Der Wächter zischt: „Falsch! Die Prüfung beginnt von Neuem.")
                }
            }
        }
    }

    // ---- Duell der spitzen Zungen ------------------------------------------

    private lateinit var duel: DuelController

    private fun openDuel(npcId: NpcId) {
        duel = DuelController()
        binding.duel.root.visibility = View.VISIBLE
        nextDuelRound()
    }

    private fun nextDuelRound() {
        binding.duel.duelScore.text =
            getString(R.string.duel_score, duel.runde, duel.spielerPunkte, duel.gegnerPunkte)
        setDuelLoading(true)
        lifecycleScope.launch {
            val round = try {
                if (offlineDialog) FallbackDuel.round(duel.runde, state.seed)
                else DuelApi.round(this@GameActivity, state, duel.verlauf)
            } catch (e: LlmClient.ApiException) {
                offlineDialog = true
                FallbackDuel.round(duel.runde, state.seed)
            }
            setDuelLoading(false)
            renderDuelRound(round)
        }
    }

    private fun renderDuelRound(round: DuelApi.DuelRound) {
        binding.duel.duelInsult.text = round.beleidigung
        speaker.speak(this, round.beleidigung, npc = NpcId.RASMUS)
        binding.duel.duelOptions.removeAllViews()
        round.kontern.forEachIndexed { index, konter ->
            addOptionButton(binding.duel.duelOptions, konter) {
                val richtig = duel.bewerte(round, index)
                comment(if (richtig) "Ein Volltreffer!" else "Das saß daneben …")
                if (duel.vorbei) finishDuel() else nextDuelRound()
            }
        }
    }

    private fun finishDuel() {
        binding.duel.root.visibility = View.GONE
        if (duel.spielerGewinnt) {
            apply(GameEngine.winDuel(state))
            comment(getString(R.string.duel_won))
        } else {
            comment(getString(R.string.duel_lost))
        }
    }

    private fun setDuelLoading(loading: Boolean) {
        binding.duel.duelSpinner.visibility = if (loading) View.VISIBLE else View.GONE
        binding.duel.duelOptions.visibility = if (loading) View.GONE else View.VISIBLE
    }

    // ---- Menü & Ende -------------------------------------------------------

    private fun showMenu() {
        val fullscreenLabel = getString(
            if (AppSettings.isFullscreen(this)) R.string.menu_fullscreen_off
            else R.string.menu_fullscreen_on,
        )
        val optionen = arrayOf(
            getString(R.string.game_save),
            getString(R.string.menu_volume),
            fullscreenLabel,
            getString(R.string.menu_settings),
            getString(R.string.game_quit_confirm),
        )
        AlertDialog.Builder(this)
            .setItems(optionen) { _, which ->
                when (which) {
                    0 -> saveNamedSlot()
                    1 -> showVolumeDialog()
                    2 -> toggleFullscreen()
                    3 -> startActivity(android.content.Intent(this, SettingsActivity::class.java))
                    4 -> confirmQuit()
                }
            }
            .show()
    }

    // ---- Vollbild ------------------------------------------------------------

    private fun toggleFullscreen() {
        val enabled = !AppSettings.isFullscreen(this)
        AppSettings.setFullscreen(this, enabled)
        applyFullscreen(enabled)
    }

    private fun applyFullscreen(enabled: Boolean) {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        if (enabled) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // ---- Lautstärke ------------------------------------------------------------

    /** Drei Regler (Musik, Ambiente, Effekte), die sofort hörbar wirken. */
    private fun showVolumeDialog() {
        val dialogBinding = DialogVolumeBinding.inflate(layoutInflater)

        dialogBinding.sliderMusicVolume.value = sliderValue(AppSettings.musicVolume(this))
        dialogBinding.sliderMusicVolume.addOnChangeListener { _, value, _ ->
            AppSettings.setMusicVolume(this, value / 100f)
            music.applyVolume()
        }
        dialogBinding.sliderAmbienceVolume.value = sliderValue(AppSettings.ambienceVolume(this))
        dialogBinding.sliderAmbienceVolume.addOnChangeListener { _, value, _ ->
            AppSettings.setAmbienceVolume(this, value / 100f)
            ambience.applyVolume()
        }
        dialogBinding.sliderSfxVolume.value = sliderValue(AppSettings.sfxVolume(this))
        dialogBinding.sliderSfxVolume.addOnChangeListener { _, value, _ ->
            AppSettings.setSfxVolume(this, value / 100f)
        }
        // Hörprobe beim Loslassen des Effekte-Reglers.
        dialogBinding.sliderSfxVolume.addOnSliderTouchListener(
            object : com.google.android.material.slider.Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: com.google.android.material.slider.Slider) {}
                override fun onStopTrackingTouch(slider: com.google.android.material.slider.Slider) {
                    Sfx.play(this@GameActivity, "blip")
                }
            },
        )

        AlertDialog.Builder(this)
            .setTitle(R.string.menu_volume)
            .setView(dialogBinding.root)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    /** Rundet eine Lautstärke (0..1) auf das 5er-Raster der Slider. */
    private fun sliderValue(volume: Float): Float =
        ((volume * 100 / 5).roundToInt() * 5).coerceIn(0, 100).toFloat()

    private fun saveNamedSlot() {
        val stamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date())
        val slot = state.cloneAs("Sicherung $stamp")
        SaveStore.add(this, slot)
        Snackbar.make(binding.root, R.string.game_saved, Snackbar.LENGTH_SHORT).show()
    }

    private fun confirmQuit() {
        AlertDialog.Builder(this)
            .setTitle(R.string.game_quit_title)
            .setMessage(R.string.game_quit_message)
            .setPositiveButton(R.string.game_quit_confirm) { _, _ -> finish() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showEnding() {
        val geloest = GameWorld.puzzles.count { GameWorld.isSolved(state, it.id) }
        val minuten = state.playtimeMs / 60000
        val wege = GameWorld.puzzles.mapNotNull { GameWorld.solvedPath(state, it.id)?.beschreibung }
            .joinToString("; ")
        val stats = getString(
            R.string.ending_stats,
            "${minuten} Min", geloest, GameWorld.puzzles.size, wege,
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.ending_title)
            .setMessage(stats)
            .setCancelable(false)
            .setPositiveButton(R.string.ending_back) { _, _ -> finish() }
            .show()
    }

    override fun onBackPressed() {
        when {
            binding.duel.root.visibility == View.VISIBLE -> Unit // Duell muss zu Ende
            binding.dialog.root.visibility == View.VISIBLE -> closeDialog()
            crafting.isOpen -> crafting.close()
            else -> confirmQuit()
        }
    }

    // ---- Hilfsfunktion für Options-Buttons ---------------------------------

    private fun addOptionButton(container: android.widget.LinearLayout, label: String, onClick: () -> Unit) {
        val button = Button(this).apply {
            text = label
            isAllCaps = false
            setOnClickListener { onClick() }
        }
        container.addView(button)
    }

    companion object {
        const val EXTRA_SAVE_ID = "save_id"

        /** So viele Meldungen zeigt der „Verlauf"-Dialog rückwirkend. */
        private const val RECENT_COMMENTS = 3
    }
}
