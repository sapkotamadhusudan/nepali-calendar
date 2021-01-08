package com.maddy.calendar

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.maddy.calendar.core.ILocalDate

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val welcomeTV = findViewById<TextView>(R.id.welcomeTV)

        val todayNepaliDate = ILocalDate.nowBS()

        welcomeTV.text = resources.getString(
            R.string.welcome_message,
            todayNepaliDate.year,
            todayNepaliDate.month,
            todayNepaliDate.day
        )
    }
}