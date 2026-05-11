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
fun RegisterScreen(onRegisterSuccess: () -> Unit, onNavigateBack: () -> Unit) {
    var email by remember { mutableStateOf("") } // Đổi username thành email
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier.fillMaxSize().background(SurfaceLight).imePadding()
    ) {
        AuthHeader(title = "Tạo tài khoản mới", subtitle = "Đăng ký bằng Email để bắt đầu")

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalArrangement = Arrangement.Center) {
            Spacer(Modifier.height(32.dp))

            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = "Địa chỉ Email",
                leadingIcon = { Icon(Icons.Outlined.Email, null, tint = TextHint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = password,
                onValueChange = { password = it },
                label = "Mật khẩu",
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = TextHint, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff, null, tint = TextHint, modifier = Modifier.size(20.dp))
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Xác nhận mật khẩu",
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = TextHint, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(if (confirmVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff, null, tint = TextHint, modifier = Modifier.size(20.dp))
                    }
                }
            )

            if (confirmPassword.isNotEmpty()) {
                Row(modifier = Modifier.padding(top = 6.dp, start = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    val matched = password == confirmPassword
                    Icon(imageVector = if (matched) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel, contentDescription = null, tint = if (matched) SuccessGreen else ErrorRed, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(text = if (matched) "Mật khẩu khớp" else "Mật khẩu chưa khớp", fontSize = 12.sp, color = if (matched) SuccessGreen else ErrorRed)
                }
            }

            Spacer(Modifier.height(32.dp))

            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(54.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue, strokeWidth = 3.dp)
                }
            } else {
                GradientButton(text = "TẠO TÀI KHOẢN") {
                    if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                        Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                        return@GradientButton
                    }
                    if (password != confirmPassword) {
                        Toast.makeText(context, "Mật khẩu nhập lại không khớp!", Toast.LENGTH_SHORT).show();
                        return@GradientButton
                    }
                    isLoading = true

                    db.collection("users").whereEqualTo("email", email).get().addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            isLoading = false
                            Toast.makeText(context, "Email này đã được sử dụng!", Toast.LENGTH_SHORT).show()
                        } else {
                            val newUser = hashMapOf(
                                "email" to email, // LƯU VÀO FIREBASE BẰNG TRƯỜNG "email"
                                "password" to password,
                                "role" to "user",
                                "file" to ""
                            )
                            db.collection("users").add(newUser).addOnSuccessListener {
                                isLoading = false;
                                Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                onRegisterSuccess()
                            }.addOnFailureListener { isLoading = false }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("Đã có tài khoản?", color = TextSecondary, fontSize = 14.sp)
                TextButton(onClick = onNavigateBack, contentPadding = PaddingValues(horizontal = 6.dp)) {
                    Text("Đăng nhập", color = PrimaryBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}