package com.alertacidadao.app

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.alertacidadao.app.data.ReportRepository
import com.alertacidadao.app.data.ReportGeoResolver
import com.alertacidadao.app.databinding.ActivityReportDetailBinding
import com.alertacidadao.app.model.Report
import com.alertacidadao.app.model.ReportStatus
import com.alertacidadao.app.model.backgroundRes
import com.alertacidadao.app.model.colorRes
import com.alertacidadao.app.model.label
import com.alertacidadao.app.model.markerRes
import com.alertacidadao.app.ui.setupSystemBars
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker

class ReportDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportDetailBinding
    private var currentReportId: Int = -1
    private var currentReport: Report? = null

    private val editReportLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadReport()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupSystemBars()

        binding = ActivityReportDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        currentReportId = intent.getIntExtra(EXTRA_REPORT_ID, -1)
        loadReport()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_report_detail, menu)
        for (index in 0 until menu.size()) {
            menu.getItem(index).icon?.mutate()?.setTint(Color.WHITE)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionEditReport -> {
                openEditReport()
                true
            }
            R.id.actionDeleteReport -> {
                confirmDeleteReport()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadReport() {
        val report = ReportRepository.getById(this, currentReportId)
        if (report == null) {
            finish()
            return
        }

        currentReport = report

        supportActionBar?.title = report.title
        supportActionBar?.subtitle = "Detalhes do relato"

        binding.textViewCategory.text = report.category
        binding.textViewTitle.text = report.title
        binding.textViewDate.text = "${report.bairro} • ${report.date}"
        binding.textViewDescription.text = report.description.ifBlank {
            "Sem descrição informada."
        }

        binding.chipStatus.text = report.status.label()
        binding.textViewStatus.text = report.status.label()
        binding.textViewStatus.visibility = android.view.View.VISIBLE

        val statusColor = getColor(report.status.colorRes())

        binding.chipStatus.setBackgroundResource(report.status.backgroundRes())
        binding.chipStatus.setTextColor(statusColor)
        binding.textViewStatus.setTextColor(statusColor)

        binding.textViewCategory.setBackgroundResource(R.drawable.bg_category_chip)
        binding.textViewCategory.setTextColor(getColor(R.color.primary_dark))

        setupDetailMap(report.status)
        showReportLocation(report)

        binding.cardLocation.setOnClickListener {
            openGoogleMaps(report)
        }

        if (!report.photoUri.isNullOrBlank()) {
            val uri = try { Uri.parse(report.photoUri) } catch (_: Exception) { null }
            if (uri != null) {
                binding.imageViewPhoto.visibility = View.VISIBLE
                binding.textViewNoPhoto.visibility = View.GONE
                try {
                    binding.imageViewPhoto.setImageURI(uri)
                } catch (e: Exception) {
                    // fallback: tentar abrir stream e decodificar imagem
                    try {
                        val `is` = contentResolver.openInputStream(uri)
                        if (`is` != null) {
                            val bmp = android.graphics.BitmapFactory.decodeStream(`is`)
                            `is`.close()
                            if (bmp != null) {
                                binding.imageViewPhoto.setImageBitmap(bmp)
                            } else {
                                binding.imageViewPhoto.visibility = View.GONE
                                binding.textViewNoPhoto.visibility = View.VISIBLE
                            }
                        } else {
                            binding.imageViewPhoto.visibility = View.GONE
                            binding.textViewNoPhoto.visibility = View.VISIBLE
                        }
                    } catch (ex: Exception) {
                        binding.imageViewPhoto.visibility = View.GONE
                        binding.textViewNoPhoto.visibility = View.VISIBLE
                    }
                }
            } else {
                binding.imageViewPhoto.visibility = View.GONE
                binding.textViewNoPhoto.visibility = View.VISIBLE
            }
        } else {
            binding.imageViewPhoto.visibility = View.GONE
            binding.textViewNoPhoto.visibility = View.VISIBLE
        }
    }

    private fun openEditReport() {
        val report = currentReport ?: return
        editReportLauncher.launch(
            Intent(this, AddReportActivity::class.java)
                .putExtra(AddReportActivity.EXTRA_REPORT_ID, report.id)
        )
    }

    private fun confirmDeleteReport() {
        val report = currentReport ?: return

        MaterialAlertDialogBuilder(this)
            .setTitle("Excluir relato")
            .setMessage("Tem certeza que deseja excluir \"${report.title}\"?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Excluir") { _, _ ->
                ReportRepository.delete(this, report.id)
                setResult(RESULT_OK)
                finish()
            }
            .show()
    }

    private fun setupDetailMap(status: ReportStatus) {
        binding.mapViewDetail.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapViewDetail.setUseDataConnection(true)
        binding.mapViewDetail.setBuiltInZoomControls(false)
        binding.mapViewDetail.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        binding.mapViewDetail.setMultiTouchControls(true)
        binding.mapViewDetail.controller.setZoom(16.0)
        binding.mapViewDetail.isClickable = true
        binding.mapViewDetail.isFocusable = true
        binding.mapViewDetail.setOnTouchListener { parent, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    parent.parent?.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    parent.parent?.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }

        binding.mapViewDetail.tag = status.markerRes()
    }

    private fun showReportLocation(report: com.alertacidadao.app.model.Report) {
        val reportPoint = ReportGeoResolver.coordinatesFor(this, report)

        binding.mapViewDetail.controller.setCenter(reportPoint)
        binding.mapViewDetail.overlays.clear()

        val marker = Marker(binding.mapViewDetail).apply {
            position = reportPoint
            title = report.title
            snippet = report.location
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = ContextCompat.getDrawable(this@ReportDetailActivity, binding.mapViewDetail.tag as Int)
        }

        binding.mapViewDetail.overlays.add(marker)
        binding.mapViewDetail.invalidate()
    }

    private fun openGoogleMaps(report: com.alertacidadao.app.model.Report) {
        val coordinates = ReportGeoResolver.coordinatesFor(this, report)
        val label = Uri.encode(report.title)
        val geoUri = Uri.parse("geo:${coordinates.latitude},${coordinates.longitude}?q=${coordinates.latitude},${coordinates.longitude}($label)")

        val mapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        val chooser = Intent.createChooser(mapsIntent, "Abrir navegação")

        try {
            startActivity(chooser)
        } catch (_: Exception) {
            startActivity(
                Intent(Intent.ACTION_VIEW, geoUri)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapViewDetail.onResume()
    }

    override fun onPause() {
        binding.mapViewDetail.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        binding.mapViewDetail.onDetach()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_REPORT_ID = "extra_report_id"
    }
}