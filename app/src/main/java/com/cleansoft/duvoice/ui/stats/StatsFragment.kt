package com.cleansoft.duvoice.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cleansoft.duvoice.R
import com.cleansoft.duvoice.data.model.Category
import com.cleansoft.duvoice.databinding.FragmentStatsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.stats.collectLatest { stats ->
                        // Estatísticas principais
                        binding.tvTotalRecordings.text = stats.totalRecordings.toString()
                        binding.tvTotalHours.text = stats.totalHoursFormatted
                        binding.tvTotalSize.text = stats.totalSizeFormatted

                        // Esta semana
                        binding.tvRecordingsThisWeek.text = stats.recordingsThisWeek.toString()
                        binding.tvDurationThisWeek.text = stats.durationThisWeekFormatted

                        // Médias
                        binding.tvAverageDuration.text = stats.averageDurationFormatted

                        // Favoritos
                        binding.tvFavorites.text = stats.favoriteCount.toString()

                        // Por categoria
                        updateCategoryStats(stats.recordingsByCategory)
                    }
                }

                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        binding.swipeRefresh.isRefreshing = isLoading
                        binding.progressBar.isVisible = isLoading && binding.tvTotalRecordings.text.isEmpty()
                    }
                }
            }
        }
    }

    private fun updateCategoryStats(categoryStats: Map<Category, Int>) {
        val sb = StringBuilder()
        Category.entries.forEach { category ->
            val count = categoryStats[category] ?: 0
            if (count > 0) {
                sb.append("${category.icon} ${category.displayName}: $count\n")
            }
        }
        binding.tvCategoryStats.text = sb.toString().trim().ifEmpty {
            getString(R.string.no_recordings)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

