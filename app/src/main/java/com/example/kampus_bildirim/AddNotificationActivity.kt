package com.example.kampus_bildirim

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddNotificationActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_notification)

        val spinnerType = findViewById<Spinner>(R.id.spinnerType)
        val etTitle = findViewById<EditText>(R.id.etAddTitle)
        val etDesc = findViewById<EditText>(R.id.etAddDesc)
        val btnGetLoc = findViewById<Button>(R.id.btnGetLocation)
        val tvLocStatus = findViewById<TextView>(R.id.tvLocationStatus)
        val btnSave = findViewById<Button>(R.id.btnSaveNotification)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Spinner'a türleri ekle
        val types = arrayOf("Sağlık", "Güvenlik", "Teknik", "Çevre","Diğer")
        spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)

        // Konum butonu
        btnGetLoc.setOnClickListener {
            getLocation(tvLocStatus)
        }

        // Kaydet butonu
        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val type = spinnerType.selectedItem.toString()
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (title.isEmpty() || desc.isEmpty() || latitude == 0.0) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun ve konum alın!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val newNotification = Notification(
                userId = userId,
                type = type,
                title = title,
                description = desc,
                latitude = latitude,
                longitude = longitude,
                status = "Açık"
            )

            saveToFirestore(newNotification)
        }
    }

    private fun getLocation(tvStatus: TextView) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                tvStatus.text = "Konum Alındı: $latitude, $longitude"
            } else {
                Toast.makeText(this, "Konum okunamadı, GPS açık mı?", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveToFirestore(notification: Notification) {
        val db = FirebaseFirestore.getInstance()
        db.collection("notifications").add(notification)
            .addOnSuccessListener {
                Toast.makeText(this, "Bildirim başarıyla oluşturuldu!", Toast.LENGTH_LONG).show()
                finish() // Sayfayı kapat
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}