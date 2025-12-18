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

        //  Sadece "Açık" olanlar
        findViewById<Button>(R.id.btnOpenOnly).setOnClickListener {
            val filtered = notificationList.filter { it.status == "Açık" }
            adapter.updateList(filtered)
        }

        //  Takip Ettiklerim
        findViewById<Button>(R.id.btnFollowed).setOnClickListener {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val filtered = notificationList.filter { it.userId == currentUserId }
            adapter.updateList(filtered)
        }

        //  Admin Yetki Alanı
        findViewById<Button>(R.id.btnAdminOnly).setOnClickListener {
            filterByType("Teknik") // Adminin türü neyse o filtreyi uygular
        }

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
        db.collection("notifications")
            .orderBy("creationTime", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    // Hata varsa
                    android.util.Log.e("FirestoreVeri", "HATA ALINDI: ${error.message}")
                    return@addSnapshotListener
                }

                if (value != null) {
                    // Veri geldiyse
                    android.util.Log.d("FirestoreVeri", "Firebase'den ${value.size()} adet döküman geldi.")

                    notificationList.clear()
                    for (doc in value.documents) {
                        // Her bir dökümanın ID'sini yazdıralım
                        android.util.Log.d("FirestoreVeri", "Gelen Döküman ID: ${doc.id}")

                        val notification = doc.toObject(Notification::class.java)
                        notification?.let {
                            notificationList.add(it.copy(id = doc.id))
                        }
                    }
                    adapter.updateList(notificationList)
                }
            }
    }
}