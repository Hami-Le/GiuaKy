package com.example.gkapp

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") } // Đổi username thành email
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
            .imePadding()
    ) {
        AuthHeader(title = "Welcome back!", subtitle = "Đăng nhập bằng Email")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(32.dp))

            AppTextField(
                value = email, // Truyền biến email
                onValueChange = { email = it },
                label = "Địa chỉ Email",
                leadingIcon = {
                    Icon(Icons.Outlined.Email, null, tint = TextHint, modifier = Modifier.size(20.dp)) // Đổi icon
                }
            )

            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = password,
                onValueChange = { password = it },
                label = "Mật khẩu",
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(Icons.Outlined.Lock, null, tint = TextHint, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            null,
                            tint = TextHint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )

            Spacer(Modifier.height(32.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue, strokeWidth = 3.dp)
                }
            } else {
                GradientButton(text = "ĐĂNG NHẬP") {
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show()
                        return@GradientButton
                    }

                    isLoading = true
                    db.collection("users")
                        .whereEqualTo("email", email) // Truy vấn bằng trường "email" trên Firebase
                        .whereEqualTo("password", password)
                        .get()
                        .addOnSuccessListener { documents ->
                            isLoading = false
                            if (!documents.isEmpty) {
                                val doc = documents.documents[0]
                                val role = doc.getString("role") ?: "user"
                                val userId = doc.id

                                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess(role, userId)
                            } else {
                                Toast.makeText(context, "Sai Email hoặc mật khẩu!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            isLoading = false
                            Toast.makeText(context, "Lỗi kết nối máy chủ!", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Chưa có tài khoản?", color = TextSecondary, fontSize = 14.sp)
                TextButton(
                    onClick = onNavigateToRegister,
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Text("Đăng ký ngay", color = PrimaryBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}