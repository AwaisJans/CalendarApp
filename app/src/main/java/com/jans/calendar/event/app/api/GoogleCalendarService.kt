package com.jans.calendar.event.app.api

import android.content.Context
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.EventDateTime
import java.io.IOException
import java.security.GeneralSecurityException
import com.google.api.client.json.JsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.jans.calendar.event.app.R
import java.util.*

class GoogleCalendarService {


    @Throws(IOException::class, GeneralSecurityException::class)
    fun addEvent(context: Context,summary: String?, location: String?, description: String?, startDate: Date?, endDate: Date?) {

    }

//    companion object {
//        @JvmStatic
//        fun main(args: Array<String>) {
//            val googleCalendarService = GoogleCalendarService()
//            val summary = "Test Event"
//            val location = "Test Location"
//            val description = "This is a test event"
//            val startDate = Date() // Set your start date here
//            val endDate = Date(startDate.time + 3600000) // Set your end date here (1 hour later)
//            try {
//                googleCalendarService.addEvent(summary, location, description, startDate, endDate)
//                println("Event added successfully.")
//            } catch (e: Exception) {
//                println("Error adding event: $e")
//            }
//        }
//    }
}