package com.aican.tlcanalyzer.adapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aican.tlcanalyzer.NewImageAnalysis
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.dataClasses.SplitData
import com.aican.tlcanalyzer.database.DatabaseHelper
import com.aican.tlcanalyzer.dialog.AuthDialog
import java.io.File

class SplitMainImageAdtr(
    val context: Context,
    val arrayList: ArrayList<SplitData>,
    val databaseHelper: DatabaseHelper,
    val id: String,
    val projectName: String,
    val projectDescription: String,
    val tableName: String,
    val type: String,
    val projectTimeStamp: String,
    val projectNumber: String,
    val splitId: String
) : RecyclerView.Adapter<SplitMainImageAdtr.ViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mainImage = itemView.findViewById<ImageView>(R.id.mainImage)
        val mainImageName = itemView.findViewById<TextView>(R.id.mainImageName)
        val spottedView = itemView.findViewById<View>(R.id.spottedView)
        val sideMenu = itemView.findViewById<ImageView>(R.id.sideMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.custom_multiple_main_images, parent, false)
        return ViewHolder(view)
    }

    private var dir =
        File(
            ContextWrapper(context).externalMediaDirs[0],
            context.resources.getString(R.string.app_name) + id
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = arrayList[position]
        val outFile = File(dir, data.imagePath)
        val markImageFile = File(dir, "MARK_" + data.imagePath)
        println("MARK Image File name: " + markImageFile.path)
        val contOutFile = File(dir, "CONT" + data.id + ".png")
        val posId = position
        holder.mainImageName.text = data.imageName

        holder.spottedView.setBackgroundColor(context.resources.getColor(R.color.ai_red))

        if (markImageFile.exists()) {
//            Toast.makeText(context, "Yes exist", Toast.LENGTH_SHORT).show()
            holder.spottedView.setBackgroundColor(context.resources.getColor(R.color.green))
        }
        if (markImageFile.exists()) {
            val myBitmap = BitmapFactory.decodeFile(markImageFile.absolutePath)
            holder.mainImage.setImageBitmap(myBitmap)

        } else {
            val myBitmap = BitmapFactory.decodeFile(outFile.absolutePath)

            holder.mainImage.setImageBitmap(myBitmap)
//            Source.toast(context, "Not Exist")
        }
        var hour = data.hour

//        holder.itemView.setOnClickListener {
//
//            AuthDialog.projectType = "Split"
//            AuthDialog.projectName = projectName + " -> " + data.imageName
//            AuthDialog.projectID = id
//
////            val intent = Intent(context, ImageAnalysis::class.java)
//            val intent = Intent(context, NewImageAnalysis::class.java)
//            intent.putExtra("positionOf", position.toString())
//            intent.putExtra("w", "split")
//            intent.putExtra("mtype", "mainImg")
//            intent.putExtra("hour", hour)
//            intent.putExtra("img_path", outFile.path)
//            intent.putExtra("projectName", projectName)
//            intent.putExtra("splitProjectName", data.imageName)
//            intent.putExtra("projectDescription", data.description)
//            intent.putExtra("projectImage", data.imagePath)
//            intent.putExtra("projectNumber", projectNumber)
//            intent.putExtra("splitId", splitId)
//            if (type == "multi") {
//                intent.putExtra("type", "multi")
//            } else {
//                intent.putExtra("type", "multi")
//            }
//            intent.putExtra("imageName", data.imageName)
//            intent.putExtra("timeStamp", projectTimeStamp)
//            intent.putExtra("tableName", tableName)
//            intent.putExtra("roiTableID", data.roiTableID)
//            intent.putExtra("thresholdVal", data.thresholdVal)
//            intent.putExtra("numberOfSpots", data.noOfSpots)
//            intent.putExtra("id", id)
//            intent.putExtra("pid", data.id)
//            intent.putExtra("volumePlotTableID", data.volumePlotTableID)
//            intent.putExtra("intensityPlotTableID", data.intensityPlotTableID)
//            intent.putExtra("plotTableID", data.plotTableID)
//            intent.putExtra("rmSpot", data.rmSpot)
//            intent.putExtra("finalSpot", data.finalSpot)
//            context.startActivity(intent)
//
//
//        }

        holder.itemView.setOnClickListener {
            val dialog =
                AlertDialog.Builder(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            val inflater = LayoutInflater.from(context)
            val dialogView = inflater.inflate(R.layout.dialog_fullscreen_image, null)
            val fullScreenImageView = dialogView.findViewById<ImageView>(R.id.fullscreenImageView)
            val closeDialog = dialogView.findViewById<ImageView>(R.id.closeDialog)
            val imageNameText = dialogView.findViewById<TextView>(R.id.imageNameText)

            imageNameText.setText(data.imageName)
            // Load the image into the ImageView
            if (markImageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(markImageFile.absolutePath)
                fullScreenImageView.setImageBitmap(bitmap)
            } else {
                fullScreenImageView.setImageResource(R.drawable.no_internet) // Add a default placeholder
            }

            val alertDialog = dialog.setView(dialogView).create()

            // Close the dialog when the close button is clicked
            closeDialog.setOnClickListener { alertDialog.dismiss() }

            alertDialog.show()
        }


        holder.sideMenu.setOnClickListener {
            val popup = PopupMenu(context, holder.sideMenu)
            //inflating menu from xml resource
            popup.inflate(R.menu.delete_menu)
            //adding click listener
            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    return when (item.getItemId()) {

                        R.id.delete_this -> {
                            deleteMainImage(context)
                            true
                        }

                        else -> false
                    }
                }
            })
            //displaying the popup
            popup.show()
        }

    }

    fun deleteMainImage(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Main Image")
        builder.setMessage("Are you sure you want to delete this main image? All corresponding split images will also be deleted.")

        builder.setPositiveButton("Delete") { dialog, which ->
            // Perform deletion
            deleteMainImageFromDatabase()
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun deleteMainImageFromDatabase() {

    }

    override fun getItemCount(): Int {
        return arrayList.size
    }
}