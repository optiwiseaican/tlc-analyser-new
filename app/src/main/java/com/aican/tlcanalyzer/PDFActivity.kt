package com.aican.tlcanalyzer


import android.app.ProgressDialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import java.io.File

class PDFActivity : AppCompatActivity() {

    lateinit var pdfView: PDFView
    var filePath: String? = null
    var fileLink: String? = null
    var flag: String? = null
    var title: String? = null
    var chNo: String? = null
    lateinit var progressDialog: ProgressDialog
    var file: File? = null
    var time: String? = null
    var shortTime: String? = null
    var fileName: String? = null
    lateinit var adsTimer: TextView
    lateinit var bannerAdView: LinearLayout
    var timer3: CountDownTimer? = null
    val handler33 = Handler()
    var runnable33: Runnable? = null
    lateinit var back: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfactivity)

        supportActionBar!!.hide()


//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//        supportActionBar!!.setDisplayShowHomeEnabled(true)

        back = findViewById(R.id.back)

        back.setOnClickListener {
            finish()
        }

        pdfView = findViewById<View>(R.id.pdfView) as PDFView
        filePath = intent.getStringExtra("path")
        fileLink = intent.getStringExtra("link")
        flag = intent.getStringExtra("flag")
        title = intent.getStringExtra("title")
        chNo = intent.getStringExtra("chNo")


        val file = File(filePath!!)
        val path = Uri.fromFile(file)
//        setTitle(intent.getStringExtra("fileName"))
        pdfView.fromUri(path)
            .scrollHandle(DefaultScrollHandle(this@PDFActivity)).load()


    }


    private fun progressDialog() {
        progressDialog = ProgressDialog(this@PDFActivity)
        progressDialog.setMessage("Loading.....")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(false)
        progressDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE, "Cancel"
        ) { dialogInterface, i -> finish() }
        progressDialog.show()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
//        if (item.itemId == R.id.download) {
//            Toast.makeText(applicationContext, "Downloading....", Toast.LENGTH_SHORT).show()
//            downloadHandler.downloadFile(fileLink, "$fileName.pdf", this@PDFActivity, file)
//            return true
//        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        if (flag == "yk") {
//            val inflater = menuInflater
//            inflater.inflate(R.menu.menu_download, menu)
//        }
        return true
    }


    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onBackPressed() {
        super.onBackPressed()

    }


}