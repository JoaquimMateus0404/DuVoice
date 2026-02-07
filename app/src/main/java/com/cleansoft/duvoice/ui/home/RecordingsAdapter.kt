package com.cleansoft.duvoice.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cleansoft.duvoice.R
import com.cleansoft.duvoice.data.model.Recording
import com.cleansoft.duvoice.databinding.ItemRecordingBinding
import com.cleansoft.duvoice.util.audio.AudioPlayer
import java.text.SimpleDateFormat
import java.util.*

class RecordingsAdapter(
    private val onPlayClick: (Recording) -> Unit,
    private val onMenuClick: (Recording, View) -> Unit,
    private val onFavoriteClick: (Recording) -> Unit,
    private val onItemClick: (Recording) -> Unit
) : ListAdapter<Recording, RecordingsAdapter.RecordingViewHolder>(RecordingDiffCallback()) {

    private var currentPlayingId: Long? = null
    private var playerState: AudioPlayer.State = AudioPlayer.State.IDLE

    fun setPlayingId(id: Long?) {
        val oldId = currentPlayingId
        currentPlayingId = id

        // Atualizar items afetados
        currentList.forEachIndexed { index, recording ->
            if (recording.id == oldId || recording.id == id) {
                notifyItemChanged(index)
            }
        }
    }

    fun setPlayerState(state: AudioPlayer.State) {
        playerState = state
        currentPlayingId?.let { id ->
            currentList.forEachIndexed { index, recording ->
                if (recording.id == id) {
                    notifyItemChanged(index)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        val binding = ItemRecordingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecordingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecordingViewHolder(
        private val binding: ItemRecordingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recording: Recording) {
            binding.apply {
                tvName.text = recording.name
                tvCategory.text = "${recording.category.icon} ${recording.category.displayName}"
                tvDuration.text = recording.durationFormatted
                tvDate.text = formatDate(recording.createdAt)

                // Ícone de favorito
                ivFavorite.setImageResource(
                    if (recording.isFavorite) android.R.drawable.star_on
                    else android.R.drawable.star_off
                )
                ivFavorite.setOnClickListener { onFavoriteClick(recording) }

                // Botão de play/pause
                val isPlaying = recording.id == currentPlayingId && playerState == AudioPlayer.State.PLAYING
                btnPlay.setImageResource(
                    if (isPlaying) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
                )
                btnPlay.setOnClickListener { onPlayClick(recording) }

                // Menu
                btnMenu.setOnClickListener { onMenuClick(recording, it) }

                // Click no item
                root.setOnClickListener { onItemClick(recording) }
            }
        }

        private fun formatDate(date: Date): String {
            val now = Calendar.getInstance()
            val recordingDate = Calendar.getInstance().apply { time = date }

            return when {
                isSameDay(now, recordingDate) -> {
                    "Hoje, ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)}"
                }
                isYesterday(now, recordingDate) -> {
                    "Ontem, ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)}"
                }
                else -> {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                }
            }
        }

        private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        }

        private fun isYesterday(today: Calendar, other: Calendar): Boolean {
            val yesterday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
            return isSameDay(yesterday, other)
        }
    }

    class RecordingDiffCallback : DiffUtil.ItemCallback<Recording>() {
        override fun areItemsTheSame(oldItem: Recording, newItem: Recording): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recording, newItem: Recording): Boolean {
            return oldItem == newItem
        }
    }
}

