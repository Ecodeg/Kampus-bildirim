package com.example.kampus_bildirim

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
class NotificationAdapter(private val notificationList: List<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

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

        // Zamanı çevirme
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        holder.timestamp.text = sdf.format(Date(notification.timestamp))

        //  ikon rengi veya ikon değiştirme türe göre
        when (notification.status) {
            "Açık" -> holder.status.setBackgroundResource(android.R.color.holo_red_light)
            "Çözüldü" -> holder.status.setBackgroundResource(android.R.color.holo_green_light)
        }
    }

    // listedeki öge sayısı
    override fun getItemCount(): Int = notificationList.size
}