package com.example.doancs.Screen


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs.component.CalendarView_Schedule
import com.example.dacs.component.ScheduleItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate

data class Schedule(
    val id: String,
    val destination: String,
    val date: LocalDate,
    val place: String,
    var hour: Int = 0,
    var minute: Int = 0,
    var plan: String = ""
)

@Composable
fun ScheduleScreen() {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var schedules by remember { mutableStateOf(listOf<Schedule>()) }
    val database = FirebaseDatabase.getInstance().reference
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(selectedDate) {
        if (currentUser != null) {
            val dateString = selectedDate.toString()
            val scheduleRef = database.child("schedule").child(currentUser.uid).child(dateString)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newSchedules = mutableListOf<Schedule>()
                    for (childSnapshot in snapshot.children) {
                        val id = childSnapshot.key ?: ""
                        val locationName = childSnapshot.child("locationName").getValue(String::class.java) ?: ""
                        val locationPlace = childSnapshot.child("locationPlace").getValue(String::class.java) ?: ""
                        val plan = childSnapshot.child("plan").getValue(String::class.java) ?: ""
                        val timeString = childSnapshot.child("time").getValue(String::class.java)
                        val hour = timeString?.substringBefore(":")?.toIntOrNull() ?: 0
                        val minute = timeString?.substringAfter(":")?.toIntOrNull() ?: 0
                        newSchedules.add(Schedule(id, locationName, selectedDate, locationPlace, hour, minute, plan))
                    }
                    // Sort schedules by hour and minute
                    newSchedules.sortBy { it.hour * 60 + it.minute }
                    schedules = newSchedules
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            }

            scheduleRef.addValueEventListener(listener)
        }
    }

    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Schedule",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            CalendarView_Schedule(
                currentDate = remember { mutableStateOf(selectedDate) }
            ) { date ->
                selectedDate = date
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "My Schedule",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            schedules.forEach { schedule ->
                ScheduleItem(
                    schedule = schedule,
                    onUpdatePlan = { updatedPlan, updatedHour, updatedMinute ->
                        val dateString = schedule.date.toString()
                        database.child("schedule").child(currentUser!!.uid).child(dateString)
                            .child(schedule.id).child("plan")
                            .setValue(updatedPlan)
                        database.child("schedule").child(currentUser.uid).child(dateString)
                            .child(schedule.id).child("time")
                            .setValue("$updatedHour:$updatedMinute")
                    },
                    onClearSchedule = {
                        val dateString = schedule.date.toString()
                        database.child("schedule").child(currentUser!!.uid).child(dateString)
                            .child(schedule.id)
                            .removeValue()
                    }
                )
            }
        }
    }
}

