// file: app/src/main/java/io/github/romanvht/byedpi/activities/MainActivity.kt
package io.github.romanvht.byedpi.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import io.github.romanvht.byedpi.R
import io.github.romanvht.byedpi.data.*
import io.github.romanvht.byedpi.databinding.ActivityMainBinding
import io.github.romanvht.byedpi.services.ServiceManager
import io.github.romanvht.byedpi.services.TestService
import io.github.romanvht.byedpi.services.appStatus
import io.github.romanvht.byedpi.utility.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.system.exitProcess

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var historyUtils: HistoryUtils

    companion object {
        private val TAG: String = MainActivity::class.java.simpleName
        private const val BATTERY_OPTIMIZATION_REQUESTED = "battery_optimization_requested"

        private fun collectLogs(): String? {
            return try {
                val process = Runtime.getRuntime().exec("logcat *:D -d")
                process.inputStream.bufferedReader().use { it.readText() }
            } catch (exception: Exception) {
                Log.e(TAG, "Failed to collect logs", exception)
                null
            }
        }
    }

    private val vpnRegister =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                ServiceManager.start(this, Mode.VPN)
            } else {
                Toast.makeText(this, R.string.vpn_permission_denied, Toast.LENGTH_SHORT).show()
                updateStatus()
            }
        }

    private val logsRegister = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ::handleLogsFileResult
    )

    private fun handleLogsFileResult(result: ActivityResult) {
        if (result.resultCode != RESULT_OK) return
        val data = result.data ?: return
        val uri = data.data
        val path = data.getStringExtra(FileActivity.EXTRA_PATH)
        val file = if (path == null) null else File(path)
        if (uri == null && file == null) return

        lifecycleScope.launch(Dispatchers.IO) {
            val logs = collectLogs() ?: run {
                runOnUiThread { Toast.makeText(this@MainActivity, R.string.logs_failed, Toast.LENGTH_SHORT).show() }
                return@launch
            }
            try {
                val outputStream = if (uri != null) contentResolver.openOutputStream(uri) else file?.outputStream()
                outputStream?.use { it.write(logs.toByteArray()) }
            } catch (exception: Exception) {
                Log.e(TAG, "Failed to save logs", exception)
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val senderOrd = intent.getIntExtra(SENDER, -1)
            val sender = Sender.entries.getOrNull(senderOrd) ?: return

            when (intent.action) {
                STARTED_BROADCAST, STOPPED_BROADCAST -> updateStatus()
                FAILED_BROADCAST -> {
                    Toast.makeText(context, getString(R.string.failed_to_start, sender.name), Toast.LENGTH_SHORT).show()
                    updateStatus()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceManager.refresh(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        historyUtils = HistoryUtils(this)

        val intentFilter = IntentFilter().apply {
            addAction(STARTED_BROADCAST)
            addAction(STOPPED_BROADCAST)
            addAction(FAILED_BROADCAST)
        }

        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, intentFilter)
        }

        // Обработка нажатий с защитой от null
        binding.statusButtonCard?.setOnClickListener {
            binding.statusButtonCard?.isClickable = false
            val (status, _) = appStatus
            when (status) {
                AppStatus.Halted -> start()
                AppStatus.Running -> stop()
            }
            binding.statusButtonCard?.postDelayed({ binding.statusButtonCard?.isClickable = true }, 1000)
        }

        binding.settingsButton?.setOnClickListener {
            if (TestService.isRunning) {
                Toast.makeText(this, R.string.test_unavailable, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (appStatus.first == AppStatus.Running) {
                Toast.makeText(this, R.string.settings_unavailable, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.dnsTestButton?.setOnClickListener {
            startActivity(Intent(this, DnsTestActivity::class.java))
        }

        if (!PermissionUtils.hasNotificationPermission(this)) {
            PermissionUtils.requestNotificationPermission(this, 1)
        } else {
            requestBatteryOptimization()
        }

        if (!TestService.isRunning && getPreferences().getBoolean("auto_connect", false) &&
            appStatus.first != AppStatus.Running) {
            this.start()
        }

        ShortcutUtils.update(this)
    }

    override fun onResume() {
        super.onResume()
        ServiceManager.refresh(this)
        updateStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_diagnostics -> {
                val report = DiagnosticUtils.buildReport(this)
                val scrollView = android.widget.ScrollView(this).apply {
                    addView(TextView(this@MainActivity).apply {
                        text = report
                        setPadding(50, 50, 50, 50)
                    })
                }
                AlertDialog.Builder(this)
                    .setTitle(R.string.diagnostics)
                    .setView(scrollView)
                    .setPositiveButton(R.string.diagnostic_copy) { _, _ ->
                        ClipboardUtils.copy(this, report, getString(R.string.diagnostics))
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
                true
            }
            R.id.action_close_app -> {
                TestService.stop()
                lifecycleScope.launch {
                    try {
                        TestService.awaitStopped()
                        ServiceManager.waitStop()
                    } catch (exception: Exception) {
                        return@launch
                    }
                    finishAffinity()
                    android.os.Process.killProcess(android.os.Process.myPid())
                    exitProcess(0)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun start() {
        if (TestService.isRunning) return
        when (getPreferences().mode()) {
            Mode.VPN -> {
                val intentPrepare = VpnService.prepare(this)
                if (intentPrepare != null) vpnRegister.launch(intentPrepare) else ServiceManager.start(this, Mode.VPN)
            }
            Mode.Proxy -> ServiceManager.start(this, Mode.Proxy)
        }
    }

    private fun stop() {
        ServiceManager.stop()
    }

    private fun updateStatus() {
        val (status, mode) = appStatus
        val (ip, port) = getPreferences().getProxyIpAndPort()

        binding.proxyAddress?.text = getString(R.string.proxy_address, ip, port)

        when (status) {
            AppStatus.Halted -> {
                val typedValue = android.util.TypedValue()
                theme.resolveAttribute(android.R.attr.colorPrimary, typedValue,true)
                binding.statusButtonCard?.setCardBackgroundColor(typedValue.data)
                binding.statusButtonIcon?.clearColorFilter()

                when (getPreferences().mode()) {
                    Mode.VPN -> binding.statusText?.setText(R.string.vpn_disconnected)
                    Mode.Proxy -> binding.statusText?.setText(R.string.proxy_down)
                }
            }
            AppStatus.Running -> {
                binding.statusButtonCard?.setCardBackgroundColor(ContextCompat.getColor(this, R.color.green_active))
                binding.statusButtonIcon?.setColorFilter(ContextCompat.getColor(this, android.R.color.white))

                when (mode) {
                    Mode.VPN -> binding.statusText?.setText(R.string.vpn_connected)
                    Mode.Proxy -> binding.statusText?.setText(R.string.proxy_up)
                }
            }
        }
    }

    private fun requestBatteryOptimization() {
        val preferences = getPreferences()
        if (!preferences.getBoolean(BATTERY_OPTIMIZATION_REQUESTED, false) && !PermissionUtils.isBatteryOptimizationDisabled(this)) {
            PermissionUtils.requestBatteryOptimization(this)
            preferences.edit { putBoolean(BATTERY_OPTIMIZATION_REQUESTED, true) }
        }
    }
}
