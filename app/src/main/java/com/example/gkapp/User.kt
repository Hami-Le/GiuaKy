package com.example.gkapp

data class User(
    var id: String = "",
    var email: String = "", // Đã đổi từ Username sang email
    var role: String = "",
    var password: String = "",
    var file: String = ""
)