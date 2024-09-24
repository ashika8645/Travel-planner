package com.example.doancs.Screen


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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs.component.OTPTextField


@Composable
fun ForgotPasswordScreen(
    onSignInClick: () -> Unit,
) {
    val otpCode = remember { mutableStateOf("") }
    val lightBlueColor = Color(0xFF2196F3)
    val (email, setEmail) = rememberSaveable { mutableStateOf("") }

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
                    text = "Forgot password", style = TextStyle(
                        fontSize = 24.sp, fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Enter your email account to reset", fontWeight = FontWeight.Light)
                Text(text = "your password", fontWeight = FontWeight.Light)
                Spacer(modifier = Modifier.height(30.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = setEmail,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Email") },
                    shape = RoundedCornerShape(30),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Spacer(modifier = Modifier.height(50.dp))
                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(30),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = lightBlueColor, contentColor = Color.White
                    )
                ) {
                    Text(text = "Send")
                }
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "OTP Verification",
                    style = TextStyle(
                        fontSize = 24.sp, fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Please check your email to see the verification",
                    fontWeight = FontWeight.Light
                )
                Spacer(modifier = Modifier.height(20.dp))
                OTPTextField(
                    length = 4,
                    onValueChange = { newValue ->
                        otpCode.value = newValue
                    }
                )
                Spacer(modifier = Modifier.height(50.dp))
                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(30),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = lightBlueColor, contentColor = Color.White
                    )
                ) {
                    Text(text = "Verify")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // TextButton with an onClick action
                    TextButton(
                        onClick = { /* Add action here */ },

                        colors = ButtonDefaults.textButtonColors(
                            contentColor = lightBlueColor, // Set text color to light blue
                        )
                    ) {
                        Text(text = "Resend code") // Text displayed on the button
                    }

                    // Text composable displaying "countdown"
                    Text(

                        text = "number",

                        )
                }

            }
        }
    }
}

