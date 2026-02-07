package com.cleansoft.duvoice.ui.player

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cleansoft.duvoice.R
import com.cleansoft.duvoice.databinding.FragmentPlayerBinding
import com.cleansoft.duvoice.util.audio.AudioPlayer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recordingId = arguments?.getLong("recordingId") ?: run {
            findNavController().navigateUp()
            return
        }
        viewModel.loadRecording(recordingId)

        setupControls()
        observeViewModel()
    }

    private fun setupControls() {
        binding.btnPlayPause.setOnClickListener {
            viewModel.togglePlayPause()
        }

        binding.btnSkipBack.setOnClickListener {
            viewModel.skipBackward(10)
        }

        binding.btnSkipForward.setOnClickListener {
            viewModel.skipForward(10)
        }

        binding.slider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.seekTo(value.toInt())
            }
        }

        binding.btnFavorite.setOnClickListener {
            viewModel.toggleFavorite()
        }

        binding.btnShare.setOnClickListener {
            shareRecording()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentRecording.collectLatest { recording ->
                        recording?.let {
                            binding.tvName.text = it.name
                            binding.tvInfo.text = "${it.category.icon} ${it.category.displayName} • ${it.sizeFormatted} • ${it.format.extension.uppercase()}"
                            updateFavoriteButton(it.isFavorite)

                            // Carregar waveform estático para o ficheiro
                            binding.waveformView.loadFromFile(it.filePath)
                        }
                    }
                }

                launch {
                    viewModel.playerState.collectLatest { state ->
                        updatePlayButton(state)
                    }
                }

                launch {
                    viewModel.currentPosition.collectLatest { position ->
                        if (!binding.slider.isPressed) {
                            binding.slider.value = position.toFloat().coerceIn(binding.slider.valueFrom, binding.slider.valueTo)
                        }
                        // Atualizar progresso do waveform
                        val duration = viewModel.duration.value
                        if (duration > 0) {
                            binding.waveformView.setProgress(position.toFloat() / duration)
                        }
                    }
                }

                launch {
                    viewModel.duration.collectLatest { duration ->
                        binding.slider.valueTo = duration.toFloat().coerceAtLeast(1f)
                    }
                }

                launch {
                    viewModel.currentPositionFormatted.collectLatest { time ->
                        binding.tvCurrentTime.text = time
                    }
                }

                launch {
                    viewModel.durationFormatted.collectLatest { time ->
                        binding.tvDuration.text = time
                    }
                }

                launch {
                    viewModel.deleted.collectLatest { deleted ->
                        if (deleted) {
                            Snackbar.make(binding.root, R.string.recording_deleted, Snackbar.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                    }
                }
            }
        }
    }

    private fun updatePlayButton(state: AudioPlayer.State) {
        val icon = when (state) {
            AudioPlayer.State.PLAYING -> R.drawable.ic_pause
            else -> R.drawable.ic_play
        }
        binding.btnPlayPause.setImageResource(icon)
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        val icon = if (isFavorite) android.R.drawable.star_on else android.R.drawable.star_off
        binding.btnFavorite.setIconResource(icon)
    }

    private fun shareRecording() {
        viewModel.currentRecording.value?.let { recording ->
            try {
                val file = File(recording.filePath)
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = recording.format.mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(Intent.createChooser(intent, getString(R.string.share)))
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Erro ao partilhar", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteDialog() {
        viewModel.currentRecording.value?.let { recording ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_recording)
                .setMessage(getString(R.string.delete_confirmation, recording.name))
                .setPositiveButton(R.string.delete) { _, _ ->
                    viewModel.deleteRecording()
                }
                .setNegativeButton(R.string.cancel_dialog, null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.releasePlayer()
        _binding = null
    }
}
