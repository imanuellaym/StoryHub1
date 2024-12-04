package com.example.storyhub.view.add

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.storyhub.R
import com.example.storyhub.data.result.ResultState
import com.example.storyhub.databinding.ActivityUploadBinding
import com.example.storyhub.utils.reduceFileImage
import com.example.storyhub.utils.uriToFile
import com.example.storyhub.view.MainViewModel
import com.example.storyhub.view.ViewModelFactory
import com.example.storyhub.view.main.MainActivity
import com.google.android.gms.location.LocationServices
import java.io.File

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private var selectedImageUri: Uri? = null
    private val viewModel: MainViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private var isUploading = false
    private var currentLat: Double? = null
    private var currentLon: Double? = null

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                displaySelectedImage()
            } else {
                showToast(getString(R.string.no_media_selected))
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                displaySelectedImage()
            } else {
                showToast(getString(R.string.camera_error))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        setupListeners()
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            title = getString(R.string.add_story)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupListeners() {
        binding.btnGallery.setOnClickListener { openGallery() }
        binding.btnCamera.setOnClickListener { openCamera() }
        binding.btnUpload.setOnClickListener { uploadStory() }
        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkLocationPermission()
            } else {
                currentLat = null
                currentLon = null
            }
        }
    }

    private fun openGallery() {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun openCamera() {
        selectedImageUri = createImageUri()
        if (selectedImageUri != null) {
            Log.d("Camera", "Launching camera with URI: $selectedImageUri")
            cameraLauncher.launch(selectedImageUri)
        } else {
            Log.e("Camera", "Failed to create URI for camera")
            showToast(getString(R.string.camera_error))
        }
    }

    private fun createImageUri(): Uri? {
        return try {
            val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            FileProvider.getUriForFile(this, "${packageName}.provider", file)
        } catch (e: Exception) {
            Log.e("Create URI", "Error creating URI: ${e.message}")
            null
        }
    }

    private fun displaySelectedImage() {
        selectedImageUri?.let {
            binding.ivPreview.setImageURI(it)
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLat = location.latitude
                    currentLon = location.longitude
                    Log.d("UploadActivity", "Location: $currentLat, $currentLon")
                } else {
                    showToast(getString(R.string.location_unavailable))
                }
            }.addOnFailureListener {
                Log.e("UploadActivity", "Failed to fetch location: ${it.message}")
            }
        } catch (e: SecurityException) {
            Log.e("UploadActivity", "Permission not granted: ${e.message}")
        }
    }

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                showToast(getString(R.string.location_permission_denied))
                binding.switchLocation.isChecked = false
            }
        }

    private fun uploadStory() {
        val description = binding.descEditText.text.toString().trim()

        if (selectedImageUri == null) {
            showToast(getString(R.string.image_required))
            return
        }

        if (description.isEmpty()) {
            showToast(getString(R.string.description_required))
            return
        }

        val imageFile = uriToFile(selectedImageUri!!, this)?.reduceFileImage()
        if (imageFile != null) {
            submitStory(imageFile, description)
        } else {
            showToast(getString(R.string.file_error))
        }
    }

    private fun submitStory(file: File, description: String) {
        isUploading = true
        viewModel.retrieveUserSession().observe(this) { session ->
            val token = session.token
            viewModel.submitImage(token, file, description).observe(this) { result ->
                when (result) {
                    is ResultState.Loading -> setLoadingState(true)
                    is ResultState.Success -> handleUploadSuccess()
                    is ResultState.Error -> handleUploadError(result.exception.message)
                }
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        isUploading = isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun handleUploadSuccess() {
        showToast(getString(R.string.upload_success))
        setLoadingState(false)
        navigateToMain()
    }

    private fun handleUploadError(error: String?) {
        showToast(error ?: getString(R.string.upload_failed))
        setLoadingState(false)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (isUploading) {
            showCancelUploadDialog()
        } else {
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun showCancelUploadDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.cancel_upload_title))
        builder.setMessage(getString(R.string.cancel_upload_message))
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            isUploading = false
            navigateToMainActivity()
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.create().show()
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 101
    }
}
