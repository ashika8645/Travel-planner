package com.example.doancs.Screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.dacs.component.SearchBar
import com.example.doancs.navigation.Screen
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Location(
    val name: String = "",
    val description: String = "",
    val place: String = "",
    val price: String = "",
    val view: Int = 0,
    var imageUrl: String = ""
)


class FirebaseRepository {
    private val database = FirebaseDatabase.getInstance().reference
    private val storage = Firebase.storage.reference

    suspend fun searchLocations(query: String): List<Location> {
        val snapshot = database.child("destination")
            .orderByChild("name")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .await()

        return snapshot.children.mapNotNull {
            val location = it.getValue(Location::class.java)
            location?.let {
                // Lấy URL ảnh đầu tiên từ folder có tên trùng với location.name
                val imageUrl = getFirstImageUrl(it.name)
                it.copy(imageUrl = imageUrl)
            }
        }
    }

    private suspend fun getFirstImageUrl(folderName: String): String {
        val imagesRef = storage.child("location/$folderName")
        val result = imagesRef.listAll().await()
        return if (result.items.isNotEmpty()) {
            result.items.first().downloadUrl.await().toString()
        } else {
            "" // Trả về chuỗi rỗng nếu không có ảnh
        }
    }
}

class SearchPlacesViewModel(private val repository: FirebaseRepository) : ViewModel() {
    private val _searchResults = MutableStateFlow<List<Location>>(emptyList())
    val searchResults: StateFlow<List<Location>> = _searchResults.asStateFlow()

    suspend fun searchPlaces(query: String) {
        if (query.isBlank()) {
            // Nếu query rỗng, trả về danh sách rỗng ngay lập tức
            _searchResults.value = emptyList()
            return
        }

        _searchResults.value = repository.searchLocations(query)
    }
}

class SearchPlacesViewModelFactory(private val repository: FirebaseRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchPlacesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchPlacesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPlacesScreen(
    navController: NavHostController,
    viewModel: SearchPlacesViewModel = viewModel(
        factory = SearchPlacesViewModelFactory(FirebaseRepository())
    )
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val searchResults by viewModel.searchResults.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TopAppBar(
                title = {
                    Text(text = "Search")
                },
                actions = {
                    TextButton(onClick = {
                        // Hủy kết quả tìm kiếm và reset trường searchQuery
                        searchQuery = TextFieldValue("")
                        coroutineScope.launch {
                            viewModel.searchPlaces(searchQuery.text)
                        }
                    }) {
                        Text(text = "Cancel")
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            SearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                onSearch = {
                    coroutineScope.launch {
                        viewModel.searchPlaces(searchQuery.text)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Search Results",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(searchResults) { location ->
                    LocationItem(location = location, navController = navController)
                }
            }
        }
    }
}


@Composable
fun LocationItem(location: Location, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clickable {
                navController.navigate(
                    Screen.Details.route + "?name=${location.name}&price=${location.price}&place=${location.place}&view=${location.view}&description=${location.description}"
                )
            },
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



