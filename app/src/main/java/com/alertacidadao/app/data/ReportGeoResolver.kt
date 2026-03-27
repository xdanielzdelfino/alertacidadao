package com.alertacidadao.app.data

import android.content.Context
import android.location.Geocoder
import com.alertacidadao.app.model.Report
import org.osmdroid.util.GeoPoint
import java.util.Collections
import java.util.Locale

object ReportGeoResolver {

    private val defaultCenterPoint = GeoPoint(-3.73139, -38.5434)

    private val fallbackByNeighborhood = mapOf(
        "meireles" to GeoPoint(-3.7270, -38.4963),
        "aldeota" to GeoPoint(-3.7442, -38.5064),
        "varjota" to GeoPoint(-3.7285, -38.4899),
        "papicu" to GeoPoint(-3.7593, -38.4804),
        "água fria" to GeoPoint(-3.7942, -38.4929),
        "agua fria" to GeoPoint(-3.7942, -38.4929),
        "centro" to GeoPoint(-3.7275, -38.5438),
        "praia de iracema" to GeoPoint(-3.7202, -38.5230),
        "cocó" to GeoPoint(-3.7608, -38.4800),
        "coco" to GeoPoint(-3.7608, -38.4800)
    )

    private val coordinatePattern = Regex("(-?\\d+(?:\\.\\d+)?)\\s*[,; ]\\s*(-?\\d+(?:\\.\\d+)?)")
    private val geocodeCache = Collections.synchronizedMap(mutableMapOf<String, GeoPoint>())

    fun defaultCenter(): GeoPoint = defaultCenterPoint

    fun coordinatesFor(context: Context, report: Report): GeoPoint {
        val explicitLatitude = report.latitude
        val explicitLongitude = report.longitude
        if (explicitLatitude != null && explicitLongitude != null) {
            return GeoPoint(explicitLatitude, explicitLongitude)
        }

        parseCoordinates(report.location)?.let { return it }
        parseCoordinates(report.bairro)?.let { return it }

        geocodeReportLocation(context, report)?.let { return it }

        val key = normalizedKey(report.bairro)
        return fallbackByNeighborhood[key] ?: defaultCenterPoint
    }

    private fun geocodeReportLocation(context: Context, report: Report): GeoPoint? {
        if (!Geocoder.isPresent()) return null

        val candidates = listOf(
            report.location,
            report.bairro,
            listOf(report.location, report.bairro)
                .filter { it.isNotBlank() }
                .joinToString(", ")
                .takeIf { it.isNotBlank() }
        ).filterNotNull()

        val geocoder = Geocoder(context.applicationContext, Locale("pt", "BR"))
        return candidates.asSequence()
            .mapNotNull { candidate -> geocodeCandidate(geocoder, candidate) }
            .firstOrNull()
    }

    private fun geocodeCandidate(geocoder: Geocoder, candidate: String): GeoPoint? {
        val cacheKey = normalizedKey(candidate)
        if (cacheKey.isBlank()) return null

        geocodeCache[cacheKey]?.let { return it }

        return try {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(candidate, 1).orEmpty()
            val address = addresses.firstOrNull() ?: return null
            GeoPoint(address.latitude, address.longitude).also {
                geocodeCache[cacheKey] = it
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseCoordinates(value: String?): GeoPoint? {
        if (value.isNullOrBlank()) return null

        val match = coordinatePattern.find(value) ?: return null
        val latitude = match.groupValues[1].toDoubleOrNull() ?: return null
        val longitude = match.groupValues[2].toDoubleOrNull() ?: return null
        return GeoPoint(latitude, longitude)
    }

    private fun normalizedKey(value: String): String = value.trim().lowercase(Locale.getDefault())
}
