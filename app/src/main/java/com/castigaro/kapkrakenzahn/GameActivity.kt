package com.castigaro.kapkrakenzahn

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.castigaro.common.llm.ProviderSettings
import com.castigaro.kapkrakenzahn.api.DialogApi
import com.castigaro.kapkrakenzahn.api.DuelApi
import com.castigaro.kapkrakenzahn.api.LlmClient
import com.castigaro.kapkrakenzahn.audio.Ambience
import com.castigaro.kapkrakenzahn.audio.Sfx
import com.castigaro.kapkrakenzahn.audio.Speaker
import com.castigaro.kapkrakenzahn.data.DialogMessage
import com.castigaro.kapkrakenzahn.data.GameState
import com.castigaro.kapkrakenzahn.data.SaveStore
import com.castigaro.kapkrakenzahn.databinding.ActivityGameBinding
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

    /** Gerade zum „Benutzen mit …" ausgewählter Gegenstand. */
    private var activeItem: ItemId? = null

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

        speaker = Speaker(this)
        ambience = Ambience(this)

        inventoryAdapter = InventoryAdapter(state.inventory.toList()) { onInventoryTap(it) }
        binding.inventoryBar.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.inventoryBar.adapter = inventoryAdapter

        binding.sceneView.listener = this
        binding.buttonMenu.setOnClickListener { showMenu() }

        enterLocation(rebindOnly = true)
    }

    override fun onResume() {
        super.onResume()
        resumedAt = System.currentTimeMillis()
        binding.sceneView.animate = true
        ambience.switchTo(state.locationId)
    }

    override fun onPause() {
        super.onPause()
        state.playtimeMs += System.currentTimeMillis() - resumedAt
        binding.sceneView.animate = false
        ambience.stop()
        autosave()
    }

    override fun onDestroy() {
        super.onDestroy()
        speaker.shutdown()
        ambience.stop()
    }

    // ---- SceneView.Listener ------------------------------------------------

    override fun onHotspotReached(hotspot: Hotspot) {
        val item = activeItem
        if (item != null) {
            clearActiveItem()
            apply(GameEngine.performAction(state, VerbId.BENUTZE, hotspot.id, item))
            return
        }
        showVerbPopup(hotspot)
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
        clearActiveItem()
    }

    // ---- Ort betreten / Rendering ------------------------------------------

    private fun enterLocation(rebindOnly: Boolean = false) {
        binding.sceneView.bind(state)
        if (!rebindOnly) binding.sceneView.placeHero(state.heroX)
        binding.locationLabel.text = GameWorld.location(state.locationId).name
        ambience.switchTo(state.locationId)
        refreshInventory()
    }

    private fun refreshInventory() {
        inventoryAdapter.submit(state.inventory.toList())
    }

    // ---- Verben & Inventar -------------------------------------------------

    private fun showVerbPopup(hotspot: Hotspot) {
        val verbs = hotspot.verbs.toList()
        val labels = verbs.map { verbLabel(it) }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(hotspot.name)
            .setItems(labels) { _, which ->
                when (verbs[which]) {
                    VerbId.REDE -> hotspot.npc?.let { openDialog(it) }
                    VerbId.BENUTZE -> apply(GameEngine.performAction(state, VerbId.BENUTZE, hotspot.id, null))
                    else -> apply(GameEngine.performAction(state, verbs[which], hotspot.id, null))
                }
            }
            .show()
    }

    private fun verbLabel(verb: VerbId): String = when (verb) {
        VerbId.SCHAU -> getString(R.string.verb_look)
        VerbId.NIMM -> getString(R.string.verb_take)
        VerbId.BENUTZE -> getString(R.string.verb_use)
        VerbId.REDE -> getString(R.string.verb_talk)
    }

    private fun onInventoryTap(item: ItemId) {
        when {
            activeItem == null -> {
                activeItem = item
                comment(getString(R.string.game_use_with, GameWorld.itemName(item)))
            }
            activeItem == item -> clearActiveItem()
            else -> {
                val other = activeItem!!
                clearActiveItem()
                apply(GameEngine.combine(state, other, item))
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
        speaker.speak(this, text, npc = null)
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
        val optionen = arrayOf(
            getString(R.string.game_save),
            getString(R.string.menu_settings),
            getString(R.string.game_quit_confirm),
        )
        AlertDialog.Builder(this)
            .setItems(optionen) { _, which ->
                when (which) {
                    0 -> saveNamedSlot()
                    1 -> startActivity(android.content.Intent(this, SettingsActivity::class.java))
                    2 -> confirmQuit()
                }
            }
            .show()
    }

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
    }
}
