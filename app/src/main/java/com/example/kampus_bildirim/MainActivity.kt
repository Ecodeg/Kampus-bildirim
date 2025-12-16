package com.example.kampus_bildirim

import android.os.Bundle
import android.text.Editable
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
        // arama çubuğu
        val etSearch = findViewById<android.widget.EditText>(R.id.etSearch)

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString()) // Her harf değiştiğinde listeyi filtrele
            }
            override fun afterTextChanged(s: Editable) {}

        })
        findViewById<android.widget.Button>(R.id.btnAll).setOnClickListener { filterByType("Hepsi") }
        findViewById<android.widget.Button>(R.id.btnHealth).setOnClickListener { filterByType("Sağlık") }
        findViewById<android.widget.Button>(R.id.btnSecurity).setOnClickListener { filterByType("Güvenlik") }
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