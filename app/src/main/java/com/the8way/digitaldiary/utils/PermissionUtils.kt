package com.the8way.digitaldiary.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.the8way.digitaldiary.R

object PermissionUtils {

    private const val REQUEST_CODE_PERMISSIONS = 100

    fun checkAndRequestPermissions(context: Context, activity: Activity): Boolean {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )

        val listPermissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        return if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toTypedArray(), REQUEST_CODE_PERMISSIONS)
            false
        } else {
            true
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        context: Context,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            val perms = permissions.mapIndexed { index, permission ->
                permission to (grantResults[index] == PackageManager.PERMISSION_GRANTED)
            }.toMap()

            if (perms.values.all { it }) {
                onGranted()
            } else {
                onDenied()
                Toast.makeText(context, R.string.perms_denied, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
