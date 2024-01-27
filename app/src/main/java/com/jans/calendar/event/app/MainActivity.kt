package com.jans.calendar.event.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.CalendarContract.Calendars
import android.provider.CalendarContract.Events
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jans.calendar.event.app.api.CalendarApiScreen
import com.jans.calendar.event.app.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.util.Calendar
import java.util.Calendar.JANUARY
import java.util.TimeZone


class MainActivity : AppCompatActivity() {


    lateinit var b: ActivityMainBinding

    private val permissions =
        listOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        requestCalendarPermissions()




        b.idCalApi.setOnClickListener {


// get the event ID that is the last element in the Uri

//            val calWriter = CalendarWriter(this@MainActivity)
//            val calendars = calWriter.getCalendars()
//            val calendarIds = mutableListOf<Long>()
//            for (calendar in calendars) {
//                Log.d("how123", calendar.accountName.toString())
//
//                calendarIds.add(calendar.calendarId)
//            }

            startActivity(Intent(this, CalendarApiScreen::class.java))

//            val num = (0 until calendarIds.size).random()
//            calWriter.addEvents(calendarIds[num], eventList())
        }

    }


    /*
    to add locally events in local calendars use this code
     */

    private fun requestCalendarPermissions() {
        Dexter.withContext(this)
            .withPermissions(permissions).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        Toast.makeText(
                            this@MainActivity,
                            "Calendar Permission Granted",
                            Toast.LENGTH_SHORT
                        ).show()
                        val calWriter = CalendarWriter(this@MainActivity)
                        val calendars = calWriter.getCalendars()
                        val calendarIds = mutableListOf<Long>()
                        for (calendar in calendars) {
                            Log.d("how123", calendar.accountName.toString())
                            calendarIds.add(calendar.calendarId)
                        }

                        Log.d("how123", calendarIds.size.toString())
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Give Permissions from App Info Settings",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) {
                    token!!.continuePermissionRequest()
                }
            }).check()
    }

    //
    class CalendarWriter(private val context: Context) {
        fun addEvents(calendarId: Long, events: List<CalendarEvent>): List<Boolean> {
            val results = mutableListOf<Boolean>()
            var eventAdded: Boolean?
//            Toast.makeText(context, "Added Successfully", Toast.LENGTH_SHORT).show()
            for (event in events) {
                val values = ContentValues().apply {
                    put(Events.DTSTART, event.startDateTime.timeInMillis)
                    put(Events.DTEND, event.endDateTime.timeInMillis)
                    put(Events.TITLE, event.title)
                    put(Events.DESCRIPTION, event.description)
                    put(Events.EVENT_LOCATION, event.location)
                    put(Events.CALENDAR_ID, calendarId)
//                    put(Calendars.SYNC_EVENTS, 1);
//                    put(Calendars.VISIBLE, 1);
                    put(Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                }

                val uri = context.contentResolver.insert(Events.CONTENT_URI, values)
                eventAdded = uri != null
//                context.contentResolver.update(ContentUris.withAppendedId(Calendars.CONTENT_URI, calendarId), values, null, null);


                val cr = context.contentResolver
                val values1 = ContentValues()
                values1.put(Calendars.SYNC_EVENTS, 1)
                values1.put(Calendars.VISIBLE, 1)

                cr.update(
                    ContentUris.withAppendedId(Events.CONTENT_URI, calendarId), values, null, null
                )



                results.add(eventAdded)

                if (eventAdded) {
                    showToast("Event added successfully: ${event.title}")
                } else {
                    showToast("Failed to add event: ${event.title}")
                }
            }


            return results
        }

        private fun showToast(msg: String) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
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
                    val displayName =
                        it.getString(it.getColumnIndex(Calendars.CALENDAR_DISPLAY_NAME))
                    val ownerAccount = it.getString(it.getColumnIndex(Calendars.OWNER_ACCOUNT))

                    val calendarInfo =
                        CalendarInfo(calendarId, accountName, displayName, ownerAccount)
                    calendars.add(calendarInfo)
                    val cr: ContentResolver = context.contentResolver
                    val values = ContentValues()
                    values.put(Calendars.SYNC_EVENTS, 1)
                    values.put(Calendars.VISIBLE, 1)

                    cr.update(
                        ContentUris.withAppendedId(uri, calendarId),
                        values,
                        null,
                        null
                    )

                    Log.d("myCalendarInfo", calendars.toString())


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
                Calendar.getInstance().apply { set(2024, JANUARY, 9, 10, 0) },
                Calendar.getInstance().apply { set(2024, JANUARY, 9, 11, 0) }
            ),
            CalendarEvent(
                "Meeting 2",
                "Discuss design changes",
                "Conference Room 2",
                Calendar.getInstance().apply { set(2024, JANUARY, 8, 14, 0) },
                Calendar.getInstance().apply { set(2024, JANUARY, 8, 15, 0) }
            ),
//            CalendarEvent(
//                "Meeting 3",
//                "Discuss project updates",
//                "Conference Room 3",
//                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 3, 10, 0) },
//                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 3, 11, 0) }
//            ),
//            CalendarEvent(
//                "Meeting 4",
//                "Discuss design changes",
//                "Conference Room 4",
//                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 4, 14, 0) },
//                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 4, 15, 0) }
//            ),
//            CalendarEvent(
//                "Meeting 5",
//                "Discuss project updates",
//                "Conference Room 5",
//                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 5, 10, 0) },
//                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 5, 11, 0) }
//            ),
//            CalendarEvent(
//                "Meeting 6",
//                "Discuss design changes",
//                "Conference Room 6",
//                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 6, 14, 0) },
//                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 6, 15, 0) }
//            ),
//            CalendarEvent(
//                "Meeting 7",
//                "Discuss project updates",
//                "Conference Room 7",
//                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 7, 10, 0) },
//                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 7, 11, 0) }
//            ),
//            CalendarEvent(
//                "Meeting 8",
//                "Discuss design changes",
//                "Conference Room 8",
//                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 8, 14, 0) },
//                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 8, 15, 0) }
//            ),
//            CalendarEvent(
//                "Meeting 9",
//                "Discuss project updates",
//                "Conference Room 9",
//                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 9, 10, 0) },
//                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 9, 11, 0) }
//            ),
//            CalendarEvent(
//                "Meeting 10",
//                "Discuss design changes",
//                "Conference Room 10",
//                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 10, 14, 0) },
//                Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 10, 15, 0) }
//            ),
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