package com.example.kampus_bildirim

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationDetailActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var notificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_detail)

        db = FirebaseFirestore.getInstance()

        // Verileri al
        notificationId = intent.getStringExtra("notif_id")
        val title = intent.getStringExtra("notif_title")
        val desc = intent.getStringExtra("notif_desc")
        val type = intent.getStringExtra("notif_type")
        val status = intent.getStringExtra("notif_status")
        val time = intent.getStringExtra("notif_time")

        // Ekrana verileri yaz
        findViewById<TextView>(R.id.tvDetayBaslik).text = title
        findViewById<TextView>(R.id.tvDetayAciklama).text = desc
        findViewById<TextView>(R.id.tvDetayTur).text = "Tür: $type"
        findViewById<TextView>(R.id.tvDetayDurum).text = "Durum: $status"
        findViewById<TextView>(R.id.tvDetayZaman).text = time // Zaman bilgisini buraya yazdık

        // Eğer giriş yapan admin ise butonu göster
        checkUserRole()

        // Admin butonuna tıklama işlemi
        findViewById<Button>(R.id.btnUpdateStatus).setOnClickListener {
            updateStatus()
        }
    }

    private fun checkUserRole() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            db.collection("users").document(currentUserId).get().addOnSuccessListener { doc ->
                val role = doc.getString("role")
                if (role == "Admin") {
                    findViewById<Button>(R.id.btnUpdateStatus).visibility = View.VISIBLE
                }
            }
        }
    }

    private fun updateStatus() {
        notificationId?.let { id ->
            val docRef = db.collection("notifications").document(id)
            docRef.get().addOnSuccessListener { doc ->
                val currentStatus = doc.getString("status")

                // Durum
                val nextStatus = when (currentStatus) {
                    "Açık" -> "İnceleniyor"
                    "İnceleniyor" -> "Çözüldü"
                    else -> "Açık"
                }

                docRef.update("status", nextStatus).addOnSuccessListener {
                    findViewById<TextView>(R.id.tvDetayDurum).text = "Durum: $nextStatus"
                    Toast.makeText(this, "Durum $nextStatus olarak güncellendi", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}