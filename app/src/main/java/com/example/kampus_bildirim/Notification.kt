package com.example.kampus_bildirim

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Notification(
    val id: String? = null,
    val userId: String? = null, // Bildirimi oluşturan kullanıcının ID'si
    val type: String = "", // Tür (Sağlık, Güvenlik, Çevre vb.)
    val title: String = "",
    val description: String = "",
    val status: String = "Açık",

    // Harita için konum
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    // Fotoğraf ekleme
    val photoUrl: String? = null,

    // Oluşturulma Zamanı
    @ServerTimestamp
    val creationTime: Date? = null
)