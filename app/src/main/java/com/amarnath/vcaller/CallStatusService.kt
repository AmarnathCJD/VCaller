package com.amarnath.vcaller

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

class CallStatusService : Service() {
    private val callState = MutableLiveData<CallState>()
    private lateinit var receiver: BroadcastReceiver
    private var popupViews = mutableListOf<View>()
    private var windowManager: WindowManager? = null

    override fun onCreate() {
        super.onCreate()
        setupBroadcastReceiver()
        observeCallState()
        startForegroundService()
    }

    private fun setupBroadcastReceiver() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val newCallState = getCallStateFromIntent(intent)
                callState.postValue(newCallState)
            }
        }
        val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        registerReceiver(receiver, filter)
    }

    private fun observeCallState() {
        callState.observeForever { state ->
            updateNotification(state)
            if (state.state == "RINGING") showOverlayPopup(state.phoneNumber)
            else if (state.state == "IDLE") hideOverlayPopup()
        }
    }

    private fun updateNotification(callState: CallState) {
        val notification = NotificationCompat.Builder(this, "CALL_STATUS_CHANNEL")
            //.setSmallIcon(R.drawable.ic_phone)
            .setContentTitle("Call Status")
            .setContentText("State: ${callState.state} || Number: ${callState.phoneNumber}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        startForeground(1, notification)
    }

    private fun showOverlayPopup(phoneNumber: String) {
        if (windowManager == null) windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )
        params.verticalMargin = -0.265f

        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_call_status, null)
        popupView.findViewById<TextView>(R.id.phoneNumberText).text = phoneNumber

        val animation = AnimationUtils.loadAnimation(this, R.anim.anim)
        popupView.startAnimation(animation)

        windowManager!!.addView(popupView, params)
        popupViews.add(popupView)

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                windowManager!!.removeView(popupView)
                popupViews.remove(popupView)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }, 5000)
    }

    private fun hideOverlayPopup() {
        popupViews.forEach {
            try {
                windowManager!!.removeView(it)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
        popupViews.clear()
    }


    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, "CALL_STATUS_CHANNEL")
          //  .setSmallIcon(R.drawable.ic_phone)
            .setContentTitle("Call Monitoring Active")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}


@Composable
fun LottieLoading(id: Int, size: Int = 200) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec
            .RawRes(id)
    )

    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true,

        speed = 1f,
        restartOnPlay = true
    )

    Column(
        Modifier
            .background(Color.Transparent)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition,
            progress,
            modifier = Modifier.size(size.dp)
        )
    }
}