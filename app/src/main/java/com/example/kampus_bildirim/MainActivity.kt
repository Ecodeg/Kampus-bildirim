package com.example.kampus_bildirim

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private lateinit var notificationList: ArrayList<Notification>
    private lateinit var db: FirebaseFirestore

    private var adminUnit: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        notificationList = arrayListOf()
        adapter = NotificationAdapter(notificationList)
        recyclerView.adapter = adapter

        fetchNotifications()

        // Harita butonu
        val btnOpenMap = findViewById<android.widget.ImageButton>(R.id.btnOpenMap)
        btnOpenMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        // Profil butonu
        val btnProfile = findViewById<android.widget.ImageButton>(R.id.btnProfile)
        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddNotification).setOnClickListener {
            val intent = Intent(this, AddNotificationActivity::class.java)
            startActivity(intent)
        }

        // arama çubuğu
        val etSearch = findViewById<EditText>(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // filtreleme
        findViewById<Button>(R.id.btnAll).setOnClickListener { adapter.updateList(notificationList) }
        findViewById<Button>(R.id.btnHealth).setOnClickListener { filterByType("Sağlık") }
        findViewById<Button>(R.id.btnSecurity).setOnClickListener { filterByType("Güvenlik") }
        //sonradan eklenen
        findViewById<Button>(R.id.btnEnvironment).setOnClickListener { filterByType("Çevre") }
        findViewById<Button>(R.id.btnTechnical).setOnClickListener { filterByType("Teknik") }

        //  Sadece "Açık" olanlar
        findViewById<Button>(R.id.btnOpenOnly).setOnClickListener {
            val filtered = notificationList.filter { it.status == "Açık" }
            adapter.updateList(filtered)
        }

        //  Takip Ettiklerim

        findViewById<Button>(R.id.btnFollowed).setOnClickListener {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            // Bildirimi oluşturan değil, takipçiler listesinde olanları filtrele
            val filtered = notificationList.filter { it.followers.contains(currentUserId) }

            if (filtered.isEmpty()) {
                Toast.makeText(this, "Henüz takip ettiğiniz bir bildirim yok", Toast.LENGTH_SHORT).show()
            }
            adapter.updateList(filtered)
        }
        //  Admin Yetki Alanı
        findViewById<Button>(R.id.btnAdminOnly).setOnClickListener {
            if (adminUnit != null) {
                val filtered = notificationList.filter { it.unit == adminUnit }

                if (filtered.isEmpty()) {
                    Toast.makeText(this, "$adminUnit birimi için henüz bildirim yok.", Toast.LENGTH_SHORT).show()
                }

                adapter.updateList(filtered)
                Toast.makeText(this, "Kendi biriminiz filtrelendi: $adminUnit", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Birim bilgileriniz yüklenemedi.", Toast.LENGTH_SHORT).show()
            }
        }




        // xmldeki butonu koda bağla
        val btnAdminPanel = findViewById<Button>(R.id.btnAdminPanel)

        //  kulllanıcın rolünü veritabanından çek
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            db.collection("users").document(currentUserId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Veriyi al ve boşlukları temizle
                        val role = document.getString("role")?.trim()
                        // admin birimi al
                        adminUnit = document.getString("unit")?.trim()

                        if (role.equals("Admin", ignoreCase = true)) {
                            android.util.Log.d("RolKontrol", "BAŞARILI: Admin rolü onaylandı.")
                            btnAdminPanel.visibility = android.view.View.VISIBLE //buton görme
                            btnAdminPanel.setOnClickListener {
                                val intent = Intent(this, AdminPanelActivity::class.java)
                                intent.putExtra("USER_ROLE", "Admin") // Rolü  gönder
                                startActivity(intent)
                            }
                        } else {
                            android.util.Log.d("RolKontrol", "HATA: Beklenen: Admin, Gelen: $role")
                            btnAdminPanel.visibility = android.view.View.GONE
                        }
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("RolKontrol", "Hata: ${e.message}")
                    btnAdminPanel.visibility = android.view.View.GONE
                }
                .addOnFailureListener {
                    // Hata durumunda buton gizli
                    btnAdminPanel.visibility = android.view.View.GONE
                }
        }
        // acil durum bildirim
        val sharedPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        db.collection("emergency_announcements")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    android.util.Log.e("AcilDurum", "Firestore Hatası: ${error.message}")
                    return@addSnapshotListener
                }

                if (value != null && !value.isEmpty) {
                    val docs = value.documents.sortedByDescending { it.getTimestamp("timestamp") }
                    val doc = docs[0]
                    val docId = doc.id
                    val lastSeenId = sharedPrefs.getString("last_emergency_id", "")

                    android.util.Log.d("AcilDurum", "En son döküman ID: $docId, Görülen ID: $lastSeenId")

                    if (true) { // lastend yazılabilir
                        val message = doc.getString("message") ?: "Mesaj bulunamadı"
                        runOnUiThread {
                            androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("⚠️ ACİL DURUM DUYURUSU")
                                .setMessage(message)
                                .setCancelable(false)
                                .setPositiveButton("Anladım") { dialog, _ ->
                                    sharedPrefs.edit().putString("last_emergency_id", docId).apply()
                                    dialog.dismiss()
                                }
                                .show()
                        }
                    }
                } else {
                    android.util.Log.d("AcilDurum", "Koleksiyon boş veya veri gelmedi.")
                }
            }
        listenFollowedNotifications()

    }

    // User takip ettiği bir bildirimin durumu değiştiğinde bildirim alır
    private fun listenFollowedNotifications() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Kullanıcının takip ettiği bildirimleri dinle
        db.collection("notifications")
            .whereArrayContains("followers", uid)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener

                value?.documentChanges?.forEach { dc ->
                    // Sadece veri güncellendiğinde çalışacak
                    if (dc.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {
                        val notif = dc.document.toObject(Notification::class.java)
                        val title = notif.title
                        val status = notif.status

                        // Kullanıcıya bilgi ver
                        Toast.makeText(this, "Takip ettiğiniz '$title' bildirimi şu an: $status", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        fetchNotifications() // Ana sayfaya her geri gelindiğinde bildirim tercihlerini kontrol eder
    }

    private fun filterList(query: String) {
        val filteredList = arrayListOf<Notification>()
        for (item in notificationList) {
            if (item.title.lowercase().contains(query.lowercase()) ||
                item.description.lowercase().contains(query.lowercase())) {
                filteredList.add(item)
            }
        }
        adapter.updateList(filteredList)
    }

    private fun filterByType(type: String) {
        if (type == "Hepsi") {
            adapter.updateList(notificationList)
        } else {
            val filteredList = notificationList.filter { it.type == type }
            adapter.updateList(filteredList)
        }
    }
    private fun fetchNotifications() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").document(userId).addSnapshotListener { userDoc, userError ->

            // Eğer kullanıcı dökümanı boşsa true  kabul et
            val prefHealth = if (userDoc?.contains("pref_health") == true) userDoc.getBoolean("pref_health") ?: true else true
            val prefSecurity = if (userDoc?.contains("pref_security") == true) userDoc.getBoolean("pref_security") ?: true else true
            val prefTechnical = if (userDoc?.contains("pref_technical") == true) userDoc.getBoolean("pref_technical") ?: true else true
            val prefEnv = if (userDoc?.contains("pref_environment") == true) userDoc.getBoolean("pref_environment") ?: true else true
            val prefOthers = if (userDoc?.contains("pref_others") == true) userDoc.getBoolean("pref_others") ?: true else true

            db.collection("notifications")
                .orderBy("creationTime", Query.Direction.DESCENDING)
                .addSnapshotListener { value, error ->
                    // hata kontrolü
                    if (value != null) {
                        notificationList.clear()
                        for (doc in value.documents) {
                            val type = doc.getString("type")

                            // Filtreleme
                            val isVisible = when (type) {
                                "Sağlık" -> prefHealth
                                "Güvenlik" -> prefSecurity
                                "Teknik" -> prefTechnical
                                "Çevre" -> prefEnv
                                "Diğer" -> prefOthers
                                else -> true // Tipi belirsiz olanları her zaman göster
                            }

                            if (isVisible) {
                                val notification = doc.toObject(Notification::class.java)
                                notification?.let {
                                    notificationList.add(it.copy(id = doc.id))
                                }
                            }
                        }
                        adapter.updateList(notificationList)
                    }
                }
        }
    }
}