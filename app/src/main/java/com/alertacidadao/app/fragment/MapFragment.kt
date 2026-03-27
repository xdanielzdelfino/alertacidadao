package com.alertacidadao.app.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import com.alertacidadao.app.ReportDetailActivity
import com.alertacidadao.app.R
import com.alertacidadao.app.data.ReportGeoResolver
import com.alertacidadao.app.data.ReportRepository
import com.alertacidadao.app.model.ReportStatus
import com.alertacidadao.app.model.markerRes
import com.alertacidadao.app.databinding.FragmentMapBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.osmdroid.api.IMapController
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.math.max

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(requireActivity()) }
    private var userLocationMarker: Marker? = null
    
    private lateinit var mapView: MapView
    private lateinit var mapController: IMapController

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                fetchCurrentLocation()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMap()
        setupControls()
        ensureUserLocation()
    }

    private fun setupMap() {
        mapView = binding.mapView
        
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setUseDataConnection(true)
        mapView.setBuiltInZoomControls(false)
        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        
        mapController = mapView.controller
        mapController.setZoom(13.0)
        
        mapController.setCenter(ReportGeoResolver.defaultCenter())
        
        mapView.setMultiTouchControls(true)

        addReportMarkers()
    }

    private fun setupControls() {
        binding.btnZoomIn.setOnClickListener {
            mapView.controller.zoomIn()
        }
        
        binding.btnZoomOut.setOnClickListener {
            mapView.controller.zoomOut()
        }
        
        binding.btnLocation.setOnClickListener {
            ensureUserLocation(forcePermission = true)
        }
        
        binding.btnLayers.setOnClickListener {
            // TODO: implementar seleção de camadas
        }
    }

    private fun addReportMarkers() {
        val reports = ReportRepository.getAll(requireContext())
        val markerPoints = mutableListOf<GeoPoint>()

        mapView.overlays.removeAll { it is Marker && it !== userLocationMarker }

        reports.forEach { report ->
            val reportPoint = ReportGeoResolver.coordinatesFor(requireContext(), report)
            markerPoints.add(reportPoint)

            val marker = Marker(mapView).apply {
                position = reportPoint
                title = report.title
                snippet = "${report.location} • ${report.date}"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = ContextCompat.getDrawable(requireContext(), report.status.markerRes())
                setOnMarkerClickListener { _, _ ->
                    mapView.controller.animateTo(reportPoint)
                    startActivity(
                        Intent(requireContext(), ReportDetailActivity::class.java)
                            .putExtra(ReportDetailActivity.EXTRA_REPORT_ID, report.id)
                    )
                    true
                }
            }
            mapView.overlays.add(marker)
        }

        if (markerPoints.isNotEmpty()) {
            focusMarkers(markerPoints)
        }

        mapView.invalidate()
    }

    private fun ensureUserLocation(forcePermission: Boolean = false) {
        if (hasLocationPermission()) {
            fetchCurrentLocation()
            return
        }

        if (forcePermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun fetchCurrentLocation() {
        val tokenSource = CancellationTokenSource()
        try {
            fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val userPoint = GeoPoint(location.latitude, location.longitude)
                        showUserLocation(userPoint)
                        mapView.controller.setZoom(15.5)
                        mapView.controller.animateTo(userPoint)
                    }
                }
        } catch (e: SecurityException) {
            // Permissão não concedida, não faz nada
        }
    }

    private fun showUserLocation(point: GeoPoint) {
        userLocationMarker?.let { mapView.overlays.remove(it) }

        userLocationMarker = Marker(mapView).apply {
            position = point
            title = "Você está aqui"
            snippet = "Localização atual"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location_compass)
        }

        mapView.overlays.add(userLocationMarker)
        mapView.invalidate()
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun focusMarkers(points: List<GeoPoint>) {
        if (points.isEmpty()) {
            return
        }

        if (points.size == 1) {
            mapView.controller.setZoom(16.0)
            mapView.controller.setCenter(points.first())
            return
        }

        val centerLatitude = points.map { it.latitude }.average()
        val centerLongitude = points.map { it.longitude }.average()
        val latSpan = points.maxOf { it.latitude } - points.minOf { it.latitude }
        val lonSpan = points.maxOf { it.longitude } - points.minOf { it.longitude }
        val span = max(latSpan, lonSpan)

        val zoom = when {
            span < 0.005 -> 16.0
            span < 0.01 -> 15.0
            span < 0.03 -> 14.0
            span < 0.06 -> 13.0
            span < 0.12 -> 12.0
            else -> 11.0
        }

        mapView.controller.setZoom(zoom)
        mapView.controller.setCenter(GeoPoint(centerLatitude, centerLongitude))
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) {
            binding.mapView.onResume()
            addReportMarkers()
        }
    }

    override fun onPause() {
        if (_binding != null) {
            binding.mapView.onPause()
        }
        super.onPause()
    }

    override fun onDestroyView() {
        if (_binding != null) {
            binding.mapView.onDetach()
        }
        super.onDestroyView()
        _binding = null
    }
}
