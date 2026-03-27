package com.alertacidadao.app.model

import com.alertacidadao.app.R

fun ReportStatus.label(): String = when (this) {
    ReportStatus.ABERTO -> "Aberto"
    ReportStatus.EM_ANALISE -> "Em Análise"
    ReportStatus.RESOLVIDO -> "Resolvido"
}

fun ReportStatus.colorRes(): Int = when (this) {
    ReportStatus.ABERTO -> R.color.status_open
    ReportStatus.EM_ANALISE -> R.color.status_analysis
    ReportStatus.RESOLVIDO -> R.color.status_resolved
}

fun ReportStatus.backgroundRes(): Int = when (this) {
    ReportStatus.ABERTO -> R.drawable.bg_status_chip_open
    ReportStatus.EM_ANALISE -> R.drawable.bg_status_chip_analysis
    ReportStatus.RESOLVIDO -> R.drawable.bg_status_chip_resolved
}

fun ReportStatus.markerRes(): Int = when (this) {
    ReportStatus.ABERTO -> R.drawable.ic_marker_open
    ReportStatus.EM_ANALISE -> R.drawable.ic_marker_analysis
    ReportStatus.RESOLVIDO -> R.drawable.ic_marker_resolved
}