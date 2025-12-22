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
    private var selectedImageUri: Uri? = null

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

        // Spinner ayarları
        val types = arrayOf("Sağlık", "Güvenlik", "Teknik", "Çevre", "Diğer")
        spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)

        // Fotoğraf seçme
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

        // güncellenen kaydet butonu
        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val type = spinnerType.selectedItem.toString()
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Form kontrolü
            if (title.isEmpty()) { etTitle.error = "Başlık boş olamaz"; return@setOnClickListener }
            if (desc.isEmpty()) { etDesc.error = "Açıklama boş olamaz"; return@setOnClickListener }
            if (latitude == 0.0) {
                Toast.makeText(this, "Lütfen konum alın!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Görseli base 64'e dönüştürme
            val base64Photo = selectedImageUri?.let { encodeImageToBase64(it) }

            // kullanıcı bildirimi çek sonra bildirimi kaydet
            if (userId != null) {
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { userDoc ->
                        // kullanıcıdan unit i al
                        val userUnit = userDoc.getString("unit") ?: "Bilinmiyor"

                        // bildirimi unit ile doldur
                        val newNotification = Notification(
                            userId = userId,
                            unit = userUnit, // Notificationa eklediğim alan
                            type = type,
                            title = title,
                            description = desc,
                            latitude = latitude,
                            longitude = longitude,
                            status = "Açık",
                            photoUrl = base64Photo
                        )

                        saveToFirestore(newNotification)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Kullanıcı bilgisi alınamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
    // Görseli firebase için uygun hale getirme
    private fun encodeImageToBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            var bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)

            // Boyut küçültme
            val maxSize = 800
            val ratio = Math.min(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
            val width = Math.round(ratio * bitmap.width)
            val height = Math.round(ratio * bitmap.height)
            bitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, width, height, true)

            // Çok büyük olunca hata veriyor bildirimi yüklemiyor, bu yüzden; daha da küçültüyoruz
            val outputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 20, outputStream)

            val byteArray = outputStream.toByteArray()
            android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            null
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