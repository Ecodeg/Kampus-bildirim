package com.example.kampus_bildirim

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Adapter bildirim listesini girdi olarak alacak
// var kullandım updateList ile güncellemek için
class NotificationAdapter(private var notificationList: List<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    fun updateList(newList: List<Notification>) {
        notificationList = newList
        notifyDataSetChanged()
    }

    // bileşenleri(xml) koda bağla
    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val description: TextView = view.findViewById(R.id.tvDescription)
        val status: TextView = view.findViewById(R.id.tvStatus)
        val timestamp: TextView = view.findViewById(R.id.tvTimestamp)
        val icon: ImageView = view.findViewById(R.id.ivTypeIcon)
    }

    //  Tasarım dosyasını (list_item_notification) tanı
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    // Veriyi tasarıma yerleştirme
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notificationList[position]

        holder.title.text = notification.title
        holder.description.text = notification.description
        holder.status.text = notification.status

        // Zamanı formatla (HH:mm -> Saat:Dakika)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

        notification.creationTime?.let {
            holder.timestamp.text = sdf.format(it)
        } ?: run {
            holder.timestamp.text = "--:--" // Eğer zaman boşsa (null)
        }

        // durum renkleri
        when (notification.status) {
            "Açık" -> holder.status.setTextColor(Color.RED)
            "İnceleniyor" -> holder.status.setTextColor(Color.BLUE)
            "Çözüldü" -> holder.status.setTextColor(Color.GREEN)
            else -> holder.status.setTextColor(Color.GRAY)
        }
        // tür renkleri
        when (notification.type) {
            "Sağlık" -> holder.icon.setImageResource(android.R.drawable.ic_menu_mylocation) // Buraya kendi ikonlarını koyabilirsin
            "Güvenlik" -> holder.icon.setImageResource(android.R.drawable.ic_lock_lock)
            "Çevre" -> holder.icon.setImageResource(android.R.drawable.ic_menu_compass)
            else -> holder.icon.setImageResource(android.R.drawable.ic_dialog_info)
        }
    }

    // listedeki öge sayısı
    override fun getItemCount(): Int = notificationList.size
}