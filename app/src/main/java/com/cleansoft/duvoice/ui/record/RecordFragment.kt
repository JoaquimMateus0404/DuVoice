package com.cleansoft.duvoice.ui.record

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cleansoft.duvoice.R
import com.cleansoft.duvoice.data.model.Category
import com.cleansoft.duvoice.databinding.FragmentRecordBinding
import com.cleansoft.duvoice.util.audio.AudioRecorder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecordFragment : Fragment() {

    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecordViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        if (audioGranted) {
            startRecording()
        } else {
            showPermissionRationale()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.bindService(requireContext())

        setupCategoryDropdown()
        setupButtons()
        observeViewModel()
    }

    private fun setupCategoryDropdown() {
        val categories = Category.entries.map { "${it.icon} ${it.displayName}" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)
        binding.actvCategory.setText(categories[0], false)

        binding.actvCategory.setOnItemClickListener { _, _, position, _ ->
            viewModel.setCategory(Category.entries[position])
        }
    }

    private fun setupButtons() {
        binding.btnRecord.setOnClickListener {
            when (viewModel.recordingState.value) {
                AudioRecorder.State.IDLE, AudioRecorder.State.STOPPED -> checkPermissionsAndRecord()
                AudioRecorder.State.RECORDING -> viewModel.pauseRecording(requireContext())
                AudioRecorder.State.PAUSED -> viewModel.resumeRecording(requireContext())
            }
        }

        binding.btnStop.setOnClickListener {
            viewModel.stopRecording(requireContext())
        }

        binding.btnCancel.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.cancel)
                .setMessage("Deseja cancelar a gravação atual?")
                .setPositiveButton(R.string.ok) { _, _ ->
                    viewModel.cancelRecording(requireContext())
                }
                .setNegativeButton(R.string.cancel_dialog, null)
                .show()
        }


        binding.etName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val currentName = binding.etName.text?.toString() ?: ""
                if (currentName.isNotBlank()) {
                    viewModel.setRecordingName(currentName)
                }
            }
        }

        // Também salvar quando o usuário terminar de digitar
        binding.etName.setOnEditorActionListener { _, _, _ ->
            val currentName = binding.etName.text?.toString() ?: ""
            if (currentName.isNotBlank()) {
                viewModel.setRecordingName(currentName)
            }
            false
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.recordingState.collectLatest { state ->
                        updateUIForState(state)
                    }
                }

                launch {
                    viewModel.elapsedTimeFormatted.collectLatest { time ->
                        binding.tvTimer.text = time
                    }
                }

                launch {
                    viewModel.amplitude.collectLatest { amplitude ->
                        binding.waveformView.addAmplitude(amplitude)
                    }
                }

                launch {
                    viewModel.recordingName.collectLatest { name ->
                        val currentText = binding.etName.text?.toString() ?: ""
                        val isIdle = viewModel.recordingState.value == AudioRecorder.State.IDLE

                        // Se o ViewModel enviar vazio, limpar o campo
                        if (name.isBlank()) {
                            binding.etName.setText("")
                        }
                        // Caso contrário, só atualizar se o campo estiver vazio e estivermos IDLE
                        else if (currentText.isBlank() && isIdle) {
                            binding.etName.setText(name)
                        }
                    }
                }

                launch {
                    viewModel.recordingSaved.collectLatest { recording ->
                        Snackbar.make(binding.root, R.string.recording_saved, Snackbar.LENGTH_SHORT).show()
                        binding.waveformView.clear()
                    }
                }

                launch {
                    viewModel.error.collectLatest { error ->
                        Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun updateUIForState(state: AudioRecorder.State) {
        when (state) {
            AudioRecorder.State.IDLE, AudioRecorder.State.STOPPED -> {
                binding.tvState.text = getString(R.string.tap_to_start)
                binding.tvTimer.text = "00:00"
                binding.btnRecord.setImageResource(R.drawable.ic_mic)
                binding.btnCancel.isVisible = false
                binding.btnStop.isVisible = false
                binding.categoryLayout.isEnabled = true
                binding.nameLayout.isEnabled = true
                binding.waveformView.clear()
            }
            AudioRecorder.State.RECORDING -> {
                binding.tvState.text = getString(R.string.recording)
                binding.btnRecord.setImageResource(R.drawable.ic_pause)
                binding.btnCancel.isVisible = true
                binding.btnStop.isVisible = true
                binding.categoryLayout.isEnabled = false
                binding.nameLayout.isEnabled = true
            }
            AudioRecorder.State.PAUSED -> {
                binding.tvState.text = getString(R.string.paused)
                binding.btnRecord.setImageResource(R.drawable.ic_play)
                binding.btnCancel.isVisible = true
                binding.btnStop.isVisible = true
                binding.categoryLayout.isEnabled = false
                binding.nameLayout.isEnabled = true
            }
        }
    }

    private fun checkPermissionsAndRecord() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isEmpty()) {
            startRecording()
        } else {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun startRecording() {
        viewModel.startRecording(requireContext())
    }

    private fun showPermissionRationale() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.permission_required)
            .setMessage(R.string.microphone_permission_rationale)
            .setPositiveButton(R.string.grant_permission) { _, _ ->
                permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
            }
            .setNegativeButton(R.string.cancel_dialog, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.unbindService(requireContext())
        _binding = null
    }
}

