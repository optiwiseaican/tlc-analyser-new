package com.aican.tlcanalyzer.adapterClasses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.dataClasses.SplitContourData
import com.aican.tlcanalyzer.interfaces.OnClicksListeners

class MultiSplitAdapter(
    val context: Context, val arrayList: ArrayList<SplitContourData>,
    val onClicksListeners: OnClicksListeners
) :
    RecyclerView.Adapter<MultiSplitAdapter.ViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.custom_multi_split, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = arrayList[position]

        holder.splitName.text = data.name

        holder.checkBox.isChecked = data.isSelected

        holder.checkBox.setOnClickListener {
            data.isSelected = holder.checkBox.isChecked
            notifyItemChanged(position)

            onClicksListeners.newOnClick(position)


        }


    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var splitName = itemView.findViewById<TextView>(R.id.splitName)
        var checkBox = itemView.findViewById<CheckBox>(R.id.checkBox)

    }


}