package io.github.romanvht.byedpi.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import io.github.romanvht.byedpi.R
import io.github.romanvht.byedpi.utility.SettingsUtils
import io.github.romanvht.byedpi.utility.getPreferences
import io.github.romanvht.byedpi.utility.getStringNotNull

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getPreferences()
        val lang = prefs.getStringNotNull("language", "system")
        SettingsUtils.setLang(lang)
        val theme = prefs.getStringNotNull("app_theme", "system")
        SettingsUtils.setTheme(theme)
        super.onCreate(savedInstanceState)
    }

    protected fun setupToolbar() {
        val toolbar: MaterialToolbar? = findViewById(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
        }
    }
}
