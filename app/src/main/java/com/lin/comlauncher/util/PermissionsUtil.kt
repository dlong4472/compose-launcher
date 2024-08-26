package com.lin.comlauncher.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionsUtil {

    fun checkPermissions(arrayPermission: Array<String>, c: Context): Boolean {
        for (permission in arrayPermission) {
            if (ContextCompat.checkSelfPermission(
                    c,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

}