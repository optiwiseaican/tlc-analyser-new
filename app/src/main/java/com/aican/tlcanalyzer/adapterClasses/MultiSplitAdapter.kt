package com.aican.tlcanalyzer.adapterClasses

import android.content.Context
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.dataClasses.SplitContourData
import com.aican.tlcanalyzer.interfaces.OnClicksListeners
import com.aican.tlcanalyzer.interfaces.OnPlotClickListeners
import java.io.File

class MultiSplitAdapter(
    val id: String,
    val context: Context, val arrayList: ArrayList<SplitContourData>,
    val onClicksListeners: OnClicksListeners,
    val onPlotClickListeners: OnPlotClickListeners
) :
    RecyclerView.Adapter<MultiSplitAdapter.ViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.custom_multi_split, parent, false)
        return ViewHolder(view)
    }

    private var dir =
        File(
            ContextWrapper(context).externalMediaDirs[0],
            context.resources.getString(R.string.app_name) + id
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = arrayList[position]
        val outFile = File(dir, data.mainImageName)
        val contOutFile = File(dir, data.contourImageName)
        holder.checkSpottedGreen.visibility = View.GONE
        holder.checkSpottedRed.visibility = View.VISIBLE

        holder.checkBox.isChecked = data.isSelected


        if (contOutFile.exists()) {
//            holder.checkSpotted.visibility = View.VISIBLE

            holder.checkSpottedGreen.visibility = View.VISIBLE
            holder.checkSpottedRed.visibility = View.GONE

            holder.rmValue.text = "RM: " + data.rmSpot
            holder.finalValue.text = "Final: " + data.finalSpot
            val myBitmap = BitmapFactory.decodeFile(contOutFile.absolutePath)

            holder.splitImage.setImageBitmap(myBitmap)

        } else {
            Log.e("COnNOtEx", "Not exist")

//            android.os.Handler().post {
            if (position < arrayList.size) {
                data.isSelected = false
                holder.checkBox.isChecked = false
                holder.checkBox.isEnabled = false
                arrayList[position].isSelected = false
//                notifyItemChanged(position)
            }
//            }
            if (outFile.exists()) {
                val myBitmap = BitmapFactory.decodeFile(outFile.absolutePath)

                holder.splitImage.setImageBitmap(myBitmap)
            } else {
//            Source.toast(context, "Not Exist")
            }
        }

        holder.splitName.text = data.name

        holder.intensityNotExist.visibility = View.GONE
        holder.hrValue.text = "Hour: " + data.hr + " hour"

        if (data.rFvsAreaArrayList == null) {
            holder.intensityNotExist.visibility = View.VISIBLE
        }
        if (data.rFvsAreaArrayList != null && data.rFvsAreaArrayList.isEmpty()) {
            holder.intensityNotExist.visibility = View.VISIBLE
        }


        holder.checkBox.setOnClickListener {
            if (contOutFile.exists()) {
                data.isSelected = holder.checkBox.isChecked
                // ❌ Don't call `onClicksListeners.newOnClick(position)`
                notifyItemChanged(position)
            }
//            onClicksListeners.newOnClick(position)


        }

        holder.intensityNotExist.visibility = View.GONE
        holder.plotButton.visibility = View.GONE


        holder.plotButton.setOnClickListener {
            onPlotClickListeners.onPlotClick(
                position,
                data.mainImageName,
                data.intensityPlotTableID
            )
        }


    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var splitName = itemView.findViewById<TextView>(R.id.splitName)
        var checkBox = itemView.findViewById<CheckBox>(R.id.checkBox)
        var intensityNotExist = itemView.findViewById<LinearLayout>(R.id.intensityNotExist)
        var plotButton = itemView.findViewById<Button>(R.id.plotButton)
        val splitImage = itemView.findViewById<ImageView>(R.id.splitImage)

        val checkSpottedGreen = itemView.findViewById<LinearLayout>(R.id.checkSpottedGreen)
        val checkSpottedRed = itemView.findViewById<LinearLayout>(R.id.checkSpottedRed)
        val rmValue = itemView.findViewById<TextView>(R.id.rmValue)
        val finalValue = itemView.findViewById<TextView>(R.id.finalValue)
        val hrValue = itemView.findViewById<TextView>(R.id.hrValue)
    }


}