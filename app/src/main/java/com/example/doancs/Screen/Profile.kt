package com.example.doancs.Screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.dacs.component.ProfileField
import com.example.dacs.component.ProfilePicture
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onHomeClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    val lightBlueColor = Color(0xFF2196F3)
    var showAddDestinationDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val usersRef = FirebaseDatabase.getInstance().reference.child("users").child(currentUser?.uid ?: "")
    val initialUserData = remember { mutableStateOf(mapOf<String, String>()) }
    val userData = remember { mutableStateOf(mapOf<String, String>()) }
    val isDataChanged = userData.value != initialUserData.value

    LaunchedEffect(Unit) {
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data =
                    snapshot.getValue(object : GenericTypeIndicator<Map<String, String>>() {})
                initialUserData.value = data ?: mapOf()
                userData.value = data ?: mapOf()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }

    val database = FirebaseDatabase.getInstance()
    val auth = FirebaseAuth.getInstance()
    val storage = FirebaseStorage.getInstance()

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            selectedImages = uris
        }
    )

    fun updateProfilePictureReference(userId: String, storageRef: StorageReference) {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(userId)

        val profilePictureRef = "users/$userId/profile_picture.jpg"

        userRef.child("profilePhoto").setValue(profilePictureRef)
            .addOnSuccessListener {
                // Profile picture reference updated successfully
                userData.value =
                    userData.value.toMutableMap().apply { this["profilePhoto"] = profilePictureRef }
            }
            .addOnFailureListener { e ->
                // Handle any errors
                println("Error updating profile picture reference: ${e.message}")
            }
    }

    fun uploadProfilePicture(imageUri: Uri) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { currentUser ->
            // Create a reference to "users/UID/profile_picture.jpg"
            val storageRef = FirebaseStorage.getInstance().reference
                .child("users/${currentUser.uid}/profile_picture.jpg")

            // Upload file to Firebase Storage
            storageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Image uploaded successfully, update profile photo reference
                    updateProfilePictureReference(currentUser.uid, storageRef)
                }
                .addOnFailureListener { e ->
                    // Handle any errors
                    println("Error uploading profile picture: ${e.message}")
                }
        }
    }

    fun uploadImages(locationKey: String, locationName: String) {
        val imagesRef = storage.reference.child("location/$locationName")

        selectedImages.forEachIndexed { index, uri ->
            val imageRef = imagesRef.child("image$index.jpg")
            val uploadTask = imageRef.putFile(uri)

            uploadTask.addOnSuccessListener {
                // Image uploaded successfully
                if (index == selectedImages.lastIndex) {
                    // All images uploaded, clear input fields and close dialog
                    name = ""
                    place = ""
                    description = ""
                    price = ""
                    selectedImages = emptyList()
                    showAddDestinationDialog = false
                }
            }.addOnFailureListener {
                // Handle failure
            }
        }
    }

    fun addDestination() {
        if (name.isBlank() || place.isBlank() || description.isBlank() || price.isBlank()) {
            // Show error message
            return
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val destinationRef = database.getReference("destination")

            destinationRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nextLocationNumber = snapshot.childrenCount + 1
                    val locationKey = "location $nextLocationNumber"

                    val newLocation = mapOf(
                        "name" to name,
                        "place" to place,
                        "price" to price,
                        "description" to description,
                        "addby" to userId
                    )

                    destinationRef.child(locationKey).setValue(newLocation)
                        .addOnSuccessListener {
                            // Upload images
                            uploadImages(locationKey, name)
                        }
                        .addOnFailureListener {
                            // Handle failure
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle cancelled event
                }
            })
        } else {
            // Handle case when user is not logged in
        }
    }

    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onHomeClick() }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Previous",
                            modifier = Modifier.size(34.dp)
                        )
                    }
                },
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = "Profile", textAlign = TextAlign.Center)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDestinationDialog = !showAddDestinationDialog }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            ProfilePicture(
                profilePhoto = userData.value["profilePhoto"] ?: "",
                onImageSelected = { uri -> uploadProfilePicture(uri) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            ProfileField(
                label = "Username",
                value = userData.value["username"] ?: "",
                onValueChange = { newValue ->
                    userData.value =
                        userData.value.toMutableMap().apply { this["username"] = newValue }
                }
            )
            ProfileField(
                label = "Email",
                value = userData.value["email"] ?: "",
                enabled = false,
                onValueChange = { newValue ->
                    userData.value =
                        userData.value.toMutableMap().apply { this["email"] = newValue }
                }
            )
            ProfileField(
                label = "Location",
                value = userData.value["location"] ?: "",
                onValueChange = { newValue ->
                    userData.value =
                        userData.value.toMutableMap().apply { this["location"] = newValue }
                }
            )
            ProfileField(
                label = "Mobile Number",
                value = userData.value["phoneNumber"] ?: "",
                onValueChange = { newValue ->
                    if (newValue.length <= 10) {
                        userData.value = userData.value.toMutableMap().apply { this["phoneNumber"] = newValue }
                    }
                }
            )
            Button(
                onClick = {
                    if (isDataChanged) {
                        usersRef.setValue(userData.value)
                        initialUserData.value =
                            userData.value
                    }
                },
                enabled = isDataChanged,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDataChanged) lightBlueColor else lightBlueColor
                )
            ) {
                Text(text = "Update Profile")
            }
            Spacer(modifier = Modifier.height(16.dp))  // Add spacing between buttons

            // Logout Button
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    onSignOutClick()
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Log Out")
            }

            if (showAddDestinationDialog) {
                AlertDialog(
                    onDismissRequest = { showAddDestinationDialog = false },
                    title = { Text("Add Destination") },
                    text = {
                        Column {
                            ProfileField(
                                label = "Name",
                                value = name,
                                onValueChange = { name = it })
                            ProfileField(
                                label = "Place",
                                value = place,
                                onValueChange = { place = it })
                            ProfileField(
                                label = "Description",
                                value = description,
                                onValueChange = { description = it }
                            )
                            ProfileField(
                                label = "Price",
                                value = price,
                                onValueChange = { price = it })

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(onClick = { multiplePhotoPickerLauncher.launch("image/*") }) {
                                Text("Select Images")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (selectedImages.isNotEmpty()) {
                                Text("Selected Images:")
                                Row(
                                    modifier = Modifier
                                        .horizontalScroll(rememberScrollState())
                                        .fillMaxWidth()
                                ) {
                                    selectedImages.forEach { uri ->
                                        Image(
                                            painter = rememberImagePainter(uri),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(100.dp)
                                                .padding(4.dp)
                                                .border(2.dp, Color.Gray),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (name.isNotBlank() && place.isNotBlank() && description.isNotBlank() && price.isNotBlank()) {
                                    addDestination()
                                } else {
                                    // Show error message
                                }
                            },
                            enabled = name.isNotBlank() && place.isNotBlank() && description.isNotBlank() && price.isNotBlank() && selectedImages.isNotEmpty()
                        ) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showAddDestinationDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}


