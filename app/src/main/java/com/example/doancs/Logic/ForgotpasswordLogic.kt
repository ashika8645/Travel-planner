package com.example.doancs.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    val email = mutableStateOf("")
    val otpCode = mutableStateOf("")
    val newPassword = mutableStateOf("")
    val confirmPassword = mutableStateOf("")
    val timeLeft = mutableStateOf(120)
    private var countDownJob: Job? = null

    fun sendVerificationCode(context: Context) {
        auth.sendPasswordResetEmail(email.value)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Verification code sent to your email", Toast.LENGTH_SHORT).show()
                    startCountDown()
                } else {
                    Toast.makeText(context, "Failed to send verification code", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun verifyOTPAndResetPassword(context: Context, onResetPasswordSuccess: () -> Unit) {
        val code = otpCode.value
        if (code.isNotEmpty()) {
            auth.verifyPasswordResetCode(code)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val newPassword = newPassword.value
                        val confirmPassword = confirmPassword.value
                        if (newPassword == confirmPassword) {
                            auth.confirmPasswordReset(code, newPassword)
                                .addOnCompleteListener { resetTask ->
                                    if (resetTask.isSuccessful) {
                                        Toast.makeText(context, "Password reset successful", Toast.LENGTH_SHORT).show()
                                        onResetPasswordSuccess()
                                    } else {
                                        Toast.makeText(context, "Failed to reset password", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(context, "New passwords do not match", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Invalid verification code", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(context, "Please enter the verification code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCountDown() {
        countDownJob?.cancel()
        countDownJob = viewModelScope.launch {
            while (timeLeft.value > 0) {
                delay(1000)
                timeLeft.value--
            }
        }
    }
}