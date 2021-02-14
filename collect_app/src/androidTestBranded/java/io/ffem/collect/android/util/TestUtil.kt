package io.ffem.collect.android.util

import android.os.SystemClock

object TestUtil {

    fun sleep(time: Int) {
        SystemClock.sleep(time.toLong())
    }
}