package com.aican.tlcanalyzer.adapterClasses

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aican.tlcanalyzer.NewImageAnalysis
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.dataClasses.SplitData
import com.aican.tlcanalyzer.database.DatabaseHelper
import com.aican.tlcanalyzer.dialog.AuthDialog
import java.io.File

class SplitAdapter(
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
) : RecyclerView.Adapter<SplitAdapter.ViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    var hours = arrayOf(
        "1 Hour",
        "2 Hour",
        "3 Hour",
        "4 Hour",
        "5 Hour",
        "6 Hour",
        "7 Hour",
        "8 Hour",
        "9 Hour",
        "10 Hour",
        "11 Hour",
        "12 Hour",
        "13 Hour",
        "14 Hour",
        "15 Hour",
        "16 Hour",
        "17 Hour",
        "18 Hour",
        "19 Hour",
        "20 Hour",
        "21 Hour",
        "22 Hour",
        "23 Hour",
        "24 Hour"
    )

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val splitImage = itemView.findViewById<ImageView>(R.id.splitImage)
        val spinnerText = itemView.findViewById<TextView>(R.id.spinnerText)
        val imageName = itemView.findViewById<TextView>(R.id.imageName)
        val imageTimeStamp = itemView.findViewById<TextView>(R.id.imageTimeStamp)
        val checkSpottedGreen = itemView.findViewById<ImageView>(R.id.checkSpottedGreen)
        val checkSpottedRed = itemView.findViewById<ImageView>(R.id.checkSpottedRed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.custom_split, parent, false)
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
        val contOutFile = File(dir, "CONT" + data.id + ".png")
        val posId = position
        holder.imageName.text = data.imageName
        holder.imageTimeStamp.text = data.timeStamp

        var hour = data.hour

        holder.checkSpottedGreen.visibility = View.GONE
        holder.checkSpottedRed.visibility = View.VISIBLE

        if (contOutFile.exists()) {
//            holder.checkSpotted.visibility = View.VISIBLE

            holder.checkSpottedGreen.visibility = View.VISIBLE
            holder.checkSpottedRed.visibility = View.GONE
        }

        if (outFile.exists()) {
            val myBitmap = BitmapFactory.decodeFile(outFile.absolutePath)

            holder.splitImage.setImageBitmap(myBitmap)
        } else {
//            Source.toast(context, "Not Exist")
        }



        holder.itemView.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(v: View?): Boolean {

                val alertDialogBuilder = AlertDialog.Builder(context)
                alertDialogBuilder.setMessage("Are you sure you want to delete this project? - ${data.imageName}")
                alertDialogBuilder.setPositiveButton("Yes") { dialog, which ->
                    // Handle deletion logic here
                    databaseHelper.deleteSplitImg(tableName, data.id)
                    arrayList.removeAt(posId)
                    notifyItemRemoved(posId)
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

        })


        holder.itemView.setOnClickListener {

            AuthDialog.projectType = "Split"
            AuthDialog.projectName = projectName + " -> " + data.imageName
            AuthDialog.projectID = id

//            val intent = Intent(context, ImageAnalysis::class.java)
            val intent = Intent(context, NewImageAnalysis::class.java)
            intent.putExtra("positionOf", position.toString())
            intent.putExtra("w", "split")
            intent.putExtra("hour", hour)
            intent.putExtra("img_path", outFile.path)
            intent.putExtra("projectName", projectName)
            intent.putExtra("projectDescription", data.description)
            intent.putExtra("projectImage", data.imagePath)
            intent.putExtra("projectNumber", projectNumber)
            intent.putExtra("splitProjectName", data.imageName)

            intent.putExtra("splitId", splitId)
            if (type == "multi") {
                intent.putExtra("type", "multi")
            } else {
                intent.putExtra("type", "multi")
            }
            intent.putExtra("imageName", data.imageName)
            intent.putExtra("timeStamp", data.timeStamp)
            intent.putExtra("tableName", tableName)
            intent.putExtra("roiTableID", data.roiTableID)
            intent.putExtra("thresholdVal", data.thresholdVal)
            intent.putExtra("numberOfSpots", data.noOfSpots)
            intent.putExtra("id", id)
            intent.putExtra("pid", data.id)
            intent.putExtra("volumePlotTableID", data.volumePlotTableID)
            intent.putExtra("intensityPlotTableID", data.intensityPlotTableID)
            intent.putExtra("plotTableID", data.plotTableID)
            intent.putExtra("rmSpot", data.rmSpot)
            intent.putExtra("finalSpot", data.finalSpot)



            context.startActivity(intent)
            if (type == "multi") {
                (context as Activity).finish()
            }
        }

//        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, hours)
        val adapter = ArrayAdapter(context, R.layout.spinner_item_large, hours)

//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter.setDropDownViewResource(R.layout.spinner_item_large)

        // Create a PopupWindow
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.spinner_popup, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val listView = popupView.findViewById<ListView>(R.id.spinner_list_view)
        listView.adapter = adapter
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.LTGRAY))

        holder.spinnerText.text = data.hour + " hour"

        // Handle item selection
        listView.setOnItemClickListener { _, _, pos, _ ->
            val selectedItem = hours[pos]
            holder.spinnerText.text = selectedItem

            val newData = SplitData(
                data.id,
                data.imageName,
                data.imagePath,
                data.timeStamp,
                data.thresholdVal,
                data.noOfSpots,
                data.roiTableID,
                data.volumePlotTableID,
                data.intensityPlotTableID,
                data.plotTableID,
                data.description,
                (pos + 1).toString(),
                data.rmSpot,
                data.finalSpot
            )

            databaseHelper.updateSplitData(newData, tableName)

            hour = (pos + 1).toString()

//            notifyItemChanged(position)

            popupWindow.dismiss()
        }

        // Show the PopupWindow on TextView click
        holder.spinnerText.setOnClickListener {
            popupWindow.showAsDropDown(holder.spinnerText, 0, 0)
        }

    }

    override fun getItemCount(): Int {
        return arrayList.size
    }
}