package com.alertacidadao.app.model

data class Report(
    val id: Int,
    val title: String,
    val category: String,
    val location: String,
    val bairro: String,
    val date: String,
    val status: ReportStatus,
    val description: String = "",
    val priority: ReportPriority = ReportPriority.MEDIA,
    val photoUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
