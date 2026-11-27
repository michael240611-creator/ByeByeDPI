// file: app/src/main/java/io/github/romanvht/byedpi/activities/MainActivity.kt
package io.github.romanvht.byedpi.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import io.github.romanvht.byedpi.databinding.ActivityMainBinding
import io.github.romanvht.byedpi.services.TestService

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        // Настройки
        binding.settingsButton?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // DNS Сканер
        binding.dnsTestButton?.setOnClickListener {
            startActivity(Intent(this, DnsTestActivity::class.java))
        }

        // Кнопка ВКЛ/ВЫКЛ
        binding.statusButtonCard?.setOnClickListener {
            Toast.makeText(this, "Кнопка работает!", Toast.LENGTH_SHORT).show()
        }
    }
}
