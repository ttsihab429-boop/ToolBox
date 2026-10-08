package com.example.ui.screens.diagnostics

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.localization.AppLanguage
import com.example.ui.components.ToolActions
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.Collections

data class PingTarget(
    val name: String,
    val host: String,
    val port: Int = 53
)

data class PingResult(
    val target: PingTarget,
    val latencyMs: Long,
    val success: Boolean
)

data class NetworkDoctorIssue(
    val title: String,
    val description: String,
    val isSevere: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkToolsScreen(
    language: AppLanguage,
    prefs: PreferencesManager,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isRefreshing by remember { mutableStateOf(false) }
    var networkType by remember { mutableStateOf("Detecting...") }
    var hasInternet by remember { mutableStateOf(false) }
    var localIpv4 by remember { mutableStateOf("Checking...") }
    var localIpv6 by remember { mutableStateOf("Checking...") }
    var wifiLinkSpeed by remember { mutableStateOf<String?>(null) }
    var wifiFrequency by remember { mutableStateOf<String?>(null) }

    val pingResults = remember { mutableStateListOf<PingResult>() }
    val networkIssues = remember { mutableStateListOf<NetworkDoctorIssue>() }

    fun refreshNetworkData() {
        if (isRefreshing) return
        isRefreshing = true
        scope.launch {
            withContext(Dispatchers.IO) {
                val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                val activeNet = connMgr?.activeNetwork
                val caps = connMgr?.getNetworkCapabilities(activeNet)

                val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
                val isEthernet = caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true
                val isVpn = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true

                networkType = when {
                    isVpn -> "VPN Protected"
                    isWifi -> "Wi-Fi Network"
                    isCellular -> "Cellular Mobile Data"
                    isEthernet -> "Ethernet LAN"
                    else -> "Disconnected (Offline)"
                }

                // Wi-Fi details if on Wi-Fi
                if (isWifi) {
                    val wifiMgr = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                    val info = wifiMgr?.connectionInfo
                    wifiLinkSpeed = if (info != null && info.linkSpeed > 0) "${info.linkSpeed} Mbps" else null
                    wifiFrequency = if (info != null && info.frequency > 0) "${info.frequency} MHz" else null
                } else {
                    wifiLinkSpeed = null
                    wifiFrequency = null
                }

                // IP Addresses from NetworkInterfaces
                var ipv4Found: String? = null
                var ipv6Found: String? = null
                try {
                    val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
                    for (intf in interfaces) {
                        if (intf.isLoopback || !intf.isUp) continue
                        val addrs = Collections.list(intf.inetAddresses)
                        for (addr in addrs) {
                            if (!addr.isLoopbackAddress) {
                                if (addr is Inet4Address && ipv4Found == null) {
                                    ipv4Found = "${addr.hostAddress} (${intf.name})"
                                } else if (addr is Inet6Address && ipv6Found == null) {
                                    ipv6Found = "${addr.hostAddress?.substringBefore('%')} (${intf.name})"
                                }
                            }
                        }
                    }
                } catch (e: Exception) {}

                localIpv4 = ipv4Found ?: "No Local IPv4"
                localIpv6 = ipv6Found ?: "No Local IPv6"

                // Ping Diagnostic
                val targets = listOf(
                    PingTarget("Google DNS", "8.8.8.8", 53),
                    PingTarget("Cloudflare DNS", "1.1.1.1", 53),
                    PingTarget("OpenDNS", "208.67.222.222", 53)
                )

                val newPings = mutableListOf<PingResult>()
                for (t in targets) {
                    val startTime = System.currentTimeMillis()
                    var success = false
                    try {
                        Socket().use { socket ->
                            socket.connect(InetSocketAddress(t.host, t.port), 1800)
                            success = true
                        }
                    } catch (e: Exception) {
                        success = false
                    }
                    val latency = System.currentTimeMillis() - startTime
                    newPings.add(PingResult(t, if (success) latency else -1L, success))
                }

                hasInternet = newPings.any { it.success }

                // Network Doctor Diagnosis
                val issues = mutableListOf<NetworkDoctorIssue>()
                if (networkType.startsWith("Disconnected")) {
                    issues.add(
                        NetworkDoctorIssue(
                            title = if (language == AppLanguage.BANGLA) "কোনো নেটওয়ার্ক সক্রিয় নেই" else "No Active Network",
                            description = if (language == AppLanguage.BANGLA) "ওয়াইফাই বা মোবাইল ডাটা চালু করুন।" else "Enable Wi-Fi or Cellular Data in your Android Quick Settings.",
                            isSevere = true
                        )
                    )
                } else if (!hasInternet) {
                    issues.add(
                        NetworkDoctorIssue(
                            title = if (language == AppLanguage.BANGLA) "ইন্টারনেট অ্যাক্সেস নেই" else "Connected Without Internet",
                            description = if (language == AppLanguage.BANGLA) "ডিভাইস রাউটারের সাথে যুক্ত কিন্তু ইন্টারনেট নেই (ক্যাপটিভ পোর্টাল বা ডাটা শেষ হতে পারে)।" else "Device is connected to local router, but public DNS servers are unreachable. May require web sign-in or data top-up.",
                            isSevere = true
                        )
                    )
                } else {
                    val avgLatency = newPings.filter { it.success }.map { it.latencyMs }.average()
                    if (avgLatency > 180) {
                        issues.add(
                            NetworkDoctorIssue(
                                title = if (language == AppLanguage.BANGLA) "উচ্চ লেটেন্সি / পিং বিলম্ব" else "High Latency Detected",
                                description = if (language == AppLanguage.BANGLA) "গড় রেসপন্স টাইম ${avgLatency.toInt()} ms। ভারী গেমিং বা ভিডিও কলে বাফারিং হতে পারে।" else "Average latency is ${avgLatency.toInt()} ms. Signal may be weak or network congested.",
                                isSevere = false
                            )
                        )
                    } else {
                        issues.add(
                            NetworkDoctorIssue(
                                title = if (language == AppLanguage.BANGLA) "সংযোগ সুস্থ ও সক্রিয়" else "Connection Healthy & Responsive",
                                description = if (language == AppLanguage.BANGLA) "DNS সার্ভার রেসপন্স দ্রুত এবং স্থিতিশীল (${avgLatency.toInt()} ms)।" else "Public DNS reachable with low latency (${avgLatency.toInt()} ms). Browsing and streaming are ready.",
                                isSevere = false
                            )
                        )
                    }
                }

                withContext(Dispatchers.Main) {
                    pingResults.clear()
                    pingResults.addAll(newPings)
                    networkIssues.clear()
                    networkIssues.addAll(issues)
                    isRefreshing = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshNetworkData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "নেটওয়ার্ক ও ইন্টারনেট টুলস" else "Network Tools",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { refreshNetworkData() }, enabled = !isRefreshing) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Network Status Hero Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (hasInternet) GreenSuccess.copy(alpha = 0.15f) else RedAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (networkType.contains("Cellular")) Icons.Default.SignalCellularAlt else Icons.Default.Wifi,
                                contentDescription = null,
                                tint = if (hasInternet) GreenSuccess else RedAccent,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = networkType,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (hasInternet) "Online • Internet Reachable" else "Offline / No Internet Access",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasInternet) GreenSuccess else RedAccent
                            )
                        }

                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = RedAccent)
                        }
                    }
                }
            }

            // Network Doctor Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = RedAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (language == AppLanguage.BANGLA) "নেটওয়ার্ক ডাক্তার ডায়াগনোসিস" else "Network Doctor Diagnosis",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        networkIssues.forEach { issue ->
                            val color = if (issue.isSevere) RedAccent else GreenSuccess
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = if (issue.isSevere) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = issue.title, fontWeight = FontWeight.SemiBold, color = color, fontSize = 14.sp)
                                    Text(text = issue.description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Ping Latency Diagnostic
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = RedAccent, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (language == AppLanguage.BANGLA) "পিং ও লেটেন্সি টেস্ট" else "Live Ping & Latency",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        pingResults.forEachIndexed { idx, res ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = res.target.name, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                    Text(text = res.target.host, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (res.success) {
                                    val color = if (res.latencyMs < 100) GreenSuccess else RedAccent
                                    Text(
                                        text = "${res.latencyMs} ms",
                                        fontWeight = FontWeight.Bold,
                                        color = color,
                                        fontSize = 16.sp
                                    )
                                } else {
                                    Text(text = "Timeout", fontWeight = FontWeight.Bold, color = RedAccent)
                                }
                            }
                            if (idx < pingResults.lastIndex) {
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder.copy(alpha = 0.5f)))
                            }
                        }
                    }
                }
            }

            // Local Network & IP Info Card
            item {
                val ipSpecs = mutableListOf(
                    SpecRow("Local IPv4", localIpv4),
                    SpecRow("Local IPv6", localIpv6)
                )
                if (wifiLinkSpeed != null) ipSpecs.add(SpecRow("Wi-Fi Link Speed", wifiLinkSpeed!!))
                if (wifiFrequency != null) ipSpecs.add(SpecRow("Wi-Fi Frequency", wifiFrequency!!))

                SpecSectionCard(
                    title = if (language == AppLanguage.BANGLA) "লোকাল আইপি ও অ্যাডাপ্টার" else "Local IP & Adapters",
                    icon = Icons.Default.Router,
                    rows = ipSpecs
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
