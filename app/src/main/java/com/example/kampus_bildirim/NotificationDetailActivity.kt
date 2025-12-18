package com.example.kampus_bildirim

import android.os.Bundle
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

class NotificationDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var notificationId: String? = null
    //yeni eklenen bileşenler
    private lateinit var mMap: GoogleMap
    private lateinit var btnFollow: Button // Yeni buton
    private var isFollowing = false // Takip durumu kontrolü

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_detail)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

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
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            db.collection("users").document(currentUserId).get().addOnSuccessListener { doc ->
                val role = doc.getString("role")
                if (role == "Admin") {
                    findViewById<Button>(R.id.btnUpdateStatus).visibility = View.VISIBLE
                }
            }
        }
    }

    private fun checkFollowStatus() {
        val currentUserId = auth.currentUser?.uid ?: return
        notificationId?.let { id ->
            db.collection("notifications").document(id).get().addOnSuccessListener { doc ->
                val followers = doc.get("followers") as? List<String>
                if (followers != null && followers.contains(currentUserId)) {
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
}