package com.alertacidadao.app.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alertacidadao.app.R
import com.alertacidadao.app.ReportDetailActivity
import com.alertacidadao.app.adapter.ReportAdapter
import com.alertacidadao.app.data.ReportRepository
import com.alertacidadao.app.databinding.FragmentHomeBinding
import com.alertacidadao.app.model.ReportStatus

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ReportAdapter
    private var currentFilter: ReportStatus? = null
    private var currentSearchQuery: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupRecyclerView()
        updateStats()
        setupFilterChips()
        setupSearch()
    }

    private fun setupRecyclerView() {
        adapter = ReportAdapter()
        adapter.onItemClick = { report ->
            val intent = Intent(requireContext(), ReportDetailActivity::class.java)
                .putExtra(ReportDetailActivity.EXTRA_REPORT_ID, report.id)
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        refreshReports()
    }

    private fun updateStats() {
        val context = requireContext()
        val stats = ReportRepository.getStats(context)

        binding.textOpenCount.text = stats.open.toString()
        binding.textAnalysisCount.text = stats.analysis.toString()
        binding.textResolvedCount.text = stats.resolved.toString()
    }

    private fun setupFilterChips() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener { _, _ ->
            val checkedId = binding.chipGroupFilters.checkedChipId
            currentFilter = when (checkedId) {
                R.id.chipTodos -> null
                R.id.chipAberto -> ReportStatus.ABERTO
                R.id.chipAnalise -> ReportStatus.EM_ANALISE
                R.id.chipResolvido -> ReportStatus.RESOLVIDO
                else -> currentFilter
            }
            refreshReports()
        }
    }

    private fun refreshReports() {
        val context = requireContext()
        val reports = when {
            currentSearchQuery.isNotBlank() -> ReportRepository.search(context, currentSearchQuery)
            currentFilter == null -> ReportRepository.getAll(context)
            else -> ReportRepository.getByStatus(context, currentFilter ?: return)
        }

        adapter.submitList(reports)

        if (reports.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private fun setupSearch() {
        binding.editTextSearch.setOnEditorActionListener { textView, _, _ ->
            currentSearchQuery = textView.text.toString().trim()
            refreshReports()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        updateStats()
        if (_binding != null) {
            refreshReports()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
