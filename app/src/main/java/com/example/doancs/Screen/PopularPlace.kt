import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.doancs.navigation.Screen
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

data class Location(
    val name: String = "",
    val place: String = "",
    val price: String = "",
    val view: Int = 0,
    val description: String = "",
    var imageUrl: String = ""
)

@Composable
fun PopularPlacesScreen(
    navController: NavHostController,
    onBackClick: () -> Unit,
) {
    var locations by remember { mutableStateOf<List<Location>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val databaseRef = FirebaseDatabase.getInstance().getReference("destination")
            val query = databaseRef.orderByChild("view").limitToLast(5)

            val dataSnapshot = query.get().await()
            val topLocations = mutableListOf<Location>()
            for (locationSnapshot in dataSnapshot.children.reversed()) {
                val location = locationSnapshot.getValue(Location::class.java)
                location?.let {
                    it.imageUrl = getFirstImageUrlFromFolder(it.name)
                    topLocations.add(it)
                }
            }
            locations = topLocations
            isLoading = false
        } catch (e: Exception) {
            error = "Error: ${e.message}"
            isLoading = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onBackClick() }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text(
                    text = "Popular Places", fontWeight = FontWeight.Bold, fontSize = 30.sp
                )
                IconButton(onClick = { /* Handle star action */ }) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Favorite",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            when {
                isLoading -> LoadingScreen()
                error != null -> ErrorScreen(error!!)
                else -> LocationList(locations, navController)
            }
        }
    }
}

suspend fun getFirstImageUrlFromFolder(locationName: String): String {
    val storageRef = FirebaseStorage.getInstance().reference.child("location").child(locationName)
    return try {
        val listResult = storageRef.listAll().await()
        if (listResult.items.isNotEmpty()) {
            listResult.items[0].downloadUrl.await().toString()
        } else {
            ""
        }
    } catch (e: Exception) {
        Log.e("FirebaseStorage", "Error getting image for $locationName", e)
        ""
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(error: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = error, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun LocationList(locations: List<Location>, navController: NavHostController) {
    // Sort the locations by the 'view' value in ascending order
    val sortedLocations = locations.sortedBy { it.view }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(sortedLocations.reversed()) { location ->
            LocationCard(location = location, navController = navController)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun LocationCard(location: Location, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(
                    Screen.Details.route + "?name=${location.name}" + "&price=${location.price}" + "&place=${location.place}" + "&view=${location.view}" + "&description=${location.description}"
                )
            }, elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            AsyncImage(
                model = location.imageUrl,
                contentDescription = "Image of ${location.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = location.name, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = location.place, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Price: ${location.price}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Views: ${location.view}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}