package com.example.gkapp

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var userList by remember { mutableStateOf(listOf<User>()) }

    var showDialog by remember { mutableStateOf(false) }
    var dialogMode by remember { mutableStateOf("add") }
    var editingUserId by remember { mutableStateOf("") }

    // Đổi biến inputUsername thành inputEmail
    var inputEmail by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }
    var inputRole by remember { mutableStateOf("user") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var currentImageUrl by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> imageUri = uri }

    LaunchedEffect(Unit) {
        db.collection("users").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            userList = snapshot.documents.map { doc ->
                User(
                    id = doc.id,
                    email = doc.getString("email") ?: "", // Đọc bằng trường email
                    password = doc.getString("password") ?: "",
                    role = doc.getString("role") ?: "",
                    file = doc.getString("file") ?: ""
                )
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
                    ) { Icon(Icons.Outlined.AdminPanelSettings, null, tint = Color.White, modifier = Modifier.size(24.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Quản lý Admin", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${userList.size} tài khoản", fontSize = 13.sp, color = Color.White.copy(alpha = 0.75f))
                    }
                    OutlinedButton(
                        onClick = onLogout,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.Logout, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Đăng xuất", fontSize = 13.sp)
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { dialogMode = "add"; inputEmail = ""; inputPassword = ""; inputRole = "user"; imageUri = null; currentImageUrl = ""; showDialog = true },
                containerColor = PrimaryBlue, contentColor = Color.White, shape = RoundedCornerShape(16.dp), modifier = Modifier.size(60.dp).shadow(12.dp, RoundedCornerShape(16.dp))
            ) { Icon(Icons.Filled.Add, "Thêm", modifier = Modifier.size(28.dp)) }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(userList, key = { it.id }) { user ->
                AnimatedVisibility(visible = true, enter = slideInVertically(tween(300)) + fadeIn(tween(300))) {
                    UserItemCard(
                        user = user,
                        onEditClick = { dialogMode = "edit"; editingUserId = user.id; inputEmail = user.email; inputPassword = user.password; inputRole = user.role; currentImageUrl = user.file; imageUri = null; showDialog = true },
                        onDeleteClick = {
                            if (user.email == "admin@gmail.com") { Toast.makeText(context, "Không thể xóa admin gốc!", Toast.LENGTH_SHORT).show(); return@UserItemCard }
                            db.collection("users").document(user.id).delete().addOnSuccessListener { Toast.makeText(context, "Đã xóa ${user.email}", Toast.LENGTH_SHORT).show() }
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AdminDialog(
            dialogMode = dialogMode, inputEmail = inputEmail, inputPassword = inputPassword, inputRole = inputRole, imageUri = imageUri, currentImageUrl = currentImageUrl,
            onEmailChange = { inputEmail = it }, onPasswordChange = { inputPassword = it }, onRoleChange = { inputRole = it },
            onPickImage = { launcher.launch("image/*") }, onDismiss = { showDialog = false },
            onConfirm = {
                if (inputEmail.isEmpty() || inputPassword.isEmpty() || inputRole.isEmpty()) { Toast.makeText(context, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show(); return@AdminDialog }
                Toast.makeText(context, "Đang lưu...", Toast.LENGTH_SHORT).show()

                fun saveToDatabase(fileData: String) {
                    val userData = hashMapOf("email" to inputEmail, "password" to inputPassword, "role" to inputRole, "file" to fileData)
                    if (dialogMode == "add") db.collection("users").add(userData).addOnSuccessListener { showDialog = false }
                    else db.collection("users").document(editingUserId).set(userData).addOnSuccessListener { showDialog = false }
                }

                if (imageUri != null) saveToDatabase(getBase64FromUri(imageUri!!, context)) else saveToDatabase(currentImageUrl)
            }
        )
    }
}

@Composable
fun AdminDialog(
    dialogMode: String, inputEmail: String, inputPassword: String, inputRole: String, imageUri: Uri?, currentImageUrl: String,
    onEmailChange: (String) -> Unit, onPasswordChange: (String) -> Unit, onRoleChange: (String) -> Unit, onPickImage: () -> Unit, onDismiss: () -> Unit, onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = SurfaceCard, shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(PrimaryBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(if (dialogMode == "add") Icons.Outlined.PersonAdd else Icons.Outlined.Edit, null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(if (dialogMode == "add") "Thêm tài khoản" else "Chỉnh sửa tài khoản", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Đổi label và icon sang Email
                AppTextField(value = inputEmail, onValueChange = onEmailChange, label = "Địa chỉ Email", leadingIcon = { Icon(Icons.Outlined.Email, null, tint = TextHint, modifier = Modifier.size(18.dp)) })
                AppTextField(value = inputPassword, onValueChange = onPasswordChange, label = "Mật khẩu", visualTransformation = PasswordVisualTransformation(), leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = TextHint, modifier = Modifier.size(18.dp)) })
                Column {
                    Text("Quyền truy cập", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("user", "admin").forEach { roleOption ->
                            val selected = inputRole == roleOption
                            FilterChip(
                                selected = selected, onClick = { onRoleChange(roleOption) },
                                label = { Text(roleOption.replaceFirstChar { it.uppercase() }, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryBlue, selectedLabelColor = Color.White, containerColor = SurfaceLight, labelColor = TextSecondary),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
                OutlinedButton(
                    onClick = onPickImage, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryBlue.copy(alpha = 0.4f)), modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(Icons.Outlined.Image, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (imageUri != null || currentImageUrl.isNotEmpty()) "Đã chọn ảnh · Bấm để đổi" else "Chọn ảnh đại diện", fontSize = 14.sp)
                }
            }
        },
        confirmButton = { Button(onClick = onConfirm, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) { Text("Lưu", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy", color = TextSecondary) } }
    )
}

@Composable
fun UserItemCard(user: User, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = SurfaceCard), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val decodedBitmap = remember(user.file) {
                if (user.file.startsWith("base64,")) {
                    try { val bytes = android.util.Base64.decode(user.file.removePrefix("base64,"), android.util.Base64.DEFAULT); android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size) } catch (e: Exception) { null }
                } else null
            }
            Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(Brush.linearGradient(listOf(GradientStart.copy(alpha = 0.2f), GradientEnd.copy(alpha = 0.2f)))), contentAlignment = Alignment.Center) {
                when {
                    decodedBitmap != null -> Image(bitmap = decodedBitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                    user.file.isNotEmpty() && !user.file.startsWith("base64,") -> AsyncImage(model = "https://ui-avatars.com/api/?name=${user.email}&background=random", contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                    else -> Text(text = user.email.firstOrNull()?.uppercaseChar()?.toString() ?: "?", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.email, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary) // Thay Username bằng email
                Spacer(Modifier.height(4.dp))
                val (badgeBg, badgeFg) = if (user.role == "admin") BadgeAdmin to BadgeAdminText else BadgeUser to BadgeUserText
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(badgeBg).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text(user.role.replaceFirstChar { it.uppercase() }, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = badgeFg)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(PrimaryBlue.copy(alpha = 0.08f))) { Icon(Icons.Filled.Edit, "Sửa", tint = PrimaryBlue, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(ErrorRed.copy(alpha = 0.08f))) { Icon(Icons.Filled.Delete, "Xóa", tint = ErrorRed, modifier = Modifier.size(18.dp)) }
            }
        }
    }
}