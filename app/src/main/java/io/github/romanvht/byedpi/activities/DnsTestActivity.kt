// file: app/src/main/java/io/github/romanvht/byedpi/activities/DnsTestActivity.kt
package io.github.romanvht.byedpi.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.romanvht.byedpi.utility.DnsTestResult
import io.github.romanvht.byedpi.utility.DnsTester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DnsTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = Color(0xFF0A0000), surface = Color(0xFF1A0000))) {
                DnsScreen()
            }
        }
    }
}

@Composable
fun DnsScreen() {
    var isTesting by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableStateOf(0) }
    var totalProgress by remember { mutableStateOf(0) }
    var currentServer by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<DnsTestResult>>(emptyList()) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0A0000)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("DNS SCANNER", color = Color.Red, fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text("YouTube Domains Test", color = Color.Gray, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (!isTesting) {
                    isTesting = true
                    results = emptyList()
                    scope.launch(Dispatchers.IO) {
                        val tempResults = mutableListOf<DnsTestResult>()
                        for (server in DnsTester.dnsServers) {
                            currentServer = server.name
                            val result = DnsTester.testServer(server) { current, total ->
                                currentProgress = current
                                totalProgress = total
                            }
                            tempResults.add(result)
                            results = tempResults.toList()
                        }
                        isTesting = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isTesting) Color(0xFF1A0000) else Color.Red),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (isTesting) "ТЕСТИРОВАНИЕ..." else "ЗАПУСТИТЬ ТЕСТ", color = if (isTesting) Color.Red else Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (isTesting) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Проверка: $currentServer", color = Color.Red, fontSize = 14.sp)
            Text("Домен $currentProgress из $totalProgress", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = if (totalProgress > 0) currentProgress.toFloat() / totalProgress else 0f,
                modifier = Modifier.fillMaxWidth(),
                color = Color.Red,
                trackColor = Color(0xFF1A0000)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(results) { res ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0000)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(res.serverName, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("${res.avgTimeMs} ms", color = if (res.avgTimeMs > 0) Color.Red else Color.Gray)
                        }
                        Text("${res.serverIp}", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Разрешено: ${res.successCount} / ${res.totalDomains}", color = if (res.successCount == res.totalDomains) Color.Red else Color.Yellow, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
