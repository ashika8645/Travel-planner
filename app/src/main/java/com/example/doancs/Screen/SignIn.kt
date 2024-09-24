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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun SignInScreen(
    onSignInClick: (username: String) -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lightBlueColor = Color(0xFF2196F3)
    val (username, setUsername) = rememberSaveable { mutableStateOf("") }
    val (password, setPassword) = rememberSaveable { mutableStateOf("") }
    val (isLoading, setIsLoading) = rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    // Function to perform login
    suspend fun login(username: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(username, password).await()
            true
        } catch (e: Exception) {
            Log.e("SignInScreen", "Login failed", e)
            false
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
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(80.dp))
                Text(
                    text = "Sign in now", style = TextStyle(
                        fontSize = 24.sp, fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Please sign in to continue our app")
                Spacer(modifier = Modifier.height(40.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = setUsername,
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
//                Text(text = "Forgot password?",
//                    style = TextStyle(
//                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = lightBlueColor
//                    ),
//                    modifier = Modifier
//                        .align(Alignment.End)
//                        .clickable { onForgotPasswordClick() })
                Button(
                    onClick = {
                        coroutineScope.launch {
                            setIsLoading(true)
                            val isAuthenticated = login(username, password)
                            setIsLoading(false)
                            if (isAuthenticated) {
                                val currentUser = auth.currentUser
                                val displayName = currentUser?.displayName ?: username
                                onSignInClick(displayName)
                            } else {
                                Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(30),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = lightBlueColor, contentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White, modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(text = "Sign in")
                    }
                }
                Spacer(modifier = Modifier.height(36.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Don't have an account?")
                    TextButton(
                        onClick = onSignUpClick, colors = ButtonDefaults.textButtonColors(
                            contentColor = lightBlueColor
                        )
                    ) {
                        Text(text = "Sign up")
                    }
                }
            }
        }
    }
}
