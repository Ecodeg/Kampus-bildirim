package com.example.kampus_bildirim

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminPanelActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter // Admine özel adapter
    private lateinit var notificationList: ArrayList<Notification>//hata aldığım yeri düzeltmek için sınıf tanımlandı

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_panel)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Admin değilse kapat
        checkAdminAccess()

        //  Bileşenlerini Bağla
        recyclerView = findViewById(R.id.rvAdminNotifications)
        recyclerView.layoutManager = LinearLayoutManager(this)
        notificationList = arrayListOf()
        adapter = NotificationAdapter(notificationList)
        recyclerView.adapter = adapter

        val etEmergencyMessage = findViewById<EditText>(R.id.etEmergencyMessage)
        val btnSendEmergency = findViewById<Button>(R.id.btnSendEmergency)

        //Verileri Getir
        fetchAllNotifications()

        // Acil Durum Bildirimi Gönder
        btnSendEmergency.setOnClickListener {
            val message = etEmergencyMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendEmergencyAnnouncement(message)
                etEmergencyMessage.text.clear()
            } else {
                Toast.makeText(this, "Mesaj boş olamaz!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAdminAccess() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            if (doc.getString("role") != "Admin") {
                Toast.makeText(this, "Yetkisiz erişim!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun fetchAllNotifications() {
        // Tüm bildirimleri kronolojik olarak çek
        db.collection("notifications")
            .orderBy("creationTime", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                value?.let {
                    notificationList.clear()
                    for (doc in it.documents) {
                        val notif = doc.toObject(Notification::class.java)
                        notif?.let { n -> notificationList.add(n.copy(id = doc.id)) }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun sendEmergencyAnnouncement(message: String) {
        val announcement = hashMapOf(
            "message" to message,
            "senderId" to auth.currentUser?.uid,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "type" to "ACİL"
        )

        db.collection("emergency_announcements").add(announcement)
            .addOnSuccessListener {
                Toast.makeText(this, "Acil durum duyurusu yayınlandı!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}