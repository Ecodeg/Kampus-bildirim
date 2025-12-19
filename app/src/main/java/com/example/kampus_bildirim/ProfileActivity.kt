package com.example.kampus_bildirim

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Checkbox tanımları
    private lateinit var cbHealth: CheckBox
    private lateinit var cbSecurity: CheckBox
    private lateinit var cbTechnical: CheckBox
    private lateinit var cbEnvironment: CheckBox
    private lateinit var cbOthers: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Bileşenleri XML'deki ID'lere göre bağlama
        cbHealth = findViewById(R.id.cbHealth)
        cbSecurity = findViewById(R.id.cbSecurity)
        cbTechnical = findViewById(R.id.cbTechnical)
        cbEnvironment = findViewById(R.id.cbEnvironment)
        cbOthers = findViewById(R.id.cbOthers)

        // Verileri Firebase'den çekme
        loadUserInfo()

        // Takip edilenleri listeleme
        setupFollowedList()

        // Çıkış Yap Butonu
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            // Çıkış yaptıktan sonra Login sayfasına gider
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Ayarları kaydetme
        val savePrefsListener = CompoundButton.OnCheckedChangeListener { _, _ -> savePreferences() }
        cbHealth.setOnCheckedChangeListener(savePrefsListener)
        cbSecurity.setOnCheckedChangeListener(savePrefsListener)
        cbTechnical.setOnCheckedChangeListener(savePrefsListener)
        cbEnvironment.setOnCheckedChangeListener(savePrefsListener)
        cbOthers.setOnCheckedChangeListener(savePrefsListener)
    }

    private fun loadUserInfo() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                // Firebaseden kullanıcı bilgilerini çeker
                val name = doc.getString("name_surname") ?: doc.getString("fullName") ?: doc.getString("name") ?: "İsimsiz Kullanıcı"
                val unit = doc.getString("unit") ?: doc.getString("birim") ?: "Birim Bilgisi Yok"
                val role = doc.getString("role") ?: "Kullanıcı"

                findViewById<TextView>(R.id.tvProfileName).text = "Ad Soyad: $name"
                findViewById<TextView>(R.id.tvProfileUnit).text = "Birim: $unit"
                findViewById<TextView>(R.id.tvProfileRole).text = "Rol: $role"
                findViewById<TextView>(R.id.tvProfileEmail).text = "E-posta: ${auth.currentUser?.email}"

                // Tercihleri yükle
                cbHealth.isChecked = doc.getBoolean("pref_health") ?: false
                cbSecurity.isChecked = doc.getBoolean("pref_security") ?: false
                cbTechnical.isChecked = doc.getBoolean("pref_technical") ?: false
                cbEnvironment.isChecked = doc.getBoolean("pref_environment") ?: false
                cbOthers.isChecked = doc.getBoolean("pref_others") ?: false
            }
        }
    }

    private fun savePreferences() {
        val userId = auth.currentUser?.uid ?: return
        val prefs = hashMapOf(
            "pref_health" to cbHealth.isChecked,
            "pref_security" to cbSecurity.isChecked,
            "pref_technical" to cbTechnical.isChecked,
            "pref_environment" to cbEnvironment.isChecked,
            "pref_others" to cbOthers.isChecked
        )
        // merge() sayesinde mevcut dökümana sadece bu tercihleri ekler veya günceller
        db.collection("users").document(userId).set(prefs, com.google.firebase.firestore.SetOptions.merge())
    }

    private fun setupFollowedList() {
        val rvFollowed = findViewById<RecyclerView>(R.id.rvFollowedNotifications)
        rvFollowed.layoutManager = LinearLayoutManager(this)

        val userId = auth.currentUser?.uid ?: return

        db.collection("notifications")
            .whereArrayContains("followers", userId)
            .addSnapshotListener { value, error ->
                if (value != null) {
                    val followedList = ArrayList<Notification>()
                    for (doc in value.documents) {
                        val notification = doc.toObject(Notification::class.java)
                        notification?.let {
                            followedList.add(it.copy(id = doc.id))
                        }
                    }
                    rvFollowed.adapter = NotificationAdapter(followedList)
                }
            }
    }
}