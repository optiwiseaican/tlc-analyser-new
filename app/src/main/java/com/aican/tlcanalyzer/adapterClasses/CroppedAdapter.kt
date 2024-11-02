package com.aican.tlcanalyzer.adapterClasses

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aican.tlcanalyzer.R

class CroppedAdapter(val context: Context, val arrayList: ArrayList<Bitmap>) :
    RecyclerView.Adapter<CroppedAdapter.ViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.dialog_image_cropped, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.iv_cropped_image.setImageBitmap(arrayList[position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val iv_cropped_image = itemView.findViewById<ImageView>(R.id.iv_cropped_image)
    }

}