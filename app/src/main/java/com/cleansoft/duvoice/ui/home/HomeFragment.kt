package com.cleansoft.duvoice.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cleansoft.duvoice.R
import com.cleansoft.duvoice.data.model.Category
import com.cleansoft.duvoice.data.model.Recording
import com.cleansoft.duvoice.data.model.SortOrder
import com.cleansoft.duvoice.databinding.FragmentHomeBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: RecordingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilters()
        setupSearch()
        setupSortButton()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = RecordingsAdapter(
            onPlayClick = { recording -> viewModel.playRecording(recording) },
            onMenuClick = { recording, anchor -> showRecordingMenu(recording, anchor) },
            onFavoriteClick = { recording -> viewModel.toggleFavorite(recording) },
            onItemClick = { recording -> navigateToPlayer(recording) }
        )

        binding.recyclerViewRecordings.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewRecordings.adapter = adapter
    }

    private fun setupFilters() {
        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setCategory(null)
        }

        binding.chipFavorites.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleFavoritesFilter()
        }

        binding.chipClasses.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setCategory(Category.CLASSES)
        }

        binding.chipMeetings.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setCategory(Category.MEETINGS)
        }

        binding.chipIdeas.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setCategory(Category.IDEAS)
        }

        binding.chipMusic.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) viewModel.setCategory(Category.MUSIC)
        }
    }

    private fun setupSearch() {
        binding.searchBar.setOnClickListener {
            // Implementar SearchView se necessário
        }
    }

    private fun setupSortButton() {
        binding.btnSort.setOnClickListener { anchor ->
            val popup = PopupMenu(requireContext(), anchor)
            SortOrder.values().forEach { order ->
                popup.menu.add(order.displayName).setOnMenuItemClickListener {
                    viewModel.setSortOrder(order)
                    true
                }
            }
            popup.show()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.recordings.collectLatest { recordings ->
                        adapter.submitList(recordings)
                        binding.emptyState.isVisible = recordings.isEmpty()
                        binding.recyclerViewRecordings.isVisible = recordings.isNotEmpty()
                        binding.tvRecordingsCount.text = getString(R.string.recordings_count, recordings.size)
                    }
                }

                launch {
                    viewModel.currentPlayingId.collectLatest { playingId ->
                        adapter.setPlayingId(playingId)
                    }
                }

                launch {
                    viewModel.audioPlayer.state.collectLatest { state ->
                        adapter.setPlayerState(state)
                    }
                }
            }
        }
    }

    private fun showRecordingMenu(recording: Recording, anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_recording_item, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_rename -> showRenameDialog(recording)
                R.id.action_category -> showCategoryDialog(recording)
                R.id.action_favorite -> viewModel.toggleFavorite(recording)
                R.id.action_share -> shareRecording(recording)
                R.id.action_delete -> showDeleteDialog(recording)
            }
            true
        }
        popup.show()
    }

    private fun showRenameDialog(recording: Recording) {
        val input = TextInputEditText(requireContext()).apply {
            setText(recording.name)
            hint = getString(R.string.recording_name)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.rename_recording)
            .setView(input)
            .setPositiveButton(R.string.ok) { _, _ ->
                val newName = input.text?.toString()?.trim()
                if (!newName.isNullOrBlank()) {
                    viewModel.renameRecording(recording.id, newName)
                }
            }
            .setNegativeButton(R.string.cancel_dialog, null)
            .show()
    }

    private fun showCategoryDialog(recording: Recording) {
        val categories = Category.values()
        val items = categories.map { "${it.icon} ${it.displayName}" }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.move_to_category)
            .setItems(items) { _, which ->
                viewModel.updateCategory(recording.id, categories[which])
            }
            .show()
    }

    private fun showDeleteDialog(recording: Recording) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_recording)
            .setMessage(getString(R.string.delete_confirmation, recording.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteRecording(recording)
            }
            .setNegativeButton(R.string.cancel_dialog, null)
            .show()
    }

    private fun shareRecording(recording: Recording) {
        // Implementar partilha
    }

    private fun navigateToPlayer(recording: Recording) {
        val bundle = bundleOf("recordingId" to recording.id)
        findNavController().navigate(R.id.action_home_to_player, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

