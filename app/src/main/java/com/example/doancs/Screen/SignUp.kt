package com.example.doancs.Screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun SignUpScreen(
    onSignInClick: () -> Unit,
) {
    val auth = FirebaseAuth.getInstance()
    val lightBlueColor = Color(0xFF2196F3)
    val (username, setUsername) = rememberSaveable { mutableStateOf("") }
    val (email, setEmail) = rememberSaveable { mutableStateOf("") }
    val (password, setPassword) = rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    suspend fun register(username: String, email: String, password: String): Boolean {
        return try {
            // Tạo tài khoản với Firebase Authentication
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user

            // Lưu username, location, và phoneNumber vào Realtime Database
            val usersRef = FirebaseDatabase.getInstance().reference.child("users")
            val userRef = usersRef.child(user?.uid ?: "")
            val userData = hashMapOf(
                "username" to username,
                "email" to email,
                "location" to "",
                "phoneNumber" to ""
            )
            userRef.setValue(userData)

            true // Đăng ký thành công
        } catch (e: Exception) {
            Log.e("SignUpScreen", "Registration failed", e)
            false // Đăng ký thất bại
        }
    }

    Surface(
        color = Color.White, modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            IconButton(onClick = { onSignInClick() }, modifier = Modifier.padding(10.dp)) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous"
                )
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(80.dp))
                Text(
                    text = "Sign up now", style = TextStyle(
                        fontSize = 24.sp, fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Please fill the details and create account")

                Spacer(modifier = Modifier.height(40.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = setUsername,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Username") },
                    shape = RoundedCornerShape(30)
                )
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = setEmail,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Email") },
                    shape = RoundedCornerShape(30),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = setPassword,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Password") },
                    shape = RoundedCornerShape(30),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Password must be 8 characters",
                    style = TextStyle(
                        fontSize = 16.sp, fontWeight = FontWeight.Light
                    ),
                    modifier = Modifier
                        .align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val isSuccess = register(username, email, password)
                            if (isSuccess) {
                                Toast.makeText(context, "Sign up successful", Toast.LENGTH_SHORT)
                                    .show()
                                onSignInClick()
                            } else {
                                Toast.makeText(context, "Sign up failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(30),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = lightBlueColor, contentColor = Color.White
                    )
                ) {
                    Text(text = "Sign up")
                }
                Spacer(modifier = Modifier.height(36.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Already have an account?")
                    TextButton(
                        onClick = onSignInClick, colors = ButtonDefaults.textButtonColors(
                            contentColor = lightBlueColor
                        )
                    ) {
                        Text(text = "Sign in")
                    }
                }
            }
        }
    }
}

