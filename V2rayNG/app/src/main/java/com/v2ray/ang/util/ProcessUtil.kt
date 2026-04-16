package com.v2ray.ang.util

import android.app.Application
import android.os.Build
import java.io.FileInputStream

object ProcessUtil {
    fun currentProcessName(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return Application.getProcessName()
        }

        return runCatching {
            FileInputStream("/proc/self/cmdline").bufferedReader().use { it.readLine() }
                ?.trim('\u0000')
                .orEmpty()
        }.getOrDefault("")
    }
}
