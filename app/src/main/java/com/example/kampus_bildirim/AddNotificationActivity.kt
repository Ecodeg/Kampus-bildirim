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
import android.net.Uri
import android.view.View

class AddNotificationActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var selectedImageUri: Uri? = null // Resmin yolu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_notification)

        val spinnerType = findViewById<Spinner>(R.id.spinnerType)
        val etTitle = findViewById<EditText>(R.id.etAddTitle)
        val etDesc = findViewById<EditText>(R.id.etAddDesc)
        val btnGetLoc = findViewById<Button>(R.id.btnGetLocation)
        val tvLocStatus = findViewById<TextView>(R.id.tvLocationStatus)
        val btnSave = findViewById<Button>(R.id.btnSaveNotification)


        val ivPreview = findViewById<ImageView>(R.id.ivPreview)
        val btnSelectPhoto = findViewById<Button>(R.id.btnSelectPhoto)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Spinner'a türleri ekle
        val types = arrayOf("Sağlık", "Güvenlik", "Teknik", "Çevre", "Diğer")
        spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)

        val pickImage = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                ivPreview.setImageURI(uri) // Resmi ekranda göster
                ivPreview.visibility = View.VISIBLE // ImageView'ı görünür yap
            }
        }

        btnSelectPhoto.setOnClickListener {
            pickImage.launch("image/*") // Galeriyi açar
        }

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

            // Form Doğrulaması
            if (title.isEmpty()) {
                etTitle.error = "Başlık boş olamaz"
                return@setOnClickListener
            }
            if (desc.isEmpty()) {
                etDesc.error = "Açıklama boş olamaz"
                return@setOnClickListener
            }
            if (latitude == 0.0) {
                Toast.makeText(this, "Lütfen konum alın!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val newNotification = Notification(
                userId = userId,
                type = type,
                title = title,
                description = desc,
                latitude = latitude,
                longitude = longitude,
                status = "Açık",
                photoUrl = selectedImageUri?.toString()
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