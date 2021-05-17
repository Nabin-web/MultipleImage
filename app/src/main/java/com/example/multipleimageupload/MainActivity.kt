package com.example.multipleimageupload

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.multipleimageupload.repository.imageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var btnAdd: Button
    private lateinit var Layout: LinearLayout
    private lateinit var btnSave: Button

    private var REQUEST_GALLERY_CODE = 0
    private var REQUEST_CAMERA_CODE = 1
    private var imageUrl: String? = null
    var count = 1
    private var images = mutableListOf<MultipartBody.Part>()
    private var imageList = mutableListOf<String>()
    lateinit var newView: ImageView



    val SELECT_FRONT_IMAGE_FROM_GALLERY_REQUEST_CODE = 999;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAdd = findViewById(R.id.btnAdd)
        btnSave = findViewById(R.id.btnSave)

        Layout = findViewById(R.id.myLayout)

        btnAdd.setOnClickListener {
            var count = 1
            newView = ImageView(this)
//            newView.setTag()
            Layout.addView(newView)
            newView.layoutParams.height= 300
            newView.layoutParams.width = 300

//            newView.x = 300F
//            newView.y = 500F
//            newView.setBackgroundColor(Color.MAGENTA)
            newView.setImageResource(R.drawable.ic_choose)
            newView.setOnClickListener {
                loadPopUpMenu(newView)
            }

            btnSave.setOnClickListener {
                uploadImage(imageList)
            }


        }
    }



    private fun loadPopUpMenu(newView: ImageView) {
        val popupMenu = PopupMenu(this, newView)
        popupMenu.menuInflater.inflate(R.menu.menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuCamera ->
                    openCamera()
                R.id.menuGallery ->
                    openGallery()
            }
            true
        }
        popupMenu.show()

    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"  //any type of image file
        startActivityForResult(intent, REQUEST_GALLERY_CODE)
    }
    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_CAMERA_CODE)
    }


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if (requestCode == SELECT_FRONT_IMAGE_FROM_GALLERY_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, SELECT_FRONT_IMAGE_FROM_GALLERY_REQUEST_CODE)
            } else {
                Toast.makeText(
                        applicationContext,
                        "You don't have to access file location",
                        Toast.LENGTH_SHORT
                ).show()
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_GALLERY_CODE && data != null) {
                val selectedImage = data.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                val contentResolver = contentResolver
                val cursor =
                        contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
                cursor!!.moveToFirst()
                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                imageUrl = cursor.getString(columnIndex)
                newView.setImageBitmap(BitmapFactory.decodeFile(imageUrl))
                cursor.close()
            } else if (requestCode == REQUEST_CAMERA_CODE && data != null) {
                val imageBitmap = data.extras?.get("data") as Bitmap
                val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val file = bitmapToFile(imageBitmap, "$timeStamp.jpg")
                val photoURI: Uri = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        file!!)

                data.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

                imageUrl = file!!.absolutePath
                imageList.add(imageUrl!!)  //add image name to mutable list
                newView.setImageBitmap(BitmapFactory.decodeFile(imageUrl))
            }
        }
    }

    private fun bitmapToFile(
            bitmap: Bitmap,
            fileNameToSave: String
    ): File? {
        var file: File? = null
        return try {
            file = File(
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                            .toString() + File.separator + fileNameToSave
            )
            file.createNewFile()
            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos) // YOU can also save it in JPEG
            val bitMapData = bos.toByteArray()
            //write the bytes in file
            val fos = FileOutputStream(file)

            fos.write(bitMapData)
            fos.flush()
            fos.close()
            file
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }



    private fun uploadImage(imageList: MutableList<String>) {
        for(i in imageList) {
            if (imageUrl != null) {
                val file = File(i!!)

                var extension = MimeTypeMap.getFileExtensionFromUrl(imageUrl)
                var MimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                val reqFile =
                        RequestBody.create(MediaType.parse(MimeType), file)
                var body =
                        MultipartBody.Part.createFormData("file", file.name, reqFile)
                images.add(body)

            }
        }


            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val imageRepo = imageRepository()
                    val response = imageRepo.uploadImage(images)
                    if (response.success == true) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Uploaded", Toast.LENGTH_SHORT)
                                .show()

                            images.clear()
                            imageList.clear()
                        }

                    }
                } catch (ex: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.d("Mero Error ", ex.localizedMessage)
                        Toast.makeText(this@MainActivity,
                                ex.localizedMessage,
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
