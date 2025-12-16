package com.example.kampus_bildirim

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    //  değişken tanımlama
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private lateinit var notificationList: ArrayList<Notification>
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Kenardan kenara görünüm
        setContentView(R.layout.activity_main)

        // üst bar boşlukları
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Firebase ve Liste Yapısını Başlatma
        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        notificationList = arrayListOf()
        adapter = NotificationAdapter(notificationList)
        recyclerView.adapter = adapter

        //  Veri Çekme
        fetchNotifications()
    }

    private fun fetchNotifications() {
        db.collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->

                if (error != null) {
                    Toast.makeText(this, "Veri çekilemedi: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (value != null) {
                    notificationList.clear() // Listeyi temizle

                    for (doc in value.documents) {
                        // Gelen veriyi çevir
                        val notification = doc.toObject(Notification::class.java)

                        // Eğer notification boş değilse içeri gir
                        notification?.let {
                            // Belge ıdsiyle listeye ekle
                            val notificationWithId = it.copy(id = doc.id)
                            notificationList.add(notificationWithId)
                    }
                    adapter.notifyDataSetChanged() // ekranı yenile
                }
            }
    }
}}