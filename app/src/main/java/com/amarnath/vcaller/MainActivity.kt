package com.amarnath.vcaller

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )

            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {}.launch(intent)
        }
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            setContent {
                CallStatusView(callState = remember { mutableStateOf(CallState("IDLE")) })
            }
        }.launch(intent)

        val intent = Intent(this, CallStatusService::class.java)
        startForegroundService(intent)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "CALL_STATUS_CHANNEL",
            "Call Status",
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = "Shows the current call status"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}

data class CallState(
    val state: String,
    val phoneNumber: String = "+91 1234567890",
    val callerName: String? = "Test Caller"
)

@Composable
fun CallStatusView(callState: MutableState<CallState>) {
    val context = LocalContext.current
    val receiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val newCallState =
                    getCallStateFromIntent(intent)
                callState.value = newCallState
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        context.registerReceiver(receiver, filter)
    }

    CallStatusViewBox(callState)
}

var apiKey = "xxxxxxxxx-23dd-4b0d-8f9b-xxxxxxxxx"
var loggedInNumber = "+91 1234567890"
var overlayStatus = mutableStateOf(false)

data class PermissionStatus(
    val overlay: Boolean = false,
    val notification: Boolean = false,
    val phoneState: Boolean = false,
    val callLog: Boolean = false
)

val permissions = mutableStateOf(
    PermissionStatus(
        overlay = true,
        notification = true,
        phoneState = true,
        callLog = true
    )
)

@Composable
fun CallStatusViewBox(callState: MutableState<CallState>) {
    val backgroundColor = Color(0xFF121212)
    val primaryColor = Color(0xFF3797EF)
    val textColor = Color(0xFFE0E0E0)

    Scaffold(
        contentColor = textColor,
        containerColor = backgroundColor,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        primaryColor,
                        shape = RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "vCaller: Enhanced Privacy",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    ) { it ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            backgroundColor,
                            Color(0xFF1E1E1E)
                        )
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(top = it.calculateTopPadding() - 40.dp)
                .padding(20.dp)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF242424)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NumberSearchView()
                    Spacer(modifier = Modifier.height(20.dp))
                    LottieLoading(id = R.raw.icon, size = 80)
                    Text(
                        text = "Powered by Truecaller SDK",
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "No upstream data collection, No Ads, No Tracking",
                        fontSize = 12.sp,
                        color = Color(0xFFFF6D00).copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Left
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF242424)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {

                    InfoRow(
                        icon = Icons.Filled.Info,
                        label = "State",
                        value = callState.value.state,
                        primaryColor = primaryColor,
                        textColor = textColor
                    )

                    InfoRow(
                        icon = Icons.Filled.Call,
                        label = "Number",
                        value = callState.value.phoneNumber,
                        primaryColor = primaryColor,
                        textColor = textColor
                    )

                    callState.value.callerName?.let {
                        InfoRow(
                            icon = Icons.Filled.Person,
                            label = "Caller",
                            value = it,
                            primaryColor = primaryColor,
                            textColor = textColor
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Logged in as: $loggedInNumber",
                            fontSize = 14.sp,
                            color = textColor.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Button(
                            onClick = {
                                overlayStatus.value = !overlayStatus.value
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (overlayStatus.value) Color(0xFF3797EF) else Color(
                                    0xFF414040
                                ),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (overlayStatus.value) "Disable Overlay" else "Enable Overlay",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                // Open settings
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3797EF),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings Icon",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sign Out",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF242424)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),

                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "API Key",
                        fontSize = 16.sp,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF242424))
                            .padding(1.dp)
                            .border(1.dp, textColor, shape = RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = apiKey,
                            fontSize = 14.sp,
                            color = Color(0xFF3797EF),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    Button(
                        onClick = {
//                        ClipboardManager.getInstance().setPrimaryClip(
//                            ClipData.newPlainText("API Key", apiKey)
//                        )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3797EF),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(
                            text = "Copy API Key",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF242424)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Text(
                        text = "Permissions",
                        fontSize = 16.sp,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            InfoRow(
                                icon = Icons.Filled.Check,
                                label = "Overlay",
                                value = if (overlayStatus.value) "Enabled" else "Disabled",
                                primaryColor = primaryColor,
                                textColor = textColor
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            InfoRow(
                                icon = Icons.Filled.Check,
                                label = "Notification",
                                value = "Enabled",
                                primaryColor = primaryColor,
                                textColor = textColor
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            InfoRow(
                                icon = Icons.Filled.Check,
                                label = "Phone State",
                                value = "Enabled",
                                primaryColor = primaryColor,
                                textColor = textColor
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            InfoRow(
                                icon = Icons.Filled.Check,
                                label = "Call Log",
                                value = "Enabled",
                                primaryColor = primaryColor,
                                textColor = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NumberSearchView() {
    var truecData by remember { mutableStateOf<Truecaller?>(null) }
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Top
    ) {
        var number by remember { mutableStateOf("") }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = number,
                onValueChange = { number = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF242424),
                    unfocusedContainerColor = Color(0xFF242424),
                    focusedLabelColor = Color(0xFF3797EF),
                    unfocusedLabelColor = Color(0xFF3797EF),
                    cursorColor = Color(0xFF3797EF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedPlaceholderColor = Color(0xFF3797EF),
                    focusedBorderColor = Color(0xFF3797EF),
                    unfocusedBorderColor = Color(0xFF3797EF),
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            Thread {
                                truecData = getTruecallerDetails(number)
                            }.start()
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color(0xFF3797EF)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search Icon",
                            tint = Color(0xFF3797EF)
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp),
            )
        }

        if (truecData != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = "Truecaller Data",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = {
                            truecData = null
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color(0xFF3797EF)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear Icon",
                            tint = Color(0xFFD50000)
                        )
                    }
                }

                truecData?.data?.forEach { data ->
                    InfoRow(
                        icon = Icons.Filled.Person,
                        label = "Name",
                        value = data.name,
                        primaryColor = Color(0xFF3797EF),
                        textColor = Color.White
                    )

                    data.phones.forEach { phone ->
                        InfoRow(
                            icon = Icons.Filled.Call,
                            label = "Phone",
                            value = phone.e164Format,
                            primaryColor = Color(0xFF3797EF),
                            textColor = Color.White
                        )
                    }

                    data.addresses.forEach { address ->
                        InfoRow(
                            icon = Icons.Filled.Info,
                            label = "Address",
                            value = parseAddress(address),
                            primaryColor = Color(0xFF3797EF),
                            textColor = Color.White
                        )
                    }

                    if (data.internetAddresses.isNotEmpty()) {
                        data.internetAddresses.forEach { internetAddress ->
                            InfoRow(
                                icon = Icons.Filled.MailOutline,
                                label = "Internet Address",
                                value = parseInternetAddress(internetAddress),
                                primaryColor = Color(0xFF3797EF),
                                textColor = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    primaryColor: Color,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "$label Icon",
            tint = primaryColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.7f), // Lighter color for label
                fontWeight = FontWeight.Normal
            )
            Text(
                text = value,
                fontSize = 16.sp,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


fun getCallStateFromIntent(intent: Intent): CallState {
    val stateString = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
    val state = when (stateString) {
        TelephonyManager.EXTRA_STATE_IDLE -> "IDLE"
        TelephonyManager.EXTRA_STATE_OFFHOOK -> "OFFHOOK"
        TelephonyManager.EXTRA_STATE_RINGING -> "RINGING"
        else -> "UNKNOWN"
    }


//    CallScreeningService

    if (state == "RINGING") {
        val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        return CallState(state, phoneNumber ?: "")
    }

    return CallState(state)
}