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

        setupQualitySelection()
        setupFormatSelection()
        setupStereoSwitch()
        observeViewModel()
    }

    private fun setupQualitySelection() {
        binding.rgQuality.setOnCheckedChangeListener { _, checkedId ->
            val quality = when (checkedId) {
                R.id.rbLow -> AudioQuality.LOW
                R.id.rbMedium -> AudioQuality.MEDIUM
                R.id.rbHigh -> AudioQuality.HIGH
                else -> return@setOnCheckedChangeListener
            }
            viewModel.updateQuality(quality)
        }
    }

    private fun setupFormatSelection() {
        binding.rgFormat.setOnCheckedChangeListener { _, checkedId ->
            val format = when (checkedId) {
                R.id.rbWav -> AudioFormat.WAV
                R.id.rbAac -> AudioFormat.AAC
                else -> return@setOnCheckedChangeListener
            }
            viewModel.updateFormat(format)
        }
    }

    private fun setupStereoSwitch() {
        binding.switchStereo.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateStereo(isChecked)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.audioSettings.collectLatest { settings ->
                    // Update quality radio buttons
                    val qualityId = when (settings.quality) {
                        AudioQuality.LOW -> R.id.rbLow
                        AudioQuality.MEDIUM -> R.id.rbMedium
                        AudioQuality.HIGH -> R.id.rbHigh
                    }
                    if (binding.rgQuality.checkedRadioButtonId != qualityId) {
                        binding.rgQuality.check(qualityId)
                    }

                    // Update format radio buttons
                    val formatId = when (settings.format) {
                        AudioFormat.WAV -> R.id.rbWav
                        AudioFormat.AAC, AudioFormat.M4A -> R.id.rbAac
                        else -> R.id.rbWav
                    }
                    if (binding.rgFormat.checkedRadioButtonId != formatId) {
                        binding.rgFormat.check(formatId)
                    }

                    // Update stereo switch
                    if (binding.switchStereo.isChecked != settings.isStereo) {
                        binding.switchStereo.isChecked = settings.isStereo
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

