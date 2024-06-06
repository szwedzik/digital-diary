package com.the8way.digitaldiary.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.the8way.digitaldiary.R
import com.the8way.digitaldiary.data.DiaryEntry
import com.the8way.digitaldiary.utils.LocationUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryEntryAdapter(
    private val diaryEntries: List<DiaryEntry>,
    private val onItemClick: (DiaryEntry) -> Unit,
    private val onItemLongClick: (DiaryEntry) -> Unit
) : RecyclerView.Adapter<DiaryEntryAdapter.DiaryEntryViewHolder>() {

    inner class DiaryEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
        private val contentTextView: TextView = itemView.findViewById(R.id.content_text_view)
        private val imageView: ImageView = itemView.findViewById(R.id.entry_image_view)
        private val locationView: TextView = itemView.findViewById(R.id.location_text_view)
        private val dateView: TextView = itemView.findViewById(R.id.created_date_text_view)

        fun bind(diaryEntry: DiaryEntry) {

            titleTextView.text = diaryEntry.title
            contentTextView.text = if (diaryEntry.content.length >= 100) {
                diaryEntry.content.substring(0, 100) + "..."
            } else {
                diaryEntry.content
            }

            imageView.visibility = View.VISIBLE
            Glide.with(itemView.context)
                .load(diaryEntry.imageUri)
                .placeholder(R.drawable.placeholder_image)
                .into(imageView)

            (itemView.context as? LifecycleOwner)?.lifecycleScope?.launch {
                val address = LocationUtils(itemView.context).getAddressFromLocation(diaryEntry.latitude, diaryEntry.longitude)
                locationView.text = address
            }

            val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(diaryEntry.createdTime))
            val displayText = String.format(itemView.context.getString(R.string.created_at), formattedDate)
            dateView.text = displayText

            itemView.setOnClickListener {
                onItemClick(diaryEntry)
            }

            itemView.setOnLongClickListener {
                AlertDialog.Builder(itemView.context)
                    .setMessage(R.string.delete_confirmation)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        onItemLongClick(diaryEntry)
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryEntryViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_diary_entry, parent, false)
        return DiaryEntryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DiaryEntryViewHolder, position: Int) {
        holder.bind(diaryEntries[position])
    }

    override fun getItemCount() = diaryEntries.size
}
