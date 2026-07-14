package com.castigaro.kapkrakenzahn

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.castigaro.common.llm.ProviderSettingsController
import com.castigaro.kapkrakenzahn.data.AppSettings
import com.castigaro.kapkrakenzahn.databinding.ActivitySettingsBinding

/**
 * Einstellungen: KI-Anbindung (Oberfläche und Logik aus der Common Lib via
 * ProviderSettingsController) plus die drei Audio-Schalter (Sprachausgabe,
 * Soundeffekte, Ambiente).
 */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ProviderSettingsController(this, binding.providerSettings)

        binding.switchTts.isChecked = AppSettings.isTtsEnabled(this)
        binding.switchSfx.isChecked = AppSettings.isSfxEnabled(this)
        binding.switchAmbience.isChecked = AppSettings.isAmbienceEnabled(this)

        binding.switchTts.setOnCheckedChangeListener { _, v -> AppSettings.setTtsEnabled(this, v) }
        binding.switchSfx.setOnCheckedChangeListener { _, v -> AppSettings.setSfxEnabled(this, v) }
        binding.switchAmbience.setOnCheckedChangeListener { _, v -> AppSettings.setAmbienceEnabled(this, v) }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
