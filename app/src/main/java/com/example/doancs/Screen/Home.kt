package com.example.doancs.Screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.doancs.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

val lightBlueColor = Color(0xFF2196F3)

data class Destination(
    val name: String = "",
    val view: Int = 0,
    val place: String = "",
    val description: String = "",
    val price: String = ""
)

@Composable
fun HomeScreen(
    onProfileClick: () -> Unit,
    navController: NavHostController
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val usersRef =
        FirebaseDatabase.getInstance().reference.child("users").child(currentUser?.uid ?: "")
    val userData = remember { mutableStateOf(mapOf<String, String>()) }
    val username = remember { mutableStateOf("") }
    val destinationsRef = FirebaseDatabase.getInstance().reference.child("destination")
    val destinationList = remember { mutableStateOf(listOf<Destination>()) }
    val profileImageUrl = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val datauser =
                    snapshot.getValue(object : GenericTypeIndicator<Map<String, String>>() {})
                userData.value = datauser ?: mapOf()
                username.value = datauser?.get("username") ?: ""
                isLoading.value = false
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        destinationsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val datalocation =
                    snapshot.children.mapNotNull { it.getValue(Destination::class.java) }
                destinationList.value = datalocation
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })

        currentUser?.uid?.let { userId ->
            val storageRef = FirebaseStorage.getInstance().reference
                .child("users/$userId/profile_picture.jpg")

            storageRef.downloadUrl.addOnSuccessListener { uri ->
                profileImageUrl.value = uri.toString()
            }.addOnFailureListener { exception ->
                Log.e("FirebaseStorage", "Error getting profile picture URL", exception)
            }
        }
    }

    Surface(
        color = Color.White, modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .clickable { onProfileClick() }
                        .padding(10.dp)
                        .background(color = lightBlueColor, CircleShape),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = rememberImagePainter(
                            data = profileImageUrl.value,
                            builder = {
                                placeholder(R.drawable.ic_launcher_foreground)
                                error(R.drawable.ic_launcher_foreground)
                            }
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Gray, CircleShape)
                    )
                    Text(
                        text = userData.value["username"] ?: "",
                        modifier = Modifier.padding(10.dp),
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    )
                }

                Text(
                    text = "Explore the",
                    style = TextStyle(
                        fontWeight = FontWeight.Light,
                        fontSize = 36.sp
                    )
                )
                Text(
                    text = "Beautiful world!",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Best Destination",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                destinationList.value.forEach { destination ->
                    DestinationCard(
                        name = destination.name,
                        view = destination.view,
                        modifier = Modifier
                            .clickable {
                                navController.navigate(
                                    "details?name=${destination.name}&price=${destination.price}&place=${destination.place}&view=${destination.view}&description=${destination.description}"
                                )
                            }
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
                if (isLoading.value) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}


@Composable
fun DestinationCard(
    name: String,
    view: Int,
    modifier: Modifier = Modifier
) {
    val imageUrl = remember { mutableStateOf("") }
    val storageRef: StorageReference =
        FirebaseStorage.getInstance().reference.child("location").child(name)

    LaunchedEffect(name) {
        storageRef.listAll().addOnSuccessListener { listResult ->
            if (listResult.items.isNotEmpty()) {
                listResult.items[0].downloadUrl.addOnSuccessListener { uri ->
                    imageUrl.value = uri.toString()
                }.addOnFailureListener { exception ->
                    Log.e("FirebaseStorage", "Error getting download URL", exception)
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("FirebaseStorage", "Error listing items", exception)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
    ) {
        Box {
            val painter = rememberImagePainter(
                data = imageUrl.value,
                builder = {
                    placeholder(R.drawable.ic_launcher_foreground)
                    error(R.drawable.ic_launcher_foreground)
                }
            )
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black
                            ),
                            startY = 300f
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Text(
                        text = name,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "★ $view",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }
}
