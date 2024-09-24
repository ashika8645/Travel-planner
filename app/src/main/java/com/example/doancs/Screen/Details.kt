package com.example.doancs.Screen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.dacs.component.CalendarView
import com.example.dacs.component.ViewBar
import com.example.doancs.R
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate


data class LocationData(
    val name: String = "",
    val view: Int = 0,
    val place: String = "",
    val description: String = "",
    val price: String = ""
)

@Composable
fun DestinationDetailsScreen(
    onBackClick: () -> Unit, navController: NavHostController, locationData: LocationData
) {
    Log.d("DestinationDetailsScreen", "Name: ${locationData.name}")
    Log.d("DestinationDetailsScreen", "Place: ${locationData.place}")
    Log.d("DestinationDetailsScreen", "Price: ${locationData.price}")
    Log.d("DestinationDetailsScreen", "View: ${locationData.view}")
    Log.d("DestinationDetailsScreen", "Description: ${locationData.description}")
    var isCardVisible by remember { mutableStateOf(false) }
    var isFullyVisible by remember { mutableStateOf(false) }
    var backgroundImageUrl by remember { mutableStateOf("") }
    val imagesList = remember { mutableStateListOf<String>() }

    LaunchedEffect(locationData.name) {
        val storageRef =
            FirebaseStorage.getInstance().reference.child("location/${locationData.name}")

        storageRef.listAll().addOnSuccessListener { listResult ->
            listResult.items.forEach { item ->
                item.downloadUrl.addOnSuccessListener { uri ->
                    imagesList.add(uri.toString())
                    if (backgroundImageUrl.isEmpty()) {
                        backgroundImageUrl = uri.toString()
                    }
                }
            }
        }
    }

    Surface(
        color = Color.White, modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberImagePainter(data = backgroundImageUrl),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                TopBar(
                    onDetailClick = { isCardVisible = !isCardVisible }, onBackClick = onBackClick
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            AnimatedVisibility(
                visible = isCardVisible,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(1000)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(1000))
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    DestinationCard(
                        locationData = locationData,
                        imagesList = imagesList,
                        modifier = Modifier.fillMaxWidth(),
                        onImageClick = { imageUrl ->
                            backgroundImageUrl = imageUrl
                        },
                        onBookNowClick = {
                            isFullyVisible = true
                        },
                        isFullyVisible = isFullyVisible
                    )
                }
            }
        }
    }
}


@Composable
fun TopBar(onDetailClick: () -> Unit, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous",
                modifier = Modifier.size(34.dp)
            )
        }
        Text(
            text = "Details",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.Black,

            )
        IconButton(onClick = onDetailClick) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Detail",
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

@Composable
fun DestinationCard(
    locationData: LocationData,
    imagesList: List<String>,
    modifier: Modifier = Modifier,
    onImageClick: (String) -> Unit,
    onBookNowClick: () -> Unit,
    isFullyVisible: Boolean
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val transitionState = remember { MutableTransitionState(isFullyVisible) }
    var isCalendarVisible by remember { mutableStateOf(false) }
    LaunchedEffect(isFullyVisible) {
        transitionState.targetState = isFullyVisible
    }
    fun closeCalendarView() {
        isCalendarVisible = false
    }
    Card(
        modifier = modifier.background(Color.White)
    ) {
        Column {
            Image(
                painter = rememberImagePainter(
                    data = imagesList.firstOrNull() ?: R.drawable.ic_launcher_foreground
                ),
                contentDescription = "Destination Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = locationData.name,
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                    )
                    Text(
                        text = locationData.place,
                        modifier = Modifier.padding(start = 4.dp),
                        fontSize = 18.sp
                    )
                }
                // Image list
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    imagesList.forEach { imageUrl ->
                        Image(
                            painter = rememberImagePainter(data = imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onImageClick(imageUrl) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ViewBar(
                        rating = locationData.view.toFloat(), modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = locationData.view.toString(),
                        modifier = Modifier.padding(start = 8.dp),
                        fontSize = 18.sp
                    )
                }
                Text(
                    text = "$${locationData.price}/Person",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = locationData.description,
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 16.sp
                )
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    isCalendarVisible = !isCalendarVisible
                }) {
                    Text(text = "Book Now")
                }
                AnimatedVisibility(
                    visible = isCalendarVisible,
                    enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(1000)),
                    exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(1000))
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CalendarView(
                        currentDate = remember { mutableStateOf(selectedDate) },
                        locationData = locationData,
                        onDateSelected = { date ->
                            selectedDate = date
                            closeCalendarView()
                        }
                    )
                }
            }
        }
    }
}

