package com.example.kampus_bildirim

data class Notification(
    // her belgenin idsi var,silme/güncelleme için
    val id: String? = null,

    // Tür
    val type: String = "",

    // Başlık
    val title: String = "",

    // Açıklama
    val description: String = "",

    // Durum (Varsayılan olarak açık)
    val status: String = "Açık",

    //  Oluşturulma Zamanı (Long kolaylık)
    val timestamp: Long = System.currentTimeMillis(),

    // Harita için konum
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)