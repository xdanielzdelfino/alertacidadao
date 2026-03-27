package com.alertacidadao.app

import android.Manifest
import android.location.Address
import android.location.Geocoder
import android.content.pm.PackageManager
import android.net.Uri
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.alertacidadao.app.data.ReportRepository
import com.alertacidadao.app.model.Report
import com.alertacidadao.app.model.ReportPriority
import com.alertacidadao.app.model.ReportStatus
import com.alertacidadao.app.databinding.ActivityAddReportBinding
import com.alertacidadao.app.ui.setupSystemBars
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.File
import java.util.Locale

class AddReportActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_REPORT_ID = "extra_report_id"
    }

    private data class ResolvedPlace(
        val latitude: Double,
        val longitude: Double,
        val label: String,
        val neighborhood: String?
    )

    private lateinit var binding: ActivityAddReportBinding
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private var editingReportId: Int = -1
    private var editingReportDate: String = ""
    private var editingReportStatus: ReportStatus = ReportStatus.ABERTO
    private var currentPhotoUri: Uri? = null
    private var existingPhotoUri: String? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private var selectedLocationLabel: String? = null
    private var selectedNeighborhood: String? = null
    private var selectedPriority: ReportPriority = ReportPriority.MEDIA

    private val categories = listOf(
        "🚧 Buraco na via",
        "💡 Iluminação pública",
        "🗑️ Lixo irregular",
        "🌳 Árvore caída",
        "🚦 Sinalização",
        "🧱 Calçada danificada",
        "🌊 Esgoto / água",
        "🧭 Outros"
    )

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchCamera()
            } else {
                Toast.makeText(this, "Permissão da câmera negada", Toast.LENGTH_SHORT).show()
            }
        }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                fetchCurrentLocation()
            } else {
                Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
            }
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && currentPhotoUri != null) {
                binding.imageViewPhoto.setImageURI(currentPhotoUri)
                binding.imageViewPhoto.visibility = View.VISIBLE
                binding.textViewPhotoHint.text = "Foto anexada"
            } else {
                Toast.makeText(this, "Não foi possível capturar a foto", Toast.LENGTH_SHORT).show()
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                try {
                    // persiste permissão de leitura para acessar a imagem depois
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                    // ignora se o provedor não permitir persistência
                }
                currentPhotoUri = uri
                binding.imageViewPhoto.setImageURI(currentPhotoUri)
                binding.imageViewPhoto.visibility = View.VISIBLE
                binding.textViewPhotoHint.text = "Foto anexada"
            } else {
                Toast.makeText(this, "Nenhuma imagem selecionada", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSystemBars()

        binding = ActivityAddReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        editingReportId = intent.getIntExtra(EXTRA_REPORT_ID, -1)
        if (editingReportId > 0) {
            supportActionBar?.title = "Editar Relato"
            supportActionBar?.subtitle = "Fortaleza, CE"
        } else {
            supportActionBar?.title = "Novo Relato"
            supportActionBar?.subtitle = "Fortaleza, CE"
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        setupCategoryDropdown()
        setupPrioritySelection()
        if (editingReportId > 0) {
            loadReportForEdit(editingReportId)
            binding.buttonSave.text = "Salvar alterações"
        }

        binding.buttonCamera.setOnClickListener {
            // mostrar opção: Tirar foto ou Escolher da galeria
            val options = arrayOf("Tirar foto", "Escolher da galeria")
            AlertDialog.Builder(this)
                .setTitle("Adicionar imagem")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            if (hasCameraPermission()) {
                                launchCamera()
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                        1 -> {
                            // abrir picker do sistema (permite persistir permissão)
                            pickImageLauncher.launch(arrayOf("image/*"))
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        binding.buttonGps.setOnClickListener {
            if (hasLocationPermission()) {
                fetchCurrentLocation()
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        binding.buttonSave.setOnClickListener {
            saveReport()
        }
    }

    private fun setupCategoryDropdown() {
        val adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            categories
        )
        binding.autoCompleteCategory.setAdapter(adapter)
        binding.autoCompleteCategory.setOnClickListener {
            binding.autoCompleteCategory.showDropDown()
        }
    }

    private fun setupPrioritySelection() {
        binding.chipGroupPriority.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedPriority = when (checkedIds.firstOrNull()) {
                R.id.chipPriorityLow -> ReportPriority.BAIXA
                R.id.chipPriorityHigh -> ReportPriority.ALTA
                else -> ReportPriority.MEDIA
            }
        }
    }

    private fun launchCamera() {
        val photoUri = createPhotoUri()
        currentPhotoUri = photoUri
        takePictureLauncher.launch(photoUri)
    }

    private fun createPhotoUri(): Uri {
        val picturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: filesDir
        if (!picturesDir.exists()) {
            picturesDir.mkdirs()
        }
        val photoFile = File(picturesDir, "report_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(this, "$packageName.fileprovider", photoFile)
    }

    private fun fetchCurrentLocation() {
        if (!hasLocationPermission()) {
            binding.textViewGpsStatus.text = "Permissão de localização não concedida"
            return
        }
        binding.textViewGpsStatus.text = "Obtendo localização..."
        val tokenSource = CancellationTokenSource()
        try {
            fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        selectedLatitude = location.latitude
                        selectedLongitude = location.longitude
                        Thread {
                            val resolvedPlace = reverseGeocode(location.latitude, location.longitude)
                            runOnUiThread {
                                if (resolvedPlace != null) {
                                    selectedLocationLabel = resolvedPlace.label
                                    selectedNeighborhood = resolvedPlace.neighborhood
                                    binding.textViewGpsStatus.text = "GPS obtido: ${resolvedPlace.label}"
                                } else {
                                    selectedLocationLabel = String.format(
                                        Locale.getDefault(),
                                        "%.5f, %.5f",
                                        location.latitude,
                                        location.longitude
                                    )
                                    selectedNeighborhood = null
                                    binding.textViewGpsStatus.text = selectedLocationLabel
                                }
                            }
                        }.start()
                    } else {
                        binding.textViewGpsStatus.text = "Não foi possível obter o GPS"
                    }
                }
                .addOnFailureListener {
                    binding.textViewGpsStatus.text = "Não foi possível obter o GPS"
                }
        } catch (e: SecurityException) {
            binding.textViewGpsStatus.text = "Permissão de localização não concedida"
        }
    }

    private fun saveReport() {
        val title = binding.editTextTitle.text?.toString()?.trim().orEmpty()
        val category = binding.autoCompleteCategory.text?.toString()?.trim().orEmpty()
        val description = binding.editTextDescription.text?.toString()?.trim().orEmpty()

        binding.inputLayoutTitle.error = null
        binding.inputLayoutCategory.error = null

        if (title.isBlank()) {
            binding.inputLayoutTitle.error = "Informe o título"
            return
        }

        if (category.isBlank()) {
            binding.inputLayoutCategory.error = "Selecione a categoria"
            return
        }

        binding.buttonSave.isEnabled = false

        Thread {
            val finalDate = if (editingReportId > 0 && editingReportDate.isNotBlank()) {
                editingReportDate
            } else {
                ReportRepository.formatCurrentDate()
            }

            val finalStatus = if (editingReportId > 0) editingReportStatus else ReportStatus.ABERTO
            val finalLatitude = selectedLatitude
            val finalLongitude = selectedLongitude

            val locationLabel = selectedLocationLabel ?: when {
                finalLatitude != null && finalLongitude != null -> String.format(
                    Locale.getDefault(),
                    "%.5f, %.5f",
                    finalLatitude,
                    finalLongitude
                )
                else -> "Fortaleza, CE"
            }

            val neighborhood = selectedNeighborhood
                ?: if (finalLatitude != null && finalLongitude != null) {
                    "Fortaleza"
                } else {
                    "Centro"
                }

                val photoUri = currentPhotoUri?.toString() ?: existingPhotoUri
        val report = Report(
            id = if (editingReportId > 0) editingReportId else 0,
            title = title,
            category = category,
            location = locationLabel,
            bairro = neighborhood,
            date = finalDate,
            status = finalStatus,
            description = description,
            priority = selectedPriority,
            photoUri = photoUri,
            latitude = finalLatitude,
            longitude = finalLongitude
        )

        if (editingReportId > 0) {
            ReportRepository.update(this, report)
        } else {
            ReportRepository.add(this, report)
        }
        runOnUiThread {
            Toast.makeText(
                this,
                if (editingReportId > 0) "Relato atualizado com sucesso" else "Relato enviado com sucesso",
                        Toast.LENGTH_SHORT
                    ).show()
                setResult(RESULT_OK)
                finish()
            }
        }.start()
    }

        private fun loadReportForEdit(reportId: Int) {
            val report = ReportRepository.getById(this, reportId) ?: return

            binding.editTextTitle.setText(report.title)
            binding.autoCompleteCategory.setText(report.category, false)
            binding.editTextDescription.setText(report.description)

            selectedPriority = report.priority
            when (report.priority) {
                ReportPriority.BAIXA -> binding.chipGroupPriority.check(R.id.chipPriorityLow)
                ReportPriority.MEDIA -> binding.chipGroupPriority.check(R.id.chipPriorityMedium)
                ReportPriority.ALTA -> binding.chipGroupPriority.check(R.id.chipPriorityHigh)
            }

            editingReportDate = report.date
            editingReportStatus = report.status
            existingPhotoUri = report.photoUri
            selectedLatitude = report.latitude
            selectedLongitude = report.longitude
            selectedLocationLabel = report.location
            selectedNeighborhood = report.bairro

            binding.textViewGpsStatus.text = if (report.location.isNotBlank()) {
                "GPS salvo: ${report.location}"
            } else {
                "Aguardando permissão..."
            }

            if (!report.photoUri.isNullOrBlank()) {
                currentPhotoUri = Uri.parse(report.photoUri)
                binding.imageViewPhoto.visibility = View.VISIBLE
                binding.textViewPhotoHint.text = "Foto anexada"
                binding.imageViewPhoto.setImageURI(currentPhotoUri)
            }
        }

    private fun reverseGeocode(latitude: Double, longitude: Double): ResolvedPlace? {
        if (!Geocoder.isPresent()) return null

        return try {
            val geocoder = Geocoder(this, Locale("pt", "BR"))
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1).orEmpty()
            val address = addresses.firstOrNull() ?: return null
            address.toResolvedPlace()
        } catch (_: Exception) {
            null
        }
    }

    private fun Address.toResolvedPlace(): ResolvedPlace {
        val displayLabel = listOfNotNull(
            getAddressLine(0)?.takeIf { it.isNotBlank() },
            featureName?.takeIf { it.isNotBlank() },
            subLocality?.takeIf { it.isNotBlank() },
            locality?.takeIf { it.isNotBlank() },
            adminArea?.takeIf { it.isNotBlank() }
        ).firstOrNull() ?: "Localização encontrada"

        val neighborhood = listOfNotNull(
            subLocality?.takeIf { it.isNotBlank() },
            locality?.takeIf { it.isNotBlank() },
            subAdminArea?.takeIf { it.isNotBlank() },
            adminArea?.takeIf { it.isNotBlank() }
        ).firstOrNull()

        return ResolvedPlace(latitude, longitude, displayLabel, neighborhood)
    }

    private fun hasCameraPermission(): Boolean =
        androidx.core.content.ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasLocationPermission(): Boolean =
        androidx.core.content.ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}