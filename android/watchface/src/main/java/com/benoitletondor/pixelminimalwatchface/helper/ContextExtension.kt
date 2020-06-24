package com.benoitletondor.pixelminimalwatchface.helper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.util.DisplayMetrics
import android.util.Log
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

fun Context.dpToPx(dp: Int): Int {
    val displayMetrics = resources.displayMetrics
    return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}

fun Context.isScreenRound() = resources.configuration.isScreenRound

fun Context.isServiceAvailable(packageName: String, serviceName: String): Boolean {
    return try {
        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SERVICES)
        val services = packageInfo.services ?: return false

        services.firstOrNull { it.name == serviceName } != null
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Context.openActivity(packageName: String, activityName: String) {
    try {
        startActivity(Intent().apply {
            component = ComponentName(
                packageName,
                activityName
            )
            flags = FLAG_ACTIVITY_NEW_TASK
        })
    } catch (t: Throwable) {
        Log.e("Pixel Minimal Watch Face", "Can't open activity: $activityName", t)
    }
}