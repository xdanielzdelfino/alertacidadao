package com.alertacidadao.app.data

import android.content.Context
import com.alertacidadao.app.model.Report
import com.alertacidadao.app.model.ReportPriority
import com.alertacidadao.app.model.ReportStats
import com.alertacidadao.app.model.ReportStatus
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportRepository {

    private const val PREFS_NAME = "alerta_cidadao_reports"
    private const val KEY_REPORTS = "reports_json"

    private val legacySeedReports = listOf(
        Report(
            1,
            "Buraco na Av. Beira Mar",
            "Buraco na via",
            "Av. Beira Mar",
            "Meireles",
            "Hoje, 14:32",
            ReportStatus.EM_ANALISE,
            "Buraco grande na faixa da direita, próximo ao calçadão.",
            ReportPriority.ALTA,
            null,
            -3.7270,
            -38.4963
        ),
        Report(
            2,
            "Poste apagado há 3 dias",
            "Iluminação pública",
            "R. Ana Bilhar",
            "Aldeota",
            "Ontem, 09:15",
            ReportStatus.ABERTO,
            "Trecho inteiro da rua permanece sem iluminação.",
            ReportPriority.MEDIA,
            null,
            -3.7442,
            -38.5064
        ),
        Report(
            3,
            "Lixo acumulado na calçada",
            "Lixo irregular",
            "R. Frederico Borges",
            "Varjota",
            "23/03, 18:00",
            ReportStatus.ABERTO,
            "Acúmulo de resíduos na calçada em frente ao comércio.",
            ReportPriority.MEDIA,
            null,
            -3.7285,
            -38.4899
        ),
        Report(
            4,
            "Árvore caída bloqueando rua",
            "Árvore caída",
            "Av. Eng. Santana Júnior",
            "Papicu",
            "22/03, 16:45",
            ReportStatus.RESOLVIDO,
            "Queda parcial da árvore bloqueando o trânsito local.",
            ReportPriority.ALTA,
            null,
            -3.7593,
            -38.4804
        ),
        Report(
            5,
            "Sinalização danificada",
            "Sinalização",
            "Av. Washington Soares",
            "Água Fria",
            "21/03, 10:20",
            ReportStatus.RESOLVIDO,
            "Placa de trânsito torta e com pouca visibilidade.",
            ReportPriority.BAIXA,
            null,
            -3.7942,
            -38.4929
        )
    )

    @Synchronized
    fun getAll(context: Context): List<Report> = loadReports(context)

    @Synchronized
    fun getById(context: Context, id: Int): Report? =
        loadReports(context).find { it.id == id }

    @Synchronized
    fun getByStatus(context: Context, status: ReportStatus): List<Report> =
        loadReports(context).filter { it.status == status }

    @Synchronized
    fun countByStatus(context: Context, status: ReportStatus): Int =
        loadReports(context).count { it.status == status }

    @Synchronized
    fun getStats(context: Context): ReportStats {
        val reports = loadReports(context)
        return ReportStats(
            total = reports.size,
            open = reports.count { it.status == ReportStatus.ABERTO },
            analysis = reports.count { it.status == ReportStatus.EM_ANALISE },
            resolved = reports.count { it.status == ReportStatus.RESOLVIDO }
        )
    }

    @Synchronized
    fun add(context: Context, report: Report): Report {
        val reports = loadReports(context).toMutableList()
        val saved = report.copy(id = (reports.maxOfOrNull { it.id } ?: 0) + 1)
        reports.add(0, saved)
        saveReports(context, reports)
        return saved
    }

    @Synchronized
    fun update(context: Context, report: Report) {
        val reports = loadReports(context).toMutableList()
        val index = reports.indexOfFirst { it.id == report.id }
        if (index != -1) {
            reports[index] = report
            saveReports(context, reports)
        }
    }

    @Synchronized
    fun delete(context: Context, id: Int) {
        val reports = loadReports(context).toMutableList()
        reports.removeAll { it.id == id }
        saveReports(context, reports)
    }

    @Synchronized
    fun search(context: Context, query: String): List<Report> {
        val normalizedQuery = query.trim().lowercase(Locale.getDefault())
        if (normalizedQuery.isBlank()) {
            return getAll(context)
        }

        return loadReports(context).filter { report ->
            report.run {
                title.contains(normalizedQuery, ignoreCase = true) ||
                    category.contains(normalizedQuery, ignoreCase = true) ||
                    location.contains(normalizedQuery, ignoreCase = true) ||
                    bairro.contains(normalizedQuery, ignoreCase = true) ||
                    description.contains(normalizedQuery, ignoreCase = true) ||
                    priority.label.contains(normalizedQuery, ignoreCase = true) ||
                    status.name.contains(normalizedQuery, ignoreCase = true)
            }
        }
    }

    fun formatCurrentDate(): String {
        return SimpleDateFormat("dd/MM, HH:mm", Locale("pt", "BR")).format(Date())
    }

    private fun loadReports(context: Context): List<Report> {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(KEY_REPORTS, null)

        if (stored.isNullOrBlank()) {
            saveReports(context, emptyList())
            return emptyList()
        }

        return try {
            val jsonArray = JSONArray(stored)
            val loadedReports = buildList {
                for (index in 0 until jsonArray.length()) {
                    add(reportFromJson(jsonArray.getJSONObject(index)))
                }
            }.sortedByDescending { it.id }

            val cleanedReports = loadedReports.filterNot { it.isLegacySeedReport() }
            if (cleanedReports.size != loadedReports.size) {
                saveReports(context, cleanedReports)
            }

            cleanedReports
        } catch (_: Exception) {
            saveReports(context, emptyList())
            emptyList()
        }
    }

    private fun saveReports(context: Context, reports: List<Report>) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        reports.forEach { report ->
            jsonArray.put(reportToJson(report))
        }
        prefs.edit().putString(KEY_REPORTS, jsonArray.toString()).apply()
    }

    private fun reportToJson(report: Report): JSONObject {
        return JSONObject().apply {
            put("id", report.id)
            put("title", report.title)
            put("category", report.category)
            put("location", report.location)
            put("bairro", report.bairro)
            put("date", report.date)
            put("status", report.status.name)
            put("description", report.description)
            put("priority", report.priority.name)
            put("photoUri", report.photoUri ?: JSONObject.NULL)
            put("latitude", report.latitude ?: JSONObject.NULL)
            put("longitude", report.longitude ?: JSONObject.NULL)
        }
    }

    private fun reportFromJson(json: JSONObject): Report {
        return Report(
            id = json.getInt("id"),
            title = json.getString("title"),
            category = json.getString("category"),
            location = json.getString("location"),
            bairro = json.getString("bairro"),
            date = json.getString("date"),
            status = ReportStatus.valueOf(json.getString("status")),
            description = json.optString("description", ""),
            priority = ReportPriority.valueOf(json.optString("priority", ReportPriority.MEDIA.name)),
            photoUri = json.optString("photoUri").takeIf { it.isNotBlank() && it != "null" },
            latitude = if (json.isNull("latitude")) null else json.optDouble("latitude"),
            longitude = if (json.isNull("longitude")) null else json.optDouble("longitude")
        )
    }

    private fun Report.isLegacySeedReport(): Boolean {
        return legacySeedReports.any { legacy ->
            id == legacy.id &&
                title == legacy.title &&
                category == legacy.category &&
                location == legacy.location &&
                bairro == legacy.bairro &&
                date == legacy.date &&
                status == legacy.status &&
                description == legacy.description &&
                priority == legacy.priority &&
                latitude == legacy.latitude &&
                longitude == legacy.longitude
        }
    }
}
