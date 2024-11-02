package com.aican.tlcanalyzer.adapterClasses

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.dataClasses.AnalMultiIntModel
import com.aican.tlcanalyzer.dataClasses.ContourData
import com.aican.tlcanalyzer.interfaces.OnClicksListeners

class AnalMultiIntAdapter(
    val context: Context,
    val arrayList: ArrayList<AnalMultiIntModel>,
    val onClickListener: OnClicksListeners
) :
    RecyclerView.Adapter<AnalMultiIntAdapter.ViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private val viewPool = RecyclerView.RecycledViewPool()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.custom_contours_lists_rec, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = arrayList[position]

        holder.imageName.text = data.imageName

//        val childLayoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        var childLayoutManager = GridLayoutManager(context, 4)

        if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            childLayoutManager.spanCount = 6
        } else {
            childLayoutManager.spanCount = 4
        }


        holder.contourListRecView.apply {
            layoutManager = childLayoutManager
            adapter =
                ContourIntGraphAdapter(context, data.dataArrayList, position, onClickListener, true, false, false)
            viewPool
        }


    }

    fun updateContourDataAtPosition(position: Int, data: ArrayList<ContourData>) {
        arrayList[position].dataArrayList.clear()
        arrayList[position].dataArrayList.addAll(data)
        notifyItemChanged(position)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val contourListRecView = view.findViewById<RecyclerView>(R.id.contourListRecView)!!
        val imageName = view.findViewById<TextView>(R.id.imageName)!!
    }

}