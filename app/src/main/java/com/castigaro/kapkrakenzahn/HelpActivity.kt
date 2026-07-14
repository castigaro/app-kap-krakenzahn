package com.castigaro.kapkrakenzahn

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.castigaro.kapkrakenzahn.databinding.ActivityHelpBinding

/** Statische Hilfe zu Steuerung, Gesprächen, Speichern und Wiederspielwert. */
class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
