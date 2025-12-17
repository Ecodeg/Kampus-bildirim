package com.example.kampus_bildirim

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit
import com.google.firebase.Timestamp

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var db: FirebaseFirestore
    private lateinit var infoCard: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        db = FirebaseFirestore.getInstance()
        infoCard = findViewById(R.id.infoCard)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Erzurum Koordinatları (Kampüs Civarı)
        val erzurumKampus = LatLng(39.9048, 41.2678)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(erzurumKampus, 14f))

        fetchNotificationsForMap()

        // Pine tıklanınca bilgi kartını aç
        mMap.setOnMarkerClickListener { marker ->
            val notification = marker.tag as? Notification
            notification?.let { showInfoCard(it) }
            false
        }

        // Haritada boş yere tıklanınca kartı kapat
        mMap.setOnMapClickListener { infoCard.visibility = View.GONE }
    }

    private fun fetchNotificationsForMap() {
        db.collection("notifications").addSnapshotListener { value, _ ->
            mMap.clear()
            value?.documents?.forEach { doc ->
                val notif = doc.toObject(Notification::class.java)
                notif?.let {
                    val pos = LatLng(it.latitude, it.longitude)

                    // Bildirimlere göre renk belirleme
                    val markerColor = when (it.type) {
                        "Sağlık" -> BitmapDescriptorFactory.HUE_RED        // Kırmızı
                        "Güvenlik" -> BitmapDescriptorFactory.HUE_BLUE     // Mavi
                        "Teknik" -> BitmapDescriptorFactory.HUE_ORANGE     // Turuncu
                        "Çevre" -> BitmapDescriptorFactory.HUE_GREEN       // Yeşil
                        else -> BitmapDescriptorFactory.HUE_CYAN           // Diğerleri Turkuaz
                    }

                    val marker = mMap.addMarker(MarkerOptions()
                        .position(pos)
                        .title(it.title)
                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor)))

                    marker?.tag = it // Notification'ı bağla (Detay için lazım)
                }
            }
        }
    }

    private fun showInfoCard(notif: Notification) {
        val tvTitle = findViewById<TextView>(R.id.tvMapTitle)
        val tvType = findViewById<TextView>(R.id.tvMapType)
        val tvTime = findViewById<TextView>(R.id.tvMapTime)
        val btnGoDetail = findViewById<Button>(R.id.btnGoDetail)

        // Bilgileri karta yazıyoruz
        tvTitle.text = notif.title
        tvType.text = "Tür: ${notif.type}"

        // Bildirim ne kadar süre önce oluşturuldu hesaplıyoruz
        val timeAgo = try {
            notif.creationTime?.let {
                val diff = System.currentTimeMillis() - it.toDate().time
                val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(diff)
                val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(diff)

                when {
                    minutes < 1 -> "Şimdi"
                    minutes < 60 -> "$minutes dk önce"
                    hours < 24 -> "$hours sa önce"
                    else -> "${hours / 24} gün önce"
                }
            } ?: "Zaman bilgisi yok"
        } catch (e: Exception) {
            "Zaman hesaplanamadı"
        }

        tvTime.text = timeAgo


        infoCard.visibility = View.VISIBLE

        // Detay gör
        btnGoDetail.setOnClickListener {
            val intent = Intent(this, NotificationDetailActivity::class.java)
            // Tüm bilgileri (ID dahil) detay sayfasına gönderiyoruz
            intent.putExtra("notif_id", notif.id)
            intent.putExtra("notif_title", notif.title)
            intent.putExtra("notif_desc", notif.description)
            intent.putExtra("notif_type", notif.type)
            intent.putExtra("notif_status", notif.status)
            intent.putExtra("notif_time", timeAgo)
            startActivity(intent)
        }
    }
}