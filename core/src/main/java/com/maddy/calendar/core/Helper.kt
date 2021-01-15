package com.maddy.calendar.core

object Helper {
    fun floorDiv(x: Long, y: Long): Int {
        var r = x / y
        // if the signs are different and modulo not zero, round down
        if (x xor y < 0 && r * y != x) {
            r--
        }
        return r.toInt()
    }

    fun floorMod(x: Long, y: Long): Long {
        return x - floorDiv(x, y) * y
    }

    fun addExact(x: Long, y: Long): Long {
        val r: Long = x + y
        return if (x xor r and (y xor r) < 0L) {
            throw ArithmeticException("long overflow")
        } else {
            r
        }
    }
}

