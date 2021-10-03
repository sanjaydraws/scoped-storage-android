package com.sanjay.scopedstorage

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sanjay.scopedstorage.androidstorage.InternalStoragePhoto
import com.sanjay.scopedstorage.androidstorage.InternalStoragePhotoAdapter
import com.sanjay.scopedstorage.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    var binding:ActivityMainBinding? = null

    private var internalStoragePhotoAdapter :InternalStoragePhotoAdapter? = null
    private var readPermissionGranted:Boolean = false
    private var writePermissionGranted:Boolean = false
    private lateinit var permissionLauncher:ActivityResultLauncher<Array<String>> // for multiple permission
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding?.apply {
            setContentView(this.root)
        }
        internalStoragePhotoAdapter = InternalStoragePhotoAdapter {
            val isDeletionSuccessful = deletePhotoFromInternalStorage(it.name)
            if(isDeletionSuccessful) {
                loadPhotosFromInternalStorageIntoRecyclerView()
                Toast.makeText(this, "Photo successfully deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to delete photo", Toast.LENGTH_SHORT).show()
            }
        }

        // to get activity result
        val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            val isPrivate = binding?.switchPrivate?.isChecked // save photos to private
            if(isPrivate == true) {
                val isSavedSuccessfully = savePhotoToInternalStorage(UUID.randomUUID().toString(), it)
                if(isSavedSuccessfully) {
                    loadPhotosFromInternalStorageIntoRecyclerView()
                    Toast.makeText(this, "Photo saved successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding?.btnTakePhoto?.setOnClickListener {
            takePhoto?.launch()
        }


        setupInternalStorageRecyclerView()
        loadPhotosFromInternalStorageIntoRecyclerView()
    }
    private fun updateRequestPermission(){
        val hasReadPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionToRequest = mutableListOf<String>()

        if(!writePermissionGranted){
            permissionToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(!readPermissionGranted){
            permissionToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        // take permission
        if(permissionToRequest.isNotEmpty()){
            permissionLauncher.launch(permissionToRequest.toTypedArray())
        }
        
    }


    private fun setupInternalStorageRecyclerView() = binding?.rvPrivatePhotos?.apply {
        adapter = internalStoragePhotoAdapter
        layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
    }

    private fun loadPhotosFromInternalStorageIntoRecyclerView() {
        lifecycleScope.launch {
            val photos = loadPhotosFromInternalStorage()
            internalStoragePhotoAdapter?.submitList(photos)
        }
    }


    private fun deletePhotoFromInternalStorage(filename: String): Boolean {
        return try {
            deleteFile(filename)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    private suspend fun loadPhotosFromInternalStorage(): List<InternalStoragePhoto> {
        return withContext(Dispatchers.IO) {
            val files = filesDir.listFiles() // get files of internal storage
            files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.map {
                val bytes = it.readBytes() // get array of bytes
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) // save bytes into bitmap
                InternalStoragePhoto(it.name, bmp)  // save into data class
            } ?: listOf()
        }
    }
    private fun savePhotoToInternalStorage(filename: String, bmp: Bitmap): Boolean {
        // need output stream

        return try {
            openFileOutput("$filename.jpg", MODE_PRIVATE).use { stream ->
                if(!bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                    throw IOException("Couldn't save bitmap.")
                }
            }
            true
        } catch(e: IOException) {
            e.printStackTrace()
            false
        }
    }
    // output stream -> put  bytes of bitmap in file
    // input stream -> get bytes of bitmap in file
}