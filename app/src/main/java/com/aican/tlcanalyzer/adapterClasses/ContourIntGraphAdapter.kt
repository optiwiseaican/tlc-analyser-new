package com.aican.tlcanalyzer.adapterClasses

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.dataClasses.ContourData
import com.aican.tlcanalyzer.interfaces.EditCallBack
import com.aican.tlcanalyzer.interfaces.OnClicksListeners
import com.aican.tlcanalyzer.utils.Source


class ContourIntGraphAdapter(
    val isSelected: Boolean,
    val context: Context,
    val arrayList: ArrayList<ContourData>,
    val parentPosition: Int,
    val onClickListener: OnClicksListeners,
    val click: Boolean,
    val showDelete: Boolean,
    val showEdit: Boolean
) :
    RecyclerView.Adapter<ContourIntGraphAdapter.ViewHolder>() {
    lateinit var editCallBack: EditCallBack

    constructor(
        isSelected: Boolean,
        context: Context,
        arrayList: ArrayList<ContourData>,
        parentPosition: Int,
        onClickListener: OnClicksListeners,
        click: Boolean,
        showDelete: Boolean,
        showEdit: Boolean,
        editCallBack: EditCallBack,
    ) : this(
        isSelected,
        context,
        arrayList,
        parentPosition,
        onClickListener,
        click,
        showDelete,
        showEdit
    ) {
        this.editCallBack = editCallBack
    }


    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.custom_contour_for_int_graph, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = arrayList[position]

        if (!showDelete) {
            holder.deleteThis.visibility = View.GONE
        } else {

            holder.deleteThis.visibility = View.VISIBLE
        }

        if (!showEdit) {
            holder.editThis.visibility = View.GONE
        } else {

            holder.editThis.visibility = View.VISIBLE
        }

        holder.deleteThis.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(context)
            alertDialogBuilder.setMessage("Are you sure you want to delete this spot?")
            alertDialogBuilder.setPositiveButton(
                "Yes"
            ) { dialog, which ->

//                (context as Activity).runOnUiThread {
//                    LoadingDialog.showLoading(context, false, false, "Deleting")
//                }
                Source.spotPositionFromAdapter = holder.adapterPosition
                Source.removingContourID = data.id
                Source.removingContourFromAdapter = true
                Source.hideAnalyserLayout = true

                Log.e("ContextNull", "Yess Clicked")

                if ((context as Activity) == null) {
                    Log.e("ContextNull", "ContextNull")
                } else {
                    context.finish()
                }
            }
            alertDialogBuilder.setNegativeButton(
                "No"
            ) { dialog, which -> dialog.dismiss() }
            alertDialogBuilder.setOnCancelListener { dialog -> dialog.dismiss() }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()

        }

        holder.editThis.setOnClickListener {


            editCallBack.editOnClick(data.id, data.rf, data.rfTop, data.rfBottom)


        }

//        holder.deleteThis.visibility = View.GONE


        holder.contText.text = "Spot " + data.id

        val defaultColor = context.getColor(R.color.con_btn_clr)


        if (!data.isSelected) {

            val newColor = defaultColor
            val existingBackground = holder.contText.background as GradientDrawable
            existingBackground.setColor(newColor)
            holder.contText.invalidate()

//            holder.contText.setBackgroundColor(defaultColor)
            holder.contText.setTextColor(context.getColor(R.color.aican_blue))
        } else {

            val newColor = data.buttonColor
            val existingBackground = holder.contText.background as GradientDrawable
            existingBackground.setColor(newColor)
            holder.contText.invalidate()
//            holder.contText.setBackgroundColor()
//            holder.contText.backgroundTintMode = data.buttonColor

            holder.contText.setTextColor(context.getColor(R.color.white))

        }

        holder.contText.setOnClickListener {

            if (isSelected) {
                if (click) {

                    if (data.isSelected) {
                        data.isSelected = false
                        val newColor = defaultColor
                        val existingBackground = holder.contText.background as GradientDrawable
                        existingBackground.setColor(newColor)
                        holder.contText.invalidate()

                        holder.contText.setTextColor(context.getColor(R.color.aican_blue))


                        onClickListener.onClick(
                            position,
                            parentPosition,
                            data.id,
                            data.rfTop,
                            data.rfBottom,
                            data.rf,
                            data.isSelected
                        )


                    } else {
                        data.isSelected = true

                        val newColor = data.buttonColor
                        val existingBackground = holder.contText.background as GradientDrawable
                        existingBackground.setColor(newColor)
                        holder.contText.invalidate()

                        holder.contText.setTextColor(context.getColor(R.color.white))

                        onClickListener.onClick(
                            position,
                            parentPosition,
                            data.id,
                            data.rfTop,
                            data.rfBottom,
                            data.rf,
                            data.isSelected
                        )
                    }

                }
            }
        }

    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contText = view.findViewById<TextView>(R.id.contText)
        val deleteThis = view.findViewById<ImageView>(R.id.delete_this)
        val editThis = view.findViewById<ImageView>(R.id.edit_this)
    }

}