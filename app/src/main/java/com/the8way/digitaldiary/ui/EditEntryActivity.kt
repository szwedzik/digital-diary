package com.the8way.digitaldiary.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.the8way.digitaldiary.R
import com.the8way.digitaldiary.data.DiaryEntry
import com.the8way.digitaldiary.databinding.ActivityEditEntryBinding
import com.the8way.digitaldiary.utils.LocationUtils
import com.the8way.digitaldiary.utils.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class EditEntryActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityEditEntryBinding
    private lateinit var googleMap: GoogleMap

    @Inject
    lateinit var locationUtils: LocationUtils

    private val diaryEntryViewModel: DiaryEntryViewModel by viewModels()

    private lateinit var entry: DiaryEntry
    private var entryLocation: LatLng? = null

    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null
    private var isRecording = false
    private var mediaPlayer: MediaPlayer? = null

    private var isImageDeleted = false
    private var imageUri = ""

    private val captureImageLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            val uri = saveImageToUri(it)
            binding.imageView.setImageURI(uri)
            binding.imageView.visibility = View.VISIBLE
            binding.addImageButton.visibility = View.GONE
            binding.deleteImageButton.visibility = View.VISIBLE
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            contentResolver.openInputStream(it)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                binding.imageView.setImageBitmap(bitmap)
                binding.imageView.visibility = View.VISIBLE
                binding.addImageButton.visibility = View.GONE
                binding.deleteImageButton.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissions()

        val entryId = intent.getIntExtra("ENTRY_ID", -1)
        if (entryId != -1) {
            lifecycleScope.launch {
                entry = diaryEntryViewModel.getEntryById(entryId)
                entryLocation = LatLng(entry.latitude, entry.longitude)
                setupUI()
                val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this@EditEntryActivity)
            }
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults, this, {
        }, {
            Toast.makeText(this, R.string.perms_denied, Toast.LENGTH_SHORT).show()
        })
    }

    private fun setupUI() {
        binding.titleEditText.setText(entry.title)
        binding.contentEditText.setText(entry.content)
        if (entry.imageUri.isNotEmpty()) {
            try {
                val uri = Uri.parse(entry.imageUri)
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.imageView.setImageBitmap(bitmap)
                    binding.imageView.visibility = View.VISIBLE
                    binding.addImageButton.visibility = View.GONE
                    binding.deleteImageButton.visibility = View.VISIBLE
                }
            } catch (e: IOException) {
                Log.e("EditEntryActivity", "Failed to load image", e)
            }
        }

        if (entry.audioUri.isNotEmpty()) {
            binding.audioPlayerView.visibility = View.VISIBLE
            binding.audioPlayerView.setAudioPath(entry.audioUri)
            binding.addAudioButton.visibility = View.GONE
            binding.deleteAudioButton.visibility = View.VISIBLE
        } else {
            binding.audioPlayerView.visibility = View.GONE
            binding.addAudioButton.visibility = View.VISIBLE
            binding.deleteAudioButton.visibility = View.GONE
        }

        binding.createdTimeTextView.text = String.format(getString(R.string.created_at), SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(entry.createdTime)))

        entry.updatedTime?.let {
            binding.updatedTimeTextView.text = String.format(getString(R.string.updated_at), SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(it)))
            binding.updatedTimeTextView.visibility = View.VISIBLE
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.saveButton.setOnClickListener {
            saveEntry()
        }

        binding.addImageButton.setOnClickListener {
            showImagePickerOptions()
        }

        binding.deleteImageButton.setOnClickListener {
            binding.imageView.setImageBitmap(null)
            binding.imageView.visibility = View.GONE
            binding.addImageButton.visibility = View.VISIBLE
            binding.deleteImageButton.visibility = View.GONE
            isImageDeleted = true
        }

        binding.addAudioButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }

        binding.deleteAudioButton.setOnClickListener {
            deleteAudio()
        }

        binding.audioPlayerView.setOnClickListener {
            playAudio()
        }
    }

    private fun saveEntry() {
        val title = binding.titleEditText.text.toString()
        val content = binding.contentEditText.text.toString()

        if (title.isEmpty()) {
            binding.titleEditText.error = getString(R.string.title_required)
            return
        }

        if (content.isEmpty()) {
            binding.contentEditText.error = getString(R.string.content_required)
            return
        }

        if (isImageDeleted) {
            val file = File(Uri.parse(entry.imageUri).path)
            if (file.exists()) {
                file.delete()
            }
            imageUri = ""
        } else {
            imageUri = (binding.imageView.drawable as? BitmapDrawable)?.let { saveDrawableToUri(it) } ?: entry.imageUri
        }

        val updatedEntry = entry.copy(
            title = title,
            content = content,
            imageUri = imageUri,
            audioUri = audioFilePath ?: "",
            latitude = entryLocation?.latitude ?: 0.0,
            longitude = entryLocation?.longitude ?: 0.0,
            updatedTime = Date().time
        )

        diaryEntryViewModel.updateDiaryEntry(updatedEntry)
        finish()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        displaySavedLocation()
    }

    private fun displaySavedLocation() {
        lifecycleScope.launch {
            entryLocation?.let { location ->
                val address = locationUtils.getAddressFromLocation(location.latitude, location.longitude)
                binding.locationTextView.text = address
                googleMap.addMarker(MarkerOptions().position(location).title(address))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            }
        }
    }

    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            audioFilePath = "${externalCacheDir?.absolutePath}/diary_audio_${System.currentTimeMillis()}.3gp"
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(audioFilePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                try {
                    prepare()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                start()
                isRecording = true
                binding.addAudioButton.text = getString(R.string.audio_stop_capture)
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false
        binding.addAudioButton.visibility = View.GONE
        binding.deleteAudioButton.visibility = View.VISIBLE
        binding.audioPlayerView.setAudioPath(audioFilePath)
        binding.audioPlayerView.visibility = View.VISIBLE
    }

    private fun deleteAudio() {
        audioFilePath?.let {
            val file = File(it)
            if (file.exists()) {
                file.delete()
            }
        }
        binding.audioPlayerView.visibility = View.GONE
        audioFilePath = null
        entry.audioUri = ""
        binding.addAudioButton.visibility = View.VISIBLE
        binding.deleteAudioButton.visibility = View.GONE
    }

    private fun playAudio() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioFilePath ?: entry.audioUri)
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun saveImageToUri(bitmap: Bitmap): Uri {
        val file = File(externalCacheDir, "image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        return FileProvider.getUriForFile(this, "$packageName.provider", file)
    }

    private fun saveDrawableToUri(drawable: BitmapDrawable): String {
        val bitmap = drawable.bitmap
        val uri = saveImageToUri(bitmap)
        return uri.toString()
    }

    private fun showImagePickerOptions() {
        val options = arrayOf(R.string.img_picker_camera, R.string.img_picker_gallery)
            .map { getString(it) }
            .toTypedArray()
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(R.string.img_picker_title)
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> captureImageLauncher.launch(null)
                1 -> pickImageLauncher.launch("image/*")
            }
        }
        builder.show()
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private const val REQUEST_PERMISSIONS = 100
    }
}
