package com.example.dacs.component

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.example.doancs.R
import com.example.doancs.Screen.Location
import com.example.doancs.Screen.LocationData
import com.example.doancs.Screen.Schedule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class Screen(val route: String, val icon: ImageVector, val title: String) {
    object Left : Screen("left", Icons.Default.KeyboardArrowLeft, "Left")
    object Right : Screen("right", Icons.Default.KeyboardArrowRight, "Right")
}

@Composable
fun SignInTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None


) {
    OutlinedTextField(
        modifier = Modifier,
        value = value,
        onValueChange = onValueChange,
        label = { Text(labelText) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(30)

    )
}


@Composable
fun HeaderText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.displayMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier,
        fontSize = 30.sp

    )
}

//____________________________________________________
//Details

@Composable
fun ViewBar(
    rating: Float, modifier: Modifier = Modifier, maxRating: Int = 5
) {
    Row(modifier = modifier) {
        repeat(maxRating) { index ->
            val tint = if (index < rating) Color.Black else Color.Gray
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "View",
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
@Composable
fun CalendarView(
    currentDate: MutableState<LocalDate>,
    locationData: LocationData,
    modifier: Modifier = Modifier,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val startOfWeek = currentDate.value.minusDays(currentDate.value.dayOfWeek.value.toLong() - 1)
    val endOfWeek = startOfWeek.plusDays(6)

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = currentDate.value.month.name.replaceFirstChar { it.uppercase() },
                style = TextStyle(
                    fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = currentDate.value.year.toString(), style = TextStyle(
                    fontSize = 18.sp, fontWeight = FontWeight.Bold
                ), modifier = Modifier.padding(10.dp)
            )
            IconButton(onClick = {
                currentDate.value = currentDate.value.minusDays(7)
            }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Previous"
                )
            }
            IconButton(onClick = {
                currentDate.value = currentDate.value.plusDays(7)
            }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Next"
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(7) { dayOfWeek ->
                Text(
                    text = DayOfWeek.values()[dayOfWeek].name.take(3), style = TextStyle(
                        fontSize = 14.sp, fontWeight = FontWeight.Bold
                    ), modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(7) { dayOfWeek ->
                val date = startOfWeek.plusDays(dayOfWeek.toLong())
                DayCell(
                    date = date,
                    isSelected = date == currentDate.value,
                    isToday = date == today,
                    onClick = { selectedDate ->
                        onDateSelected(selectedDate)
                    },
                    locationData = locationData
                )
            }
        }
    }
}

@Composable
fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: (LocalDate) -> Unit,
    locationData: LocationData,
    modifier: Modifier = Modifier
) {
    val backgroundColor =
        if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent
    val textColor = if (isToday) MaterialTheme.colorScheme.primary else Color.Black
    val context = LocalContext.current
    var isCalendarVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable {
                onClick(date)
                checkAndAddScheduleToFirebase(date, locationData)

                updateViewCountInFirebase(locationData.name) { updatedViewCount ->

                }
                isCalendarVisible = false // Đóng calendarVisible
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = TextStyle(
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
        )
    }
}

private fun updateViewCountInFirebase(locationName: String, onUpdate: (Int) -> Unit) {
    val database = FirebaseDatabase.getInstance().reference
    val query = database.child("destination").orderByChild("name").equalTo(locationName)

    query.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            for (childSnapshot in snapshot.children) {
                val viewCount = childSnapshot.child("view").getValue(Int::class.java) ?: 0
                val updatedViewCount = viewCount + 1
                childSnapshot.ref.child("view").setValue(updatedViewCount)
                Log.d("Firebase", "Updated view count for $locationName to $updatedViewCount")
                onUpdate(updatedViewCount) // Callback to update Composable state
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Failed to update view count: ${error.message}")
        }
    })
}

private fun checkAndAddScheduleToFirebase(date: LocalDate, locationData: LocationData) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser != null) {
        val uid = currentUser.uid
        val database: DatabaseReference = FirebaseDatabase.getInstance().reference

        // Format the date as a string
        val dateString = date.formatAsString()

        // Check if the schedule already exists
        database.child("schedule").child(uid).child(dateString).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Check if any child node contains the same location data
                    var locationExists = false
                    for (childSnapshot in snapshot.children) {
                        val existingLocationName =
                            childSnapshot.child("locationName").getValue(String::class.java)
                        if (existingLocationName == locationData.name) {
                            locationExists = true
                            break
                        }
                    }

                    if (!locationExists) {
                        // If the location doesn't exist for this date, add it
                        addLocationToSchedule(database, uid, dateString, locationData)
                    } else {
                        Log.d(
                            "Firebase",
                            "Location ${locationData.name} already exists for date $dateString"
                        )
                    }
                } else {
                    // If the date doesn't exist in the schedule, add the new location
                    addLocationToSchedule(database, uid, dateString, locationData)
                }
            }.addOnFailureListener { exception ->
                // Handle failure if needed
                Log.e("Firebase", "Failed to check schedule: ${exception.message}")
            }
    }
}

private fun addLocationToSchedule(
    database: DatabaseReference,
    uid: String,
    dateString: String,
    locationData: LocationData
) {
    val scheduleEntry = mapOf(
        "locationName" to locationData.name,
        "locationPlace" to locationData.place
    )
    database.child("schedule").child(uid).child(dateString).push().setValue(scheduleEntry)
    Log.d("Firebase", "Added location ${locationData.name} to schedule for date $dateString")
}

// Helper function to format LocalDate as a string
fun LocalDate.formatAsString(): String {
    return this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

//____________________________________________________
//Schedule

@Composable
fun ScheduleItem(
    schedule: Schedule,
    onUpdatePlan: (String, Int, Int) -> Unit,
    onClearSchedule: () -> Unit,
    modifier: Modifier = Modifier
) {
    var plan by remember { mutableStateOf(schedule.plan) }
    var hour by remember { mutableStateOf(schedule.hour) }
    var minute by remember { mutableStateOf(schedule.minute) }
    var imageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(schedule.destination) {
        val storageRef = FirebaseStorage.getInstance().reference
            .child("location/${schedule.destination}")

        storageRef.listAll().addOnSuccessListener { listResult ->
            if (listResult.items.isNotEmpty()) {
                listResult.items[0].downloadUrl.addOnSuccessListener { uri ->
                    imageUrl = uri.toString()
                }
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberImagePainter(
                data = imageUrl ?: R.drawable.ic_launcher_foreground
            ),
            contentDescription = "Destination Image",
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(18.dp))
                .weight(1f),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .weight(4f)
        ) {
            Text(
                text = "Date: ${schedule.date}",
                style = TextStyle(
                    fontSize = 20.sp,
                    color = Color.Gray
                )
            )
            Text(
                text = schedule.destination,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Location: ${schedule.place}", // Display location
                style = TextStyle(
                    fontSize = 20.sp,
                    color = Color.Gray
                )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = if (hour == 0) "" else hour.toString(),
                    onValueChange = {
                        hour = it.toIntOrNull()?.coerceIn(0, 24) ?: 0
                    },
                    modifier = Modifier
                        .width(50.dp)
                        .padding(vertical = 4.dp),
                    textStyle = TextStyle(
                        fontSize = 20.sp,
                        color = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onUpdatePlan(plan, hour, minute)
                        }
                    )
                )
                Text(
                    text = ":",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                BasicTextField(
                    value = if (minute == 0) "" else minute.toString(),
                    onValueChange = {
                        minute = it.toIntOrNull()?.coerceIn(0, 59) ?: 0
                        if (hour == 24) minute = 0
                        if (hour == 23 && minute > 59) minute = 59
                    },
                    modifier = Modifier
                        .width(50.dp)
                        .padding(vertical = 4.dp),
                    textStyle = TextStyle(
                        fontSize = 20.sp,
                        color = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onUpdatePlan(plan, hour, minute)
                        }
                    )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color = Color.Black)
            )
            BasicTextField(
                value = plan,
                onValueChange = { plan = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                textStyle = TextStyle(
                    fontSize = 20.sp,
                    color = Color.Black
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onUpdatePlan(plan, hour, minute)
                    }
                )
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            IconButton(onClick = { onUpdatePlan(plan, hour, minute) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.Green
                )
            }
            IconButton(onClick = onClearSchedule) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
fun CalendarView_Schedule(
    currentDate: MutableState<LocalDate>,
    modifier: Modifier = Modifier,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val startOfWeek = currentDate.value.minusDays(currentDate.value.dayOfWeek.value.toLong() - 1)
    val endOfWeek = startOfWeek.plusDays(6)
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    currentDate.value = currentDate.value.minusWeeks(2)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous"
                )
            }
            Text(
                text = currentDate.value.month.name.replaceFirstChar { it.uppercase() },
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = currentDate.value.year.toString(),
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = {
                    currentDate.value = currentDate.value.plusWeeks(2)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next"
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DayOfWeek.values().forEach { dayOfWeek ->
                Text(
                    text = dayOfWeek.name.take(3),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            for (week in 0 until 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (day in 0 until 7) {
                        val date = startOfWeek.plusDays((week * 7 + day).toLong())
                        DayCell_Schedule(
                            date = date,
                            isSelected = date == selectedDate,
                            isToday = date == today,
                            onClick = {
                                selectedDate = it
                                onDateSelected(it)
                            },
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell_Schedule(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        isSelected -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        else -> Color.Transparent
    }
    val textColor = if (isToday) MaterialTheme.colorScheme.primary else Color.Black

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick(date) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            fontSize = 18.sp,
            style = TextStyle(
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
        )
    }
}

//____________________________________________________
//Profile

@Composable
fun ProfilePicture(profilePhoto: String, onImageSelected: (Uri) -> Unit) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                imageUri = it
                onImageSelected(it)
            }
        }
    )

    LaunchedEffect(profilePhoto) {
        if (profilePhoto.isNotEmpty()) {
            val storageRef = FirebaseStorage.getInstance().reference.child(profilePhoto)
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                imageUri = uri
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = rememberImagePainter(
                data = imageUri ?: R.drawable.ic_launcher_foreground,
                builder = {
                    crossfade(true)
                    placeholder(R.drawable.ic_launcher_foreground)
                }
            ),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape)
        )
        TextButton(
            onClick = { singlePhotoPickerLauncher.launch("image/*") },
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(
                text = "Avatar",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 20.sp,
            )
        }
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            fontSize = 20.sp,
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            textStyle = TextStyle(fontSize = 20.sp),
            singleLine = true
        )
        Divider(
            modifier = Modifier.padding(top = 8.dp),
            color = Color.LightGray
        )
    }
}

//____________________________________________________
//Find

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(text = "Search Places")
        },
        singleLine = true,
        modifier = modifier,
        shape = RoundedCornerShape(30),
        leadingIcon = {
            IconButton(onClick = onSearch) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
        },
        colors = TextFieldDefaults.textFieldColors(
            cursorColor = Color.Black,
            disabledLabelColor = Color.Black,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}


