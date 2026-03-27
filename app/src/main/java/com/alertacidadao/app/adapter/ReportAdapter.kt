package com.alertacidadao.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alertacidadao.app.R
import com.alertacidadao.app.databinding.ItemReportBinding
import com.alertacidadao.app.model.Report
import com.alertacidadao.app.model.ReportStatus
import com.alertacidadao.app.model.backgroundRes
import com.alertacidadao.app.model.colorRes
import com.alertacidadao.app.model.label

class ReportAdapter :
    ListAdapter<Report, ReportAdapter.ReportViewHolder>(DiffCallback()) {

    var onItemClick: ((Report) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReportViewHolder(private val binding: ItemReportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(report: Report) {
            val context = binding.root.context

            // Título
            binding.textTitle.text = report.title

            // Categoria
            binding.textCategory.text = report.category

            // Localização + Data
            binding.textLocation.text = "${report.bairro} • ${report.date}"

            binding.textStatus.text = report.status.label()
            binding.textStatus.setBackgroundResource(report.status.backgroundRes())
            binding.textStatus.setTextColor(context.getColor(report.status.colorRes()))

            binding.root.setOnClickListener {
                onItemClick?.invoke(report)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Report>() {
        override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean =
            oldItem == newItem
    }
}
