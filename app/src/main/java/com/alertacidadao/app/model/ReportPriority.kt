package com.alertacidadao.app.model

enum class ReportPriority(val label: String) {
    BAIXA("Baixa"),
    MEDIA("Média"),
    ALTA("Alta");

    companion object {
        fun fromLabel(label: String): ReportPriority {
            return entries.firstOrNull { it.label.equals(label, ignoreCase = true) }
                ?: MEDIA
        }
    }
}