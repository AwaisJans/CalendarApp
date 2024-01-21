package com.jans.calendar.event.app

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.TimeZone

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        callAddToCalendar()
    }

    private fun callAddToCalendar() {
        val builder = AlertDialog.Builder(this)
        val startDate = LocalDateTime.of(2024, 1, 8, 10, 0) // January 1, 2023, 10:00 AM
        val zdt = ZonedDateTime.of(startDate, ZoneId.systemDefault())
        val date = zdt.toInstant().toEpochMilli()
        builder.setTitle("Confirmation")
            .setMessage("Do You want to Add To Calendar?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                addEventToCalendar(
                    this@MainActivity2,
                    "Meeting86878",
                    "Discuss project updates",
                    "Conference Room",
                    CalendarContract.Events.STATUS_CONFIRMED,
                    date,
                    true,
                    true
                )
                finish()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, id ->
                finish()
                dialog.dismiss()
            }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    companion object {
        fun addEventToCalendar(
            context: Context, title: String?,
            addInfo: String?, place: String?, status: Int, startDate: Long,
            isRemind: Boolean, isMailService: Boolean
        ) {
            val cr = context.contentResolver
            val eventUriStr = "content://com.android.calendar/events"
            val event = ContentValues()
            // id, We need to choose from our mobile foEr primary its 1
            event.put(CalendarContract.Events.CALENDAR_ID, 1)
            event.put(CalendarContract.Events.TITLE, title)
            event.put(CalendarContract.Events.DESCRIPTION, addInfo)
            event.put(CalendarContract.Events.EVENT_LOCATION, place)
            event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            // For next 1hr
            val endDate = startDate + 1000 * 60 * 60
            event.put(CalendarContract.Events.DTSTART, startDate)
            event.put(CalendarContract.Events.DTEND, endDate)
            //If it is bithday alarm or such kind (which should remind me for whole day) 0 for false, 1 for true
            // values.put("allDay", 1);
            //status =  CalendarContract.Events.AVAILABILITY_TENTATIVE;
            event.put(
                CalendarContract.Events.STATUS,
                status
            ) //CalendarContract.Events.AVAILABILITY_BUSY
            event.put(CalendarContract.Events.HAS_ALARM, 1)
            val eventUri = cr.insert(Uri.parse(eventUriStr), event)
            Toast.makeText(context, "added successfully", Toast.LENGTH_SHORT).show()
            val eventID = eventUri!!.lastPathSegment!!.toLong()
            if (isRemind) {
                val method = 1
                val minutes = 35
                addAlarms(cr, eventID, minutes, method)
            }
            if (isMailService) {
                var attendeeName = "Rick"
                var attendeeEmail = "writesowais@gmail.com"
                var attendeeRelationship = 2
                var attendeeType = 2
                var attendeeStatus = 1
                addAttendees(
                    cr, eventID, attendeeName, attendeeEmail,
                    attendeeRelationship, attendeeType, attendeeStatus
                )
                attendeeName = "Marion"
                attendeeEmail = "writesowais@gmail.com"
                attendeeRelationship = 4
                attendeeType = 2
                attendeeStatus = 3
                addAttendees(
                    cr, eventID, attendeeName, attendeeEmail,
                    attendeeRelationship, attendeeType, attendeeStatus
                )
            }
        }

        private fun addAlarms(
            cr: ContentResolver, eventId: Long,
            minutes: Int, method: Int
        ) {
            val reminderUriString = "content://com.android.calendar/reminders"
            val reminderValues = ContentValues()
            reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId)
            // Default value of the system. Minutes is a integer
            reminderValues.put(CalendarContract.Reminders.MINUTES, minutes)
            // Alert Methods: Default(0), Alert(1), Email(2), SMS(3)
            reminderValues.put(CalendarContract.Reminders.METHOD, method)
            cr.insert(Uri.parse(reminderUriString), reminderValues)
        }

        private fun addAttendees(
            cr: ContentResolver, eventId: Long,
            attendeeName: String, attendeeEmail: String,
            attendeeRelationship: Int, attendeeType: Int, attendeeStatus: Int
        ) {
            val attendeuesesUriString = "content://com.android.calendar/attendees"
            /********* To add multiple attendees need to insert ContentValues multiple times  */
            val attendeesValues = ContentValues()
            attendeesValues.put(CalendarContract.Attendees.EVENT_ID, eventId)
            // Attendees name
            attendeesValues.put(
                CalendarContract.Attendees.ATTENDEE_NAME,
                attendeeName
            )
            // Attendee email
            attendeesValues.put(
                CalendarContract.Attendees.ATTENDEE_EMAIL,
                attendeeEmail
            )
            // ship_Attendee(1), Relationship_None(0), Organizer(2), Performer(3), Speaker(4)
            attendeesValues.put(
                CalendarContract.Attendees.ATTENDEE_RELATIONSHIP,
                attendeeRelationship
            )
            // None(0), Optional(1), Required(2), Resource(3)
            attendeesValues.put(
                CalendarContract.Attendees.ATTENDEE_TYPE,
                attendeeType
            )
            // None(0), Accepted(1), Decline(2), Invited(3), Tentative(4)
            attendeesValues.put(
                CalendarContract.Attendees.ATTENDEE_STATUS,
                attendeeStatus
            )
            cr.insert(Uri.parse(attendeuesesUriString), attendeesValues) //Uri attendeuesesUri =
        }
    }
}