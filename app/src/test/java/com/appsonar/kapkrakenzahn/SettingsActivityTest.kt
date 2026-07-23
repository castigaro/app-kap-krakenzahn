package com.appsonar.kapkrakenzahn

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Regressionstest: Die Einstellungen crashten beim Öffnen, weil das Layout
 * eine Material-3-Komponente (MaterialSwitch) unter dem Material-2-Theme
 * der App aufblies.
 */
@RunWith(RobolectricTestRunner::class)
class SettingsActivityTest {

    @Test fun `Einstellungen lassen sich oeffnen`() {
        Robolectric.buildActivity(SettingsActivity::class.java).setup()
    }
}
