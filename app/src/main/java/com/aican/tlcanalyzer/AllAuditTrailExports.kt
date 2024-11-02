package com.aican.tlcanalyzer

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.aican.tlcanalyzer.adapterClasses.PdfAdapter
import com.aican.tlcanalyzer.interfaces.OnPDFSelectListener
import com.aican.tlcanalyzer.utils.Source
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File

class AllAuditTrailExports : AppCompatActivity(), OnPDFSelectListener {

    lateinit var recView: RecyclerView
    lateinit var pdfAdapter: PdfAdapter
    lateinit var pdfList: ArrayList<File>
    var spanCount = 1
    private val STORAGE_PERMISSION_REQUEST_CODE = 1
    lateinit var back: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_audit_trail_exports)

        recView = findViewById(R.id.recView)

        supportActionBar?.hide()

        back = findViewById(R.id.back)
        back.setOnClickListener {
            finish()
        }


        //
        val fileDestination =
            File(getExternalFilesDir(null).toString() + "/Users Activity Files")
        if (!fileDestination.exists()) {
            fileDestination.mkdirs()
        }

        if (Build.VERSION.SDK_INT >= 33) {
            val permissions = arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.READ_MEDIA_AUDIO
            )

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permissions are not granted, request them
                ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    STORAGE_PERMISSION_REQUEST_CODE
                )
            } else {
                displayPDF()
            }

        } else {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permissions are not granted, request them
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    STORAGE_PERMISSION_REQUEST_CODE
                )
            } else {
                displayPDF()
            }
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun runtimePermission() {
        Dexter.withContext(this@AllAuditTrailExports).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                    displayPDF()
                }

                override fun onPermissionRationaleShouldBeShown(
                    list: List<PermissionRequest>,
                    permissionToken: PermissionToken
                ) {
                    permissionToken.continuePermissionRequest()
                }
            }).withErrorListener { error ->
                Toast.makeText(
                    applicationContext,
                    "There was an error : $error",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.e("Dexter", "There was an error: $error")
            }.check()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            // Check if the permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayPDF()
                // Permission is granted, proceed with using external storage
                // Your code for accessing external storage goes here
            } else {
                Source.toast(this@AllAuditTrailExports, "We need permission to access these files")
                // Permission is denied, handle accordingly (e.g., show a message or disable functionality)
            }
        }
    }


    fun findPDF(file: File?): ArrayList<File>? {
        val arrayList = ArrayList<File>()
        val files = file!!.listFiles()
        for (singleFile in files) {
            if (singleFile.isDirectory && !singleFile.isHidden) {
                arrayList.addAll(findPDF(singleFile)!!)
            } else {
                if (singleFile.name.endsWith(".pdf")) {
                    arrayList.add(singleFile)
                }
            }
        }
        return arrayList
    }



    fun displayPDF() {
        val layoutManager = GridLayoutManager(this, spanCount)
        recView = findViewById<View>(R.id.recView) as RecyclerView
        recView.setHasFixedSize(true)
//        recyclerView.layoutManager = layoutManager
        pdfList = ArrayList<File>()
        //        pdfList.addAll(findPDF(Environment.getExternalStoragePublicDirectory("Khan Sir App")));
        pdfList.addAll(findPDF(applicationContext.getExternalFilesDir(getString(R.string.folderLocation2)))!!)
        pdfAdapter = PdfAdapter(this, pdfList, this)
        recView.adapter = pdfAdapter
        pdfAdapter.notifyDataSetChanged()

    }


    override fun onPDFSelected(file: File?, fileName: String, position: Int) {
        val intent = Intent(this@AllAuditTrailExports, PDFActivity::class.java)
        intent.putExtra("path", file!!.absolutePath)
        intent.putExtra("flag", "n")
        intent.putExtra("fileName", fileName);
        startActivity(intent)
    }

    override fun onDelete(file: File?, position: Int) {
        val fearFiles = File(file!!.absolutePath)
        val deleted = fearFiles.delete()
        if (deleted) {
            Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error - Please try again", Toast.LENGTH_SHORT).show()
        }
        displayPDF()
    }

    override fun inExternalApp(file: File?, context: Context?) {
        try {
            val target = Intent(Intent.ACTION_VIEW)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    target.setDataAndType(FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file), "*/*");
                if (file!!.name.endsWith(".pdf") || file!!.name.endsWith(".pptx")) {
                    target.setDataAndType(
                        FileProvider.getUriForFile(
                            context!!,
                            BuildConfig.APPLICATION_ID + ".provider",
                            file
                        ), "application/pdf"
                    )
                }
                if (file!!.name.endsWith(".png") || file.name.endsWith(".jpeg") || file.name.endsWith(
                        ".jpg"
                    )
                ) {
                    target.setDataAndType(
                        FileProvider.getUriForFile(
                            context!!,
                            BuildConfig.APPLICATION_ID + ".provider",
                            file
                        ), "image/*"
                    )
                }
                if (file.name.endsWith(".docx") || file.name.endsWith(".docs") || file.name.endsWith(
                        ".txt"
                    )
                ) {
                    target.setDataAndType(
                        FileProvider.getUriForFile(
                            context!!,
                            BuildConfig.APPLICATION_ID + ".provider",
                            file
                        ), "text/plain"
                    )
                }
                if (file.name.endsWith(".sce")) {
                    target.setDataAndType(
                        FileProvider.getUriForFile(
                            context!!,
                            BuildConfig.APPLICATION_ID + ".provider",
                            file
                        ), "*/*"
                    )
                }


//                target.setDataAndType(FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file), "application/pdf");
//                target.setDataAndType(FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file), "*/*");
//                target.setDataAndType(FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file), "image/*");
//                Toast.makeText(context, "SDK N : "+FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file), Toast.LENGTH_SHORT).show();
            } else {
                target.setDataAndType(Uri.fromFile(file), "*/*")
                //                target.setDataAndType(Uri.fromFile(file), "image/*");
//                target.setDataAndType(Uri.fromFile(file), "application/pdf");
//                Toast.makeText(context, "SDK !N", Toast.LENGTH_SHORT).show();
            }
            target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            target.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            val intent = Intent.createChooser(target, "Open File")
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No apk found to open this file", Toast.LENGTH_SHORT).show()
                // Instruct the user to install a PDF reader here, or something
            }
        } catch (e: Exception) {
            Log.d("ErrorGot", e.message!!)
        }
    }


}