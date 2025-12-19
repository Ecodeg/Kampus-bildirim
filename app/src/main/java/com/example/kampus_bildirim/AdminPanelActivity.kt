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

        val roleFromIntent = intent.getStringExtra("USER_ROLE")
        if (roleFromIntent == "Admin") {
            // Direkt aç, kontrole gerek yok
        } else {
            checkAdminAccess() // Emin olmak için yine de kontrol et
        }


        //  Bileşenlerini Bağla
        recyclerView = findViewById(R.id.rvAdminNotifications)
        recyclerView.layoutManager = LinearLayoutManager(this)
        notificationList = arrayListOf()
        adapter = NotificationAdapter(notificationList)
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener { notification ->
            // Durum Döngüsü: Açık -> İnceleniyor -> Çözüldü -> Açık
            val yeniDurum = when (notification.status) {
                "Açık" -> "İnceleniyor"
                "İnceleniyor" -> "Çözüldü"
                else -> "Açık"
            }

            // veritabanıyla ilgili dökümanı güncelle
            notification.id?.let { docId ->
                db.collection("notifications").document(docId)
                    .update("status", yeniDurum)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Durum '$yeniDurum' olarak güncellendi", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

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
            val role = doc.getString("role")?.trim()


            if (!role.equals("Admin", ignoreCase = true)) {
                Toast.makeText(this, "Yetkisiz erişim!", Toast.LENGTH_SHORT).show()
                finish() // admin değilse  kapat
            } else {
                android.util.Log.d("AdminPanel", "Erişim onaylandı, hoş geldiniz Admin.")
            }
        }.addOnFailureListener {
            // herhangi bir hatada kullanıcıyı uyarabilir
            android.util.Log.e("AdminPanel", "Rol doğrulama hatası.")
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
                Toast.makeText(this, "Acil durum duyurusu tüm kampüse yayınlandı!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}