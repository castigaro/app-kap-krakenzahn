package com.castigaro.kapkrakenzahn

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.castigaro.common.llm.ProviderSettingsController
import com.castigaro.kapkrakenzahn.data.AppSettings
import com.castigaro.kapkrakenzahn.databinding.ActivitySettingsBinding
import kotlin.math.roundToInt

/**
 * Einstellungen: KI-Anbindung (Oberfläche und Logik aus der Common Lib via
 * ProviderSettingsController) plus die Audio-Schalter (Sprachausgabe,
 * Soundeffekte, Ambiente, Musik) und die Lautstärkeregler.
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
        binding.switchMusic.isChecked = AppSettings.isMusicEnabled(this)

        binding.switchTts.setOnCheckedChangeListener { _, v -> AppSettings.setTtsEnabled(this, v) }
        binding.switchSfx.setOnCheckedChangeListener { _, v -> AppSettings.setSfxEnabled(this, v) }
        binding.switchAmbience.setOnCheckedChangeListener { _, v -> AppSettings.setAmbienceEnabled(this, v) }
        binding.switchMusic.setOnCheckedChangeListener { _, v -> AppSettings.setMusicEnabled(this, v) }

        binding.sliderAmbienceVolume.value = sliderValue(AppSettings.ambienceVolume(this))
        binding.sliderAmbienceVolume.addOnChangeListener { _, value, _ ->
            AppSettings.setAmbienceVolume(this, value / 100f)
        }
        binding.sliderMusicVolume.value = sliderValue(AppSettings.musicVolume(this))
        binding.sliderMusicVolume.addOnChangeListener { _, value, _ ->
            AppSettings.setMusicVolume(this, value / 100f)
        }
    }

    /** Rundet eine Lautstärke (0..1) auf das 5er-Raster des Sliders. */
    private fun sliderValue(volume: Float): Float =
        ((volume * 100 / 5).roundToInt() * 5).coerceIn(0, 100).toFloat()

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
