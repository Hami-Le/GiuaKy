package com.example.gkapp

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(userId: String, onLogout: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var currentUser by remember { mutableStateOf<User?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var inputPassword by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var currentImageUrl by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) imageUri = uri
    }

    // Lắng nghe dữ liệu thực tế của User này từ Firebase
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val user = User(
                    id = snapshot.id,
                    email = snapshot.getString("email") ?: "", // Lấy trường email thay vì Username
                    password = snapshot.getString("password") ?: "",
                    role = snapshot.getString("role") ?: "",
                    file = snapshot.getString("file") ?: ""
                )
                currentUser = user
                if (!isEditing) {
                    inputPassword = user.password
                    currentImageUrl = user.file
                }
            }
        }
    }

    Scaffold(
        containerColor = SurfaceLight,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(GradientStart, GradientEnd)))
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Outlined.Person, null, tint = Color.White, modifier = Modifier.size(24.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Trang Cá Nhân", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        // Hiển thị Email thay cho Username
                        Text(if (isEditing) "Đang chỉnh sửa..." else "Xin chào, ${currentUser?.email ?: ""}", fontSize = 13.sp, color = Color.White.copy(alpha = 0.75f))
                    }
                    OutlinedButton(
                        onClick = onLogout,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        // Sửa lỗi cảnh báo icon Logout ở bản cũ
                        Icon(Icons.AutoMirrored.Outlined.Logout, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Đăng xuất", fontSize = 13.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        if (currentUser == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // AVATAR
            Box(contentAlignment = Alignment.BottomEnd) {
                val fileToDisplay = if (imageUri != null) imageUri.toString() else currentImageUrl
                val decoded = remember(fileToDisplay) {
                    if (fileToDisplay.startsWith("base64,")) {
                        try { val bytes = android.util.Base64.decode(fileToDisplay.removePrefix("base64,"), android.util.Base64.DEFAULT); android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size) } catch (e: Exception) { null }
                    } else null
                }

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(GradientStart.copy(alpha = 0.2f), GradientEnd.copy(alpha = 0.2f)))),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        decoded != null -> Image(bitmap = decoded.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                        imageUri != null -> AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                        fileToDisplay.isNotEmpty() && !fileToDisplay.startsWith("base64,") -> AsyncImage(model = "https://ui-avatars.com/api/?name=${currentUser!!.email}&background=random", contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                        // Lấy chữ cái đầu của email làm avatar mặc định
                        else -> Text(text = currentUser!!.email.firstOrNull()?.uppercaseChar()?.toString() ?: "?", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }
                }

                if (isEditing) {
                    IconButton(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue)
                            .border(2.dp, Color.White, CircleShape)
                    ) {
                        Icon(Icons.Outlined.Edit, "Đổi ảnh", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // EMAIL - Không được phép sửa
            AppTextField(
                value = currentUser!!.email, // Hiển thị email
                onValueChange = {},
                label = "Địa chỉ Email (Không thể đổi)",
                readOnly = true,
                leadingIcon = { Icon(Icons.Outlined.Email, null, tint = TextHint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(Modifier.height(16.dp))

            // ROLE - Không được phép sửa
            AppTextField(
                value = currentUser!!.role.replaceFirstChar { it.uppercase() },
                onValueChange = {},
                label = "Quyền truy cập",
                readOnly = true,
                leadingIcon = { Icon(Icons.Outlined.Badge, null, tint = TextHint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(Modifier.height(16.dp))

            // PASSWORD - Chỉ được sửa khi bật chế độ isEditing
            AppTextField(
                value = if (isEditing) inputPassword else "********",
                onValueChange = { if(isEditing) inputPassword = it },
                label = "Mật khẩu",
                readOnly = !isEditing,
                visualTransformation = if (isEditing) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = TextHint, modifier = Modifier.size(20.dp)) }
            )

            Spacer(Modifier.height(32.dp))

            // NÚT ĐIỀU KHIỂN
            if (isLoading) {
                CircularProgressIndicator(color = PrimaryBlue)
            } else {
                if (isEditing) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = {
                                isEditing = false
                                imageUri = null
                                inputPassword = currentUser!!.password
                            },
                            modifier = Modifier.weight(1f).height(54.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Hủy", fontWeight = FontWeight.Bold)
                        }

                        GradientButton(
                            text = "LƯU",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (inputPassword.isEmpty()) {
                                    Toast.makeText(context, "Mật khẩu không được để trống!", Toast.LENGTH_SHORT).show()
                                    return@GradientButton
                                }
                                isLoading = true
                                fun saveToDatabase(fileData: String) {
                                    db.collection("users").document(userId)
                                        .update(
                                            "password", inputPassword,
                                            "file", fileData
                                        )
                                        .addOnSuccessListener {
                                            isLoading = false
                                            isEditing = false
                                            Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            isLoading = false
                                            Toast.makeText(context, "Lỗi khi cập nhật!", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                if (imageUri != null) saveToDatabase(getBase64FromUri(imageUri!!, context))
                                else saveToDatabase(currentImageUrl)
                            }
                        )
                    }
                } else {
                    GradientButton(
                        text = "CHỈNH SỬA THÔNG TIN",
                        onClick = { isEditing = true }
                    )
                }
            }
        }
    }
}