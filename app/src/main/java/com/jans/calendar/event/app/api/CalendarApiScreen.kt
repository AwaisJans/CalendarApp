package com.jans.calendar.event.app.api

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.android.material.snackbar.Snackbar
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventReminder
import com.jans.calendar.event.app.R
import com.jans.calendar.event.app.databinding.ActivityCalendarApiScreenBinding
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Collections
import java.util.Locale


class CalendarApiScreen : AppCompatActivity(), EasyPermissions.PermissionCallbacks {


    private var calendarService: Calendar? = null
    private lateinit var googleSignInAccount: GoogleSignInAccount
    private var signInClient: GoogleSignInClient? = null

    private lateinit var binding: ActivityCalendarApiScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarApiScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        signInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = signInClient?.signInIntent
        startActivityForResult(signInIntent!!, RC_SIGN_IN)


    }


    private val eventList = listOf(
        EventModel(
            "Event 0",
            "Description for Event 0",
            "22-01-2024 09:55:00",
            "22-01-2024 10:55:00"
        ),
        EventModel(
            "Event 1",
            "Description for Event 1",
            "23-01-2024 09:55:00",
            "23-01-2024 10:55:00"
        ),
        EventModel(
            "Event 2",
            "Description for Event 2",
            "24-01-2024 11:30:00",
            "24-01-2024 12:30:00"
        ),
        EventModel(
            "Event 3",
            "Description for Event 3",
            "25-01-2024 14:00:00",
            "25-01-2024 15:00:00",
        ),
        EventModel(
            "Event 4",
            "Description for Event 4",
            "26-01-2024 16:45:00",
            "26-01-2024 17:45:00",
        )
    )

    // Add Events Here
    private fun initializeCalendarService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            this,
            Collections.singleton(CalendarScopes.CALENDAR)
        )
        credential.selectedAccount = account.account

        calendarService = Calendar.Builder(
            AndroidHttp.newCompatibleTransport(),
            AndroidJsonFactory(),
            credential
        )
            .setApplicationName(getString(R.string.app_name))
            .build()





        binding.tvSelectedAccount.text = credential.selectedAccount.name.toString()

        binding.btAdd.setOnClickListener {
            if (credential.selectedAccount == null) {
                Toast.makeText(this, "Account not signed in", Toast.LENGTH_SHORT).show()
            } else {
                for (event in eventList) {
                    GoogleCalendar.Companion.InsertData(credential).execute(
                        makeListEvent(
                            event.eventName,
                            event.eventDescription,
                            event.startDate,
                            event.endDate,
                        )
                    )
                }

            }
        }
    }

    data class EventModel(
        val eventName: String,
        val eventDescription: String,
        val startDate: String,
        val endDate: String,
    )

    class GoogleCalendar(context: Activity) {

        var mCredential: GoogleAccountCredential

        companion object {
            internal const val REQUEST_AUTHORIZATION = 1001

            var lastAdded: ArrayList<Any> = ArrayList()
            var lastError: String = ""

            class InsertData internal constructor(credential: GoogleAccountCredential) :
                AsyncTask<ArrayList<Any>, Void, Void>() {
                private var mService: Calendar? = null

                init {
                    val transport = AndroidHttp.newCompatibleTransport()
                    val jsonFactory = JacksonFactory.getDefaultInstance()
                    mService = Calendar.Builder(
                        transport, jsonFactory, credential
                    )
                        .setApplicationName("CalendarEventApp")
                        .build()
                }

                override fun doInBackground(vararg params: ArrayList<Any>?): Void? {
                    if (params.isNotEmpty()) {
                        for (subList: ArrayList<Any>? in params) {
                            if (subList != null) {
                                insertEvent(
                                    subList[0] as String,
                                    subList[1] as String,
                                    subList[2] as String,
                                    subList[3] as String,
                                    subList[4] as Boolean,
                                    subList[5] as Boolean,
                                    subList[6] as Boolean,
                                    subList[7] as Activity
                                )
                            }
                        }
                    }
                    return null
                }

                @Throws(IOException::class)
                private fun insertEvent(
                    summary: String, des: String, startDate: String, endDate: String,
                    repeating: Boolean, reminder: Boolean, dateTime: Boolean, context: Activity
                ) {
                    try {
                        val cont = context
                        if (!startDate.isEmpty()) {
                            val startDater: DateTime
                            val endDater: DateTime
                            val formatter: SimpleDateFormat
                            println(startDate)
                            println(endDate)
                            if (!dateTime) {
                                formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                startDater = DateTime(formatter.parse(startDate))
                                endDater = DateTime(formatter.parse(endDate))
                            } else {
                                formatter =
                                    SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                                startDater = DateTime(formatter.parse(startDate))
                                endDater = DateTime(formatter.parse(endDate))
                            }
                            val event = Event()
                                .setSummary(summary)
                                .setDescription(des)
                            val start = EventDateTime()
                                .setDateTime(startDater)
                            event.start = start
                            val end = EventDateTime()
                                .setDateTime(endDater)
                            event.end = end
                            if (repeating) {
                                val recurrence = arrayOf("RRULE:FREQ=WEEKLY")
                                event.recurrence = Arrays.asList(*recurrence)
                            }
                            if (reminder) {
                                val reminderOverrides = arrayOf(
                                    EventReminder().setMethod("email").setMinutes(24 * 60),
                                    EventReminder().setMethod("popup").setMinutes(10)
                                )
                                val reminders = Event.Reminders()
                                    .setUseDefault(false)
                                    .setOverrides(listOf(*reminderOverrides))
                                event.reminders = reminders
                            } else {
                                event.reminders = Event.Reminders()
                                    .setUseDefault(false)
                            }
                            val calendarId = "primary"
                            //val calendarId = context.getString(R.string.app_name)
                            mService?.events()?.insert(calendarId, event)
                                ?.setSendNotifications(true)?.execute()
                            lastError = "Event $summary added successfully to google calendar!!"
                            val rootView: View =
                                cont.findViewById(android.R.id.content) // Replace with your root view
                            Snackbar.make(rootView, lastError, Snackbar.LENGTH_SHORT).show()

                        }
                    } catch (e: UserRecoverableAuthIOException) {
                        lastAdded = arrayListOf(
                            summary,
                            des,
                            startDate,
                            endDate,
                            repeating,
                            reminder,
                            dateTime
                        )
                        context.startActivityForResult(e.intent, REQUEST_AUTHORIZATION)
                        lastError =
                            "something went wrong, adding the event to your google calendar (${e.message})"
                        val rootView: View =
                            context.findViewById(android.R.id.content) // Replace with your root view
                        Snackbar.make(rootView, lastError, Snackbar.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        println("couldn't insert into Google Calendar! " + e.message)
                        lastError =
                            "something went wrong, adding the event to your google calendar (${e.message})"
                        val rootView: View =
                            context.findViewById(android.R.id.content) // Replace with your root view
                        Snackbar.make(rootView, lastError, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }


        }

        val SCOPES = arrayOf(CalendarScopes.CALENDAR)

        init {
            // Initialize credentials and service object.
            mCredential = GoogleAccountCredential.usingOAuth2(
                context.applicationContext, Arrays.asList(*SCOPES)
            )
                .setBackOff(ExponentialBackOff())
        }

    }

    private fun handleSignInResult(completedTask: GoogleSignInAccount) {
        try {
            requestCalendarPermissions(completedTask)
        } catch (e: ApiException) {
            Log.w("CalendarIntegration", "signInResult:failed code=${e.statusCode}")
            Toast.makeText(this, "Sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestCalendarPermissions(account: GoogleSignInAccount) {
        initializeCalendarService(account)

    }


    private fun makeListEvent(
        title: String,
        desc: String,
        startDate: String,
        endDate: String
    ): ArrayList<Any> {
        return arrayListOf(
            title,
            desc,
            startDate,
            endDate,
            false,
            false,
            true,
            this@CalendarApiScreen
        )
    }


    // Handle permissions grant result
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        // Permissions granted, proceed with API requests
        initializeCalendarService(googleSignInAccount)
    }

    // Handle permissions denied result
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        // Permissions denied, show a message or handle accordingly
        Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val RC_SIGN_IN = 1001
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val taskResult = task.result
                Toast.makeText(this@CalendarApiScreen, "success", Toast.LENGTH_SHORT).show()
                handleSignInResult(taskResult)
            } catch (e: RuntimeExecutionException) {
            }
        }
    }
}


