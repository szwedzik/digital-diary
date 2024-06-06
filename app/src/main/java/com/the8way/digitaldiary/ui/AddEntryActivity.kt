package com.the8way.digitaldiary.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.the8way.digitaldiary.DiaryApplication
import com.the8way.digitaldiary.R
import com.the8way.digitaldiary.data.DiaryEntry
import com.the8way.digitaldiary.databinding.ActivityAddEntryBinding
import com.the8way.digitaldiary.utils.LocationUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.Date

@AndroidEntryPoint
class AddEntryActivity : AppCompatActivity() {

    private val diaryEntryViewModel: DiaryEntryViewModel by viewModels {
        DiaryEntryViewModelFactory((application as DiaryApplication).database.diaryEntryDao())
    }

    private lateinit var binding: ActivityAddEntryBinding

    private var currentLocation: Location? = null
    private var imageUri: Uri? = null
    private var audioUri: Uri? = null
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false

    private val captureImageLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val uri = saveImageToUri(it)
            imageUri = uri
            binding.imageView.apply {
                setImageURI(uri)
                visibility = View.VISIBLE
            }
            binding.deleteImageButton.visibility = View.VISIBLE
            binding.captureImageButton.visibility = View.GONE
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri = it
            binding.imageView.apply {
                setImageURI(it)
                visibility = View.VISIBLE
            }
            binding.deleteImageButton.visibility = View.VISIBLE
            binding.captureImageButton.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()

        binding.captureImageButton.setOnClickListener {
            showImagePickerOptions()
        }

        binding.deleteImageButton.setOnClickListener {
            imageUri = null
            binding.imageView.visibility = View.GONE
            binding.deleteImageButton.visibility = View.GONE
            binding.captureImageButton.visibility = View.VISIBLE
        }

        binding.captureAudioButton.setOnClickListener {
            startRecording()
        }

        binding.stopCaptureAudioButton.setOnClickListener {
            stopRecording()
        }

        binding.deleteAudioButton.setOnClickListener {
            audioUri = null
            binding.audioPlayerView.visibility = View.GONE
            binding.deleteAudioButton.visibility = View.GONE
            binding.captureAudioButton.visibility = View.VISIBLE
        }

        binding.saveEntryButton.setOnClickListener {
            saveDiaryEntry()
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions()
        } else {
            getLocation()
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ),
            REQUEST_PERMISSIONS
        )
    }

    private fun getLocation() {
        val locationProvider = LocationUtils(this)
        lifecycleScope.launch {
            currentLocation = locationProvider.getCurrentLocation()
            if (currentLocation == null) {
                Toast.makeText(this@AddEntryActivity, R.string.location_unable, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToUri(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "diary_${System.currentTimeMillis()}", null)
        return Uri.parse(path)
    }

    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            audioUri = Uri.fromFile(File(externalCacheDir?.absolutePath, "diary_audio_${System.currentTimeMillis()}.3gp"))
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(audioUri?.path)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                try {
                    prepare()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                start()
                isRecording = true
                binding.captureAudioButton.visibility = View.GONE
                binding.stopCaptureAudioButton.visibility = View.VISIBLE
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
        if (isRecording) {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            binding.captureAudioButton.visibility = View.GONE
            binding.stopCaptureAudioButton.visibility = View.GONE
            binding.deleteAudioButton.visibility = View.VISIBLE
            binding.audioPlayerView.apply {
                setAudioPath(audioUri?.path)
                visibility = View.VISIBLE
            }
        }
    }

    private fun saveDiaryEntry() {
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

        val diaryEntry = DiaryEntry(
            title = title,
            content = content,
            imageUri = imageUri?.toString().orEmpty(),
            audioUri = audioUri?.toString().orEmpty(),
            latitude = currentLocation?.latitude ?: 0.0,
            longitude = currentLocation?.longitude ?: 0.0,
            createdTime = Date().time,
            updatedTime = null
        )

        diaryEntryViewModel.addDiaryEntry(diaryEntry)
        Toast.makeText(this, R.string.diary_entry_saved, Toast.LENGTH_SHORT).show()
        finish()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                getLocation()
            } else {
                if (grantResults.any { it == PackageManager.PERMISSION_DENIED }) {
                    val builder = android.app.AlertDialog.Builder(this)
                    builder.setTitle(R.string.perms_required)
                    builder.setMessage(R.string.perms_required_desc)
                    builder.setPositiveButton(R.string.perms_required_grant) { _, _ ->
                        requestPermissions()
                    }
                    builder.setNegativeButton(R.string.perms_required_deny) { dialog, _ ->
                        dialog.dismiss()
                        Toast.makeText(this, R.string.perms_denied, Toast.LENGTH_SHORT).show()
                    }
                    builder.show()
                } else {
                    Toast.makeText(this, R.string.perms_denied, Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startRecording()
            } else {
                Toast.makeText(this, R.string.perms_audio_denied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_PERMISSIONS = 1
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 2
    }
}
