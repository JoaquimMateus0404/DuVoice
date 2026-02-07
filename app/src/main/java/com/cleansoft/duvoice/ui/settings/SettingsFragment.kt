package com.cleansoft.duvoice.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cleansoft.duvoice.R
import com.cleansoft.duvoice.data.model.AudioFormat
import com.cleansoft.duvoice.data.model.AudioQuality
import com.cleansoft.duvoice.databinding.FragmentSettingsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    // Flag para evitar loop infinito nos listeners
    private var isUpdatingUI = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Primeiro observar (para preencher valores iniciais), depois configurar listeners
        observeViewModel()
        setupListeners()
    }

    private fun setupListeners() {
        binding.rgQuality.setOnCheckedChangeListener { _, checkedId ->
            if (isUpdatingUI) return@setOnCheckedChangeListener
            val quality = when (checkedId) {
                R.id.rbLow -> AudioQuality.LOW
                R.id.rbMedium -> AudioQuality.MEDIUM
                R.id.rbHigh -> AudioQuality.HIGH
                else -> return@setOnCheckedChangeListener
            }
            viewModel.updateQuality(quality)
        }

        binding.rgFormat.setOnCheckedChangeListener { _, checkedId ->
            if (isUpdatingUI) return@setOnCheckedChangeListener
            val format = when (checkedId) {
                R.id.rbWav -> AudioFormat.WAV
                R.id.rbAac -> AudioFormat.AAC
                else -> return@setOnCheckedChangeListener
            }
            viewModel.updateFormat(format)
        }

        binding.switchStereo.setOnCheckedChangeListener { _, isChecked ->
            if (isUpdatingUI) return@setOnCheckedChangeListener
            viewModel.updateStereo(isChecked)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.audioSettings.collectLatest { settings ->
                    isUpdatingUI = true

                    // Update quality radio buttons
                    val qualityId = when (settings.quality) {
                        AudioQuality.LOW -> R.id.rbLow
                        AudioQuality.MEDIUM -> R.id.rbMedium
                        AudioQuality.HIGH -> R.id.rbHigh
                    }
                    binding.rgQuality.check(qualityId)

                    // Update format radio buttons
                    val formatId = when (settings.format) {
                        AudioFormat.WAV -> R.id.rbWav
                        AudioFormat.AAC, AudioFormat.M4A, AudioFormat.MP3 -> R.id.rbAac
                    }
                    binding.rgFormat.check(formatId)

                    // Update stereo switch
                    binding.switchStereo.isChecked = settings.isStereo

                    isUpdatingUI = false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

