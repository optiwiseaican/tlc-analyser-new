package com.aican.tlcanalyzer.adapterClasses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.dataClasses.ContourTableData

class MyCustomTableAdapter(val context: Context, val arrayList: ArrayList<ContourTableData>) :
    RecyclerView.Adapter<MyCustomTableAdapter.ViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.custom_rows_table, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = arrayList[position]

        holder.id.setText(data.id)
        holder.area.text = data.area
        holder.cv.text = data.cv
        holder.rf.text = data.rf
        holder.areaPercentage.text = data.areaPercentage
        holder.volume.text = data.volume


    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val id = itemView.findViewById<TextView>(R.id.id)
        val cv = itemView.findViewById<TextView>(R.id.cv)
        val rf = itemView.findViewById<TextView>(R.id.rf)
        val area = itemView.findViewById<TextView>(R.id.area)
        val areaPercentage = itemView.findViewById<TextView>(R.id.areaPercentage)
        val volume = itemView.findViewById<TextView>(R.id.volume)
    }

}