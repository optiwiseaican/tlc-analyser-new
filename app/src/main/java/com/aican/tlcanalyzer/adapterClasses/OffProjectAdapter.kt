package com.aican.tlcanalyzer.adapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aican.tlcanalyzer.NewImageAnalysis
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.SplitImage
import com.aican.tlcanalyzer.dataClasses.ProjectOfflineData
import com.aican.tlcanalyzer.database.DatabaseHelper
import com.aican.tlcanalyzer.interfaces.refreshProjectArrayList
import com.aican.tlcanalyzer.utils.Source
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import java.io.File


class OffProjectAdapter(
    val context: Context,
    var arrayList: ArrayList<ProjectOfflineData>,
    val databaseHelper: DatabaseHelper,
    val refreshProjectArrayList: refreshProjectArrayList
) :
    RecyclerView.Adapter<OffProjectAdapter.ViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //        var editEvery = itemView.findViewById<ImageView>(R.id.editEvery)
        var imageHeader = itemView.findViewById<ImageView>(R.id.imageHeader)
        var popupMenu = itemView.findViewById<ImageView>(R.id.popupMenu)
        var editBtn = itemView.findViewById<ImageView>(R.id.editBtn)
        var projectName = itemView.findViewById<TextView>(R.id.projectName)
        var timeStamp = itemView.findViewById<TextView>(R.id.timeStamp)
        var projectId = itemView.findViewById<TextView>(R.id.projectId)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.custom_projects, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val posId = position
        val data = arrayList[position]

        holder.projectName.text = data.projectName
        holder.timeStamp.text = data.timeStamp
        holder.projectId.text = "Project ID = ${data.id}"


        var dir = Source.getSplitFolderFile(
            context,
           data.projectName,
           data.id
        )

        val outFile = File(dir, data.projectImage)

        if (outFile.exists()) {
            val myBitmap = BitmapFactory.decodeFile(outFile.absolutePath)

            holder.imageHeader.setImageBitmap(myBitmap)
        } else {

//            databaseHelper.deleteLink(data.id)
//            Source.toast(context, "Not Exist")
        }

        holder.itemView.setOnLongClickListener(object : View.OnClickListener,
            View.OnLongClickListener {

            override fun onLongClick(v: View?): Boolean {
                val alertDialogBuilder = AlertDialog.Builder(context)
                alertDialogBuilder.setMessage("Are you sure you want to delete this project? - ${data.projectName}")
                alertDialogBuilder.setPositiveButton("Yes") { dialog, which ->
                    // Handle deletion logic here
                    databaseHelper.deleteLink(data.id)

                    var deleted = false

                    if (dir.exists()) {
                        deleted = dir.delete()
                    } else {
//                        Source.toast(context, "Not exist")

                    }

                    refreshProjectArrayList.refreshProjects()
                    if (deleted) {
                        Source.toast(context, "Deleted")

                    } else {
//                        Source.toast(context, "Not Deleted " + dir.name)

                    }
//                    arrayList.removeAt(posId)
//                    notifyItemRemoved(posId)
                }
                alertDialogBuilder.setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }
                alertDialogBuilder.setOnCancelListener { dialog ->
                    dialog.dismiss()
                }

                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()

                return true
            }

            override fun onClick(v: View?) {
                TODO("Not yet implemented")
            }

        })


        holder.popupMenu.setOnClickListener(View.OnClickListener { //creating a popup menu
            val popup = PopupMenu(context, holder.popupMenu)
            //inflating menu from xml resource
            popup.inflate(R.menu.options_menu)
            //adding click listener
            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    return when (item.getItemId()) {
                        R.id.edit_this -> {                      //handle menu1 click
                            editThis(data)
                            true
                        }

                        R.id.delete_this -> {
                            deleteThis(data)//handle menu2 click
                            true
                        }

                        else -> false
                    }
                }
            })
            //displaying the popup
            popup.show()
        })

        holder.itemView.setOnClickListener {
            if (outFile.exists()) {


                if (data.imageSplitAvailable == "true") {
                    val intent = Intent(context, SplitImage::class.java)
                    intent.putExtra("p", "pixel")
                    intent.putExtra("w", "existing")
                    intent.putExtra("mtype", "single")
                    intent.putExtra("img_path", outFile.path)
                    intent.putExtra("projectName", data.projectName)
                    intent.putExtra("projectDescription", data.projectDescription)
                    intent.putExtra("timeStamp", data.timeStamp)
                    intent.putExtra("projectImage", data.projectImage)
                    intent.putExtra("id", data.id)
                    intent.putExtra("splitId", data.splitId)
                    intent.putExtra("imageSplitAvailable", data.imageSplitAvailable)
                    intent.putExtra("projectNumber", data.projectNumber)
                    intent.putExtra("thresholdVal", data.thresholdVal)
                    intent.putExtra("numberOfSpots", data.noOfSpots)
                    intent.putExtra("type", "na")
                    intent.putExtra("tableName", data.tableName)
                    intent.putExtra("roiTableID", data.roiTableID)
                    intent.putExtra("volumePlotTableID", data.volumePlotTableID)
                    intent.putExtra("intensityPlotTableID", data.intensityPlotTableID)
                    intent.putExtra("plotTableID", data.plotTableID)
                    intent.putExtra("plotTableID", data.plotTableID)
                    context.startActivity(intent)
                } else {
//                    val intent = Intent(context, ImageAnalysis::class.java)
                    val intent = Intent(context, NewImageAnalysis::class.java)
                    intent.putExtra("p", "pixel")
                    intent.putExtra("w", "existing")
                    intent.putExtra("mtype", "single")
                    intent.putExtra("projectName", data.projectName)
                    intent.putExtra("img_path", outFile.path)
                    intent.putExtra("projectDescription", data.projectDescription)
                    intent.putExtra("timeStamp", data.timeStamp)
                    intent.putExtra("projectImage", data.projectImage)
                    intent.putExtra("id", data.id)
                    intent.putExtra("splitId", data.splitId)
                    intent.putExtra("imageSplitAvailable", data.imageSplitAvailable)
                    intent.putExtra("type", "na")
                    intent.putExtra("projectNumber", data.projectNumber)
                    intent.putExtra("thresholdVal", data.thresholdVal)
                    intent.putExtra("numberOfSpots", data.noOfSpots)
                    intent.putExtra("tableName", data.tableName)
                    intent.putExtra("roiTableID", data.roiTableID)
                    intent.putExtra("volumePlotTableID", data.volumePlotTableID)
                    intent.putExtra("intensityPlotTableID", data.intensityPlotTableID)
                    intent.putExtra("plotTableID", data.plotTableID)
                    intent.putExtra("rmSpot", data.rmSpot)
                    intent.putExtra("finalSpot", data.finalSpot)
                    context.startActivity(intent)
                }
            } else {
                Source.toast(
                    context,
                    "Source image not exist, delete this project and create new one"
                )
            }
        }

        holder.editBtn.setOnClickListener {
            val dialogView = mInflater.inflate(R.layout.name_description, null)
            val builder = androidx.appcompat.app.AlertDialog.Builder(context)
                .setView(dialogView)


            val alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val projectNameD = dialogView.findViewById<EditText>(R.id.projectNameD)
            val projectDescriptionD = dialogView.findViewById<EditText>(R.id.projectDescriptionD)
            val isSplit = dialogView.findViewById<MaterialCheckBox>(R.id.isSplit)
            isSplit.visibility = View.GONE

            projectNameD.setText(data.projectName)
            projectDescriptionD.setText(data.projectDescription)

            dialogView.findViewById<MaterialButton>(R.id.submitBtnD).setOnClickListener {
                val pName = projectNameD.text.toString()
                val pDescription = projectDescriptionD.text.toString()

                val projectOfflineData = ProjectOfflineData(
                    data.id,
                    pName,
                    pDescription,
                    data.timeStamp,
                    data.projectNumber,
                    data.projectImage,
                    data.imageSplitAvailable,
                    data.splitId,
                    data.thresholdVal,
                    data.noOfSpots,
                    data.tableName,
                    data.roiTableID,
                    data.volumePlotTableID,
                    data.intensityPlotTableID,
                    data.plotTableID,
                    data.rmSpot,
                    data.finalSpot
                )
                val o = databaseHelper.updateData(projectOfflineData)
                if (o == -1L) {
                    Source.toast(context, "Not updated")
                } else {


                    refreshProjectArrayList.refreshProjects()
                    alertDialog.dismiss()

                }

            }
            alertDialog.show()
        }

    }

    fun editThis(data: ProjectOfflineData) {

        val dialogView = mInflater.inflate(R.layout.name_description, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
            .setView(dialogView)


        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val projectNameD = dialogView.findViewById<EditText>(R.id.projectNameD)
        val projectDescriptionD = dialogView.findViewById<EditText>(R.id.projectDescriptionD)
        val isSplit = dialogView.findViewById<MaterialCheckBox>(R.id.isSplit)
        isSplit.visibility = View.GONE

        projectNameD.setText(data.projectName)
        projectDescriptionD.setText(data.projectDescription)

        dialogView.findViewById<MaterialButton>(R.id.submitBtnD).setOnClickListener {
            val pName = projectNameD.text.toString()
            val pDescription = projectDescriptionD.text.toString()

            val projectOfflineData = ProjectOfflineData(
                data.id,
                pName,
                pDescription,
                data.timeStamp,
                data.projectNumber,
                data.projectImage,
                data.imageSplitAvailable,
                data.splitId,
                data.thresholdVal,
                data.noOfSpots,
                data.tableName,
                data.roiTableID,
                data.volumePlotTableID,
                data.intensityPlotTableID,
                data.plotTableID,
                data.rmSpot,
                data.finalSpot
            )
            val o = databaseHelper.updateData(projectOfflineData)
            if (o == -1L) {
                Source.toast(context, "Not updated")
            } else {

                refreshProjectArrayList.refreshProjects()

                alertDialog.dismiss()
            }

        }
        alertDialog.show()
    }

    fun deleteThis(data: ProjectOfflineData) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setMessage("Are you sure you want to delete this project? - ${data.projectName}")
        alertDialogBuilder.setPositiveButton("Yes") { dialog, which ->
            // Handle deletion logic here
            databaseHelper.deleteLink(data.id)
            refreshProjectArrayList.refreshProjects()
//                    arrayList.removeAt(posId)
//                    notifyItemRemoved(posId)
        }
        alertDialogBuilder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        alertDialogBuilder.setOnCancelListener { dialog ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    fun notify(position: Int) {
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }
}