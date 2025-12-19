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

    private var onItemClickListener: ((Notification) -> Unit)? = null

    fun setOnItemClickListener(listener: (Notification) -> Unit) {
        onItemClickListener = listener
    }

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

        // Zamanı formatla
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

        notification.creationTime?.let { timestamp ->
            // Timestamp'i Date'e çeviriyoruz
            val date = timestamp.toDate()
            holder.timestamp.text = sdf.format(date)
        } ?: run {
            holder.timestamp.text = "--:--"
        }

        // durum renkleri
        when (notification.status) {
            "Açık" -> holder.status.setTextColor(Color.RED)
            "İnceleniyor" -> holder.status.setTextColor(Color.BLUE)
            "Çözüldü" -> holder.status.setTextColor(Color.GREEN)
            else -> holder.status.setTextColor(Color.GRAY)
        }
        // tür renkleri ve türler
        when (notification.type) {
            "Sağlık" -> {
                holder.icon.setImageResource(R.drawable.ic_saglik_yeni)
                holder.icon.setColorFilter(Color.RED)
            }
            "Güvenlik" -> {
                holder.icon.setImageResource(android.R.drawable.ic_lock_lock)
                holder.icon.setColorFilter(Color.BLACK)
            }
            "Çevre" -> {
                holder.icon.setImageResource(R.drawable.ic_cevre_yeni)
                holder.icon.setColorFilter(Color.parseColor("#4CAF50")) // Yeşil
            }
            "Teknik" -> {//turuncu
                holder.icon.setImageResource(android.R.drawable.ic_menu_preferences)
                holder.icon.setColorFilter(Color.parseColor("#FF9800"))
            }
            else -> {
                holder.icon.setImageResource(android.R.drawable.ic_dialog_info)
                holder.icon.setColorFilter(Color.GRAY)
            }
        }

        // Liste elemanına tıklandığında detay ekranına geçiş
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context

            // adminden tıklama varsa çalıştır
            if (onItemClickListener != null) {
                onItemClickListener?.invoke(notification)
            } else {
                // tıklama yoksa detay ekranına git 
                val intent = android.content.Intent(context, NotificationDetailActivity::class.java)

                intent.putExtra("notif_id", notification.id)
                intent.putExtra("notif_title", notification.title)
                intent.putExtra("notif_desc", notification.description)
                intent.putExtra("notif_type", notification.type)
                intent.putExtra("notif_status", notification.status)
                intent.putExtra("notif_lat", notification.latitude)
                intent.putExtra("notif_lng", notification.longitude)

                val sdfDetail = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val dateStr = notification.creationTime?.toDate()?.let { sdfDetail.format(it) } ?: "Bilinmiyor"
                intent.putExtra("notif_time", dateStr)

                context.startActivity(intent)
            }
        }
    }

    // listedeki öge sayısı
    override fun getItemCount(): Int = notificationList.size
}