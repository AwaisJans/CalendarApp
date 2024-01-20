package com.jans.calendar.event.app

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CalendarContract.Calendars
import android.provider.CalendarContract.Events
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.nio.file.Files.size
import java.util.Calendar
import java.util.TimeZone


class MainActivity : AppCompatActivity() {

    lateinit var btnAdd: TextView


    var permissions = listOf(
        android.Manifest.permission.READ_CALENDAR,
        android.Manifest.permission.WRITE_CALENDAR
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btnAdd = findViewById(R.id.idAdd)

        for (element in permissions) {
            if (ContextCompat.checkSelfPermission(this, element)
                == PackageManager.PERMISSION_GRANTED) {
                val calendarWriter = CalendarWriter(this)

                val calendars = calendarWriter.getCalendars()

                val selectedCalendar = calendars.firstOrNull()


                btnAdd.setOnClickListener {
                    selectedCalendar?.let {
                        calendarWriter.addEvents(it.calendarId, eventList())
                    }
                }
            }
            else{
                btnAdd.setOnClickListener {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
                requestCalendarPermissions()
            }

        }




    }


    private fun requestCalendarPermissions(){
        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.READ_CALENDAR,
                android.Manifest.permission.WRITE_CALENDAR,
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()){
                        Toast.makeText(this@MainActivity, "Calendar Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this@MainActivity, "Give Permissions from App Info Settings", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?) {
                    token!!.continuePermissionRequest()
                }
            }).check()
    }


    class CalendarWriter(private val context: Context) {
        fun addEvents(calendarId: Long, events: List<CalendarEvent>): List<Boolean> {
            val results = mutableListOf<Boolean>()
            var eventAdded:Boolean?
            showToast()
            for (event in events) {
                val values = ContentValues().apply {
                    put(Events.DTSTART, event.startDateTime.timeInMillis)
                    put(Events.DTEND, event.endDateTime.timeInMillis)
                    put(Events.TITLE, event.title)
                    put(Events.DESCRIPTION, event.description)
                    put(Events.EVENT_LOCATION, event.location)
                    put(Events.CALENDAR_ID, calendarId)
                    put(Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                }

                val uri = context.contentResolver.insert(Events.CONTENT_URI, values)
                eventAdded = uri != null

                results.add(eventAdded)

//                if (eventAdded) {
//                    showToast("Event added successfully: ${event.title}")
//                } else {
//                    showToast("Failed to add event: ${event.title}")
//                }
            }


            return results
        }
        private fun showToast() {
            Toast.makeText(context, "Event Added Successfully", Toast.LENGTH_SHORT).show()
        }

        @SuppressLint("Range")
        fun getCalendars(): List<CalendarInfo> {
            val calendars = mutableListOf<CalendarInfo>()
            val projection = arrayOf(
                Calendars._ID,
                Calendars.ACCOUNT_NAME,
                Calendars.CALENDAR_DISPLAY_NAME,
                Calendars.OWNER_ACCOUNT
            )
            val uri = Calendars.CONTENT_URI
            val cursor = context.contentResolver.query(uri, projection, null, null, null)

            cursor?.use {
                while (it.moveToNext()) {
                    val calendarId = it.getLong(it.getColumnIndex(Calendars._ID))
                    val accountName = it.getString(it.getColumnIndex(Calendars.ACCOUNT_NAME))
                    val displayName = it.getString(it.getColumnIndex(Calendars.CALENDAR_DISPLAY_NAME))
                    val ownerAccount = it.getString(it.getColumnIndex(Calendars.OWNER_ACCOUNT))

                    val calendarInfo = CalendarInfo(calendarId, accountName, displayName, ownerAccount)
                    calendars.add(calendarInfo)
                }
            }

            return calendars
        }

        data class CalendarInfo(
            val calendarId: Long,
            val accountName: String?,
            val displayName: String?,
            val ownerAccount: String?
        )
    }

    private fun eventList(): List<CalendarEvent> {
        return listOf(
            CalendarEvent(
                "Meeting 1",
                "Discuss project updates",
                "Conference Room 1",
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 1, 10, 0) },
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 1, 11, 0) }
            ),
            CalendarEvent(
                "Meeting 2",
                "Discuss design changes",
                "Conference Room 2",
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 2, 14, 0) },
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 2, 15, 0) }
            ),
            CalendarEvent(
                "Meeting 3",
                "Discuss project updates",
                "Conference Room 3",
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 3, 10, 0) },
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 3, 11, 0) }
            ),
            CalendarEvent(
                "Meeting 4",
                "Discuss design changes",
                "Conference Room 4",
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 4, 14, 0) },
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 4, 15, 0) }
            ),
            CalendarEvent(
                "Meeting 5",
                "Discuss project updates",
                "Conference Room 5",
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 5, 10, 0) },
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 5, 11, 0) }
            ),
            CalendarEvent(
                "Meeting 6",
                "Discuss design changes",
                "Conference Room 6",
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 6, 14, 0) },
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 6, 15, 0) }
            ),
            CalendarEvent(
                "Meeting 7",
                "Discuss project updates",
                "Conference Room 7",
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 7, 10, 0) },
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 7, 11, 0) }
            ),
            CalendarEvent(
                "Meeting 8",
                "Discuss design changes",
                "Conference Room 8",
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 8, 14, 0) },
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 8, 15, 0) }
            ),
            CalendarEvent(
                "Meeting 9",
                "Discuss project updates",
                "Conference Room 9",
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 9, 10, 0) },
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 9, 11, 0) }
            ),
            CalendarEvent(
                "Meeting 10",
                "Discuss design changes",
                "Conference Room 10",
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 10, 14, 0) },
                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 10, 15, 0) }
            ),
        )
    }

    data class CalendarEvent(
        val title: String,
        val description: String,
        val location: String,
        val startDateTime: Calendar,
        val endDateTime: Calendar
    )

}