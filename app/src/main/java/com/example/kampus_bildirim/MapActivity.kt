package com.example.kampus_bildirim

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
                    val markerColor = when (it.type) {
                        "Sağlık" -> BitmapDescriptorFactory.HUE_RED
                        "Güvenlik" -> BitmapDescriptorFactory.HUE_BLUE
                        "Teknik" -> BitmapDescriptorFactory.HUE_YELLOW
                        else -> BitmapDescriptorFactory.HUE_GREEN
                    }

                    val marker = mMap.addMarker(MarkerOptions()
                        .position(pos)
                        .title(it.title)
                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor)))

                    marker?.tag = it // Notification nesnesini marker'a bağla
                }
            }
        }
    }

    private fun showInfoCard(notif: Notification) {
        findViewById<TextView>(R.id.tvMapTitle).text = notif.title
        findViewById<TextView>(R.id.tvMapType).text = "Tür: ${notif.type}"

        // Zamanı hesapla
        val diff = System.currentTimeMillis() - (notif.creationTime?.time ?: System.currentTimeMillis())
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        findViewById<TextView>(R.id.tvMapTime).text = "$minutes dakika önce oluşturuldu"

        infoCard.visibility = View.VISIBLE

        findViewById<Button>(R.id.btnGoDetail).setOnClickListener {
            // Detay sayfasına geçiş
        }
    }
}