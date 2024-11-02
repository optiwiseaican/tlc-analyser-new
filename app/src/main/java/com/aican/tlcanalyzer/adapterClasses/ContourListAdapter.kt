package com.aican.tlcanalyzer.adapterClasses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.interfaces.RemoveContourInterface

class ContourListAdapter(
    val context: Context,
    var arrayList: ArrayList<Int>,
    var removeContourInterface: RemoveContourInterface
) : RecyclerView.Adapter<ContourListAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contourId = itemView.findViewById<TextView>(R.id.contourId)
        val contourName = itemView.findViewById<TextView>(R.id.contourName)
        val deleteCont = itemView.findViewById<ImageView>(R.id.deleteCont)
        val editCont = itemView.findViewById<ImageView>(R.id.editCont)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.custom_remove_cont, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.editCont.visibility = View.GONE

        holder.contourId.text = "${(arrayList[position])}. "
        holder.contourName.text = "Spot ${(arrayList[position])}"

        holder.deleteCont.setOnClickListener {
            removeContourInterface.removeContour(arrayList[position])
        }

    }

    override fun getItemCount(): Int {
        return arrayList.size
    }
}