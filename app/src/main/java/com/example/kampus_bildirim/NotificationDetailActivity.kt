package com.example.kampus_bildirim

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// Harita kütüphaneleri
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

import android.widget.ImageView

class NotificationDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var notificationId: String? = null
    //yeni eklenen bileşenler
    private lateinit var mMap: GoogleMap
    private lateinit var btnFollow: Button // Yeni buton
    private var isFollowing = false // Takip durumu kontrolü

    // yeni eklenen butonlar
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_detail)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        //  butonu xml ıdsine göre bağla
        btnFollow = findViewById(R.id.btnFollow)

        btnEdit = findViewById(R.id.btnEditNotification)
        btnDelete = findViewById(R.id.btnDeleteNotification)

        // Verileri al
        notificationId = intent.getStringExtra("notif_id")
        val title = intent.getStringExtra("notif_title")
        val desc = intent.getStringExtra("notif_desc")
        val type = intent.getStringExtra("notif_type")
        val status = intent.getStringExtra("notif_status")
        val time = intent.getStringExtra("notif_time")


        // Ekrana verileri yaz
        findViewById<TextView>(R.id.tvDetayBaslik).text = title
        findViewById<TextView>(R.id.tvDetayAciklama).text = desc
        findViewById<TextView>(R.id.tvDetayTur).text = "Tür: $type"
        findViewById<TextView>(R.id.tvDetayDurum).text = "Durum: $status"
        findViewById<TextView>(R.id.tvDetayZaman).text = time // Zaman bilgisini buraya yazdık

        // Base 64'den görsele dönüştürme
        val photoBase64 = intent.getStringExtra("notif_photoUrl")
        val ivDetayFoto = findViewById<ImageView>(R.id.ivDetayFoto)

        if (!photoBase64.isNullOrEmpty() && photoBase64 != "null") {
            ivDetayFoto.visibility = View.VISIBLE
            try {
                val imageBytes = android.util.Base64.decode(photoBase64, android.util.Base64.DEFAULT)
                val decodedImage = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ivDetayFoto.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                ivDetayFoto.visibility = View.GONE
            }
        } else {
            ivDetayFoto.visibility = View.GONE
        }

        // Mini Haritayı Başlat
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mini_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Eğer giriş yapan admin ise butonu göster
        checkUserRole()

        // Mevcut takip durumunu kontrol et
        checkFollowStatus()

        // Admin butonuna tıklama işlemi
        findViewById<Button>(R.id.btnUpdateStatus).setOnClickListener {
            updateStatus()
        }
        btnFollow.setOnClickListener {
            toggleFollow()
        }
    }

    // Harita Hazır Olduğunda Çalışır
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // gelen konum bilgilerini al boşsa kampüs
        val lat = intent.getDoubleExtra("notif_lat", 39.9048)
        val lng = intent.getDoubleExtra("notif_lng", 41.2678)
        val location = LatLng(lat, lng)

        // İşaretçi ekle ve odakla
        mMap.addMarker(MarkerOptions().position(location).title("Olay Konumu"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

        // Mini harita olduğu için kaydırmayı kapatt
        mMap.uiSettings.isScrollGesturesEnabled = false
    }

    private fun checkUserRole() {
        val currentUserId = auth.currentUser?.uid ?: return
        db.collection("users").document(currentUserId).get().addOnSuccessListener { doc ->
            val role = doc.getString("role")?.trim()

            if (role.equals("Admin", ignoreCase = true)) {
                // Butonları görünür yapıyoruz
                findViewById<Button>(R.id.btnUpdateStatus).visibility = View.VISIBLE
                btnEdit.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE

                // Tıklama olaylarını tanımlıyoruz
                btnDelete.setOnClickListener {
                    deleteNotification()
                }
                btnEdit.setOnClickListener {
                    editNotification()
                }
            }
        }
    }

    private fun checkFollowStatus() {
        val currentUserId = auth.currentUser?.uid ?: return
        notificationId?.let { id ->
            db.collection("notifications").document(id).get().addOnSuccessListener { doc ->
                val followersList = doc.get("followers") as? List<String> ?: listOf()
                if (followersList.contains(currentUserId)) {
                    isFollowing = true
                    btnFollow.text = "Takibi Bırak"
                } else {
                    isFollowing = false
                    btnFollow.text = "Bildirimi Takip Et"
                }
            }
        }
    }

    private fun toggleFollow() {
        val currentUserId = auth.currentUser?.uid ?: return
        val docRef = notificationId?.let { db.collection("notifications").document(it) } ?: return

        if (!isFollowing) {
            // Takip  ekle
            docRef.update("followers", FieldValue.arrayUnion(currentUserId)).addOnSuccessListener {
                isFollowing = true
                btnFollow.text = "Takibi Bırak"
                Toast.makeText(this, "Takip listesine eklendi", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Log.e("HATA", "Takip hatası: ${e.message}")
            }
        } else {
            // Takip çıkar
            docRef.update("followers", FieldValue.arrayRemove(currentUserId)).addOnSuccessListener {
                isFollowing = false
                btnFollow.text = "Bildirimi Takip Et"
                Toast.makeText(this, "Takip listesinden çıkarıldı", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStatus() {
        notificationId?.let { id ->
            val docRef = db.collection("notifications").document(id)
            docRef.get().addOnSuccessListener { doc ->
                val currentStatus = doc.getString("status")

                // Durum
                val nextStatus = when (currentStatus) {
                    "Açık" -> "İnceleniyor"
                    "İnceleniyor" -> "Çözüldü"
                    else -> "Açık"
                }
                docRef.update("status", nextStatus).addOnSuccessListener {
                    findViewById<TextView>(R.id.tvDetayDurum).text = "Durum: $nextStatus"
                    Toast.makeText(this, "Durum $nextStatus olarak güncellendi", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun deleteNotification() {
        notificationId?.let { id ->
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Bildirimi Sonlandır")
                .setMessage("Bu bildirimi uygunsuz olduğu gerekçesiyle silmek istediğinize emin misiniz?")
                .setPositiveButton("Evet, Sil") { _, _ ->
                    db.collection("notifications").document(id).delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Bildirim sistemden kaldırıldı.", Toast.LENGTH_SHORT).show()
                            finish() // Sayfayı kapatır ve listeye döner
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Vazgeç", null)
                .show()
        }
    }

    private fun editNotification() {
        notificationId?.let { id ->
            val docRef = db.collection("notifications").document(id)
            docRef.get().addOnSuccessListener { doc ->
                val currentDesc = doc.getString("description") ?: ""
                // Admin düzenlemesi olduğunu belirtmek için metni güncelliyoruz
                val updatedDesc = "[Yönetici Tarafından Düzenlendi] $currentDesc"

                docRef.update("description", updatedDesc).addOnSuccessListener {
                    findViewById<TextView>(R.id.tvDetayAciklama).text = updatedDesc
                    Toast.makeText(this, "Açıklama başarıyla güncellendi.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}