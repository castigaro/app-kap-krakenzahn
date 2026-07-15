package com.castigaro.kapkrakenzahn

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.castigaro.common.update.UpdateChecker
import com.castigaro.common.update.UpdateUi
import com.castigaro.kapkrakenzahn.data.GameState
import com.castigaro.kapkrakenzahn.data.SaveStore
import com.castigaro.kapkrakenzahn.databinding.ActivityMainBinding
import com.castigaro.kapkrakenzahn.world.GameWorld

/**
 * Startbildschirm: animiertes Titelbild, „Weiterspielen" (jüngster Stand),
 * Liste aller Spielstände (antippen = laden, lang drücken = löschen) und
 * „Neues Spiel" mit frischem Seed.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: SaveAdapter
    private val updateChecker = UpdateChecker(VERSION_URL, BuildConfig.VERSION_CODE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        adapter = SaveAdapter(
            emptyList(),
            onClick = { save -> openGame(save.id) },
            onLongClick = { save -> confirmDelete(save) },
        )
        binding.saveList.layoutManager = LinearLayoutManager(this)
        binding.saveList.adapter = adapter

        binding.buttonContinue.setOnClickListener {
            SaveStore.newest(this)?.let { openGame(it.id) }
        }
        binding.fabNewGame.setOnClickListener { startNewGame() }

        // Stille Update-Prüfung beim Start.
        UpdateUi.checkSilently(this, binding.root, updateChecker)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val saves = SaveStore.getAll(this)
        adapter.submit(saves)
        binding.emptyState.visibility = if (saves.isEmpty()) View.VISIBLE else View.GONE
        binding.buttonContinue.visibility = if (saves.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun startNewGame() {
        AlertDialog.Builder(this)
            .setTitle(R.string.main_new_game_title)
            .setMessage(R.string.main_new_game_message)
            .setPositiveButton(R.string.main_start) { _, _ ->
                val save = GameWorld.newGame(System.nanoTime(), neuerName())
                SaveStore.add(this, save)
                openGame(save.id)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun neuerName(): String {
        val vorhandene = SaveStore.getAll(this).count { it.name.startsWith("Abenteuer") }
        return "Abenteuer ${vorhandene + 1}"
    }

    private fun openGame(id: String) {
        startActivity(Intent(this, GameActivity::class.java).putExtra(GameActivity.EXTRA_SAVE_ID, id))
    }

    private fun confirmDelete(save: GameState) {
        AlertDialog.Builder(this)
            .setTitle(R.string.main_delete_save_title)
            .setMessage(getString(R.string.main_delete_save_message, save.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                SaveStore.delete(this, save)
                refresh()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_help -> {
            startActivity(Intent(this, HelpActivity::class.java)); true
        }
        R.id.action_check_update -> {
            UpdateUi.checkManually(this, binding.root, updateChecker); true
        }
        R.id.action_settings -> {
            startActivity(Intent(this, SettingsActivity::class.java)); true
        }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {
        // Die Versionsdatei liegt am GitHub-Release-Asset (Pre-Release) dieses Repos.
        private const val VERSION_URL =
            "https://github.com/castigaro/app-kap-krakenzahn/releases/download/latest/kap-krakenzahn-version.json"
    }
}