@Composable
fun LocationItem(location: Location) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            AsyncImage(
                model = location.imageUrl,
                contentDescription = "Image of ${location.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(18.dp)),

                contentScale = ContentScale.Crop
            )
            Text(
                text = location.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = location.place,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Price: ${location.price}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun MessageItem(
    name: String,
    message: String,
    time: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Image(
            painter = painterResource(id = R.drawable.facebook),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = name,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = message,
                style = TextStyle(fontSize = 14.sp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = time,
            style = TextStyle(
                fontSize = 12.sp,
                color = Color.Gray
            )
        )
    }
}

@Composable
fun OTPTextField(
    length: Int,
    onValueChange: (String) -> Unit
) {
    val textFieldValue = remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        repeat(length) { index ->
            OutlinedTextField(
                value = if (index < textFieldValue.value.length) textFieldValue.value[index].toString() else "",
                onValueChange = {
                    if (it.length <= 1) {
                        val newValue = textFieldValue.value.toCharArray().toMutableList()
                        if (index < newValue.size) {
                            newValue[index] = it.firstOrNull() ?: ' '
                        }
                        if (index == newValue.size - 1 && it.isNotEmpty()) {
                            newValue.add(' ')
                        }
                        textFieldValue.value = newValue.joinToString("")
                        onValueChange(textFieldValue.value.replace(" ", ""))
                    }
                },
                modifier = Modifier.width(48.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineLarge
            )
        }
    }
}
