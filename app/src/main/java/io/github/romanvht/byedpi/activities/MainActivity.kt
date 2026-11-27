package io.github.romanvht.byedpi.activities

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import io.github.romanvht.byedpi.databinding.ActivityMainBinding
import io.github.romanvht.byedpi.services.ServiceManager
import io.github.romanvht.byedpi.data.Mode
import io.github.romanvht.byedpi.services.appStatus
import io.github.romanvht.byedpi.data.AppStatus

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    private val vpnPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                ServiceManager.start(this, Mode.VPN)
                Toast.makeText(this, "VPN Запущен!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Вы отклонили доступ к VPN", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()

        // Настройки
        binding.settingsButton?.setOnClickListener {
            Toast.makeText(this, "Открываю настройки...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // DNS Сканер
        binding.dnsTestButton?.setOnClickListener {
            Toast.makeText(this, "Открываю DNS Сканер...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, DnsTestActivity::class.java))
        }

        // Кнопка ВКЛ/ВЫКЛ
        binding.statusButtonCard?.setOnClickListener {
            val (status, _) = appStatus
            if (status == AppStatus.Running) {
                ServiceManager.stop()
                Toast.makeText(this, "VPN Остановлен", Toast.LENGTH_SHORT).show()
            } else {
                val intent = VpnService.prepare(this)
                if (intent != null) {
                    vpnPermissionLauncher.launch(intent)
                } else {
                    ServiceManager.start(this, Mode.VPN)
                    Toast.makeText(this, "VPN Запущен", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
