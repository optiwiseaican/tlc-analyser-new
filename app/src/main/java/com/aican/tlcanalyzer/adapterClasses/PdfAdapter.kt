package com.aican.tlcanalyzer.adapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.aican.tlcanalyzer.BuildConfig
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.interfaces.OnPDFSelectListener
import java.io.File

class PdfAdapter(
    private val context: Context,
    private val pdfFile: List<File>,
    listener: OnPDFSelectListener
) : RecyclerView.Adapter<PdfAdapter.PdfViewHolder?>() {
    private val listener: OnPDFSelectListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.getContext())
        val view: View = inflater.inflate(R.layout.custom_exported, parent, false)
        return PdfViewHolder(view)
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        holder.fileName.text = pdfFile[holder.adapterPosition].name
        holder.fileName.isSelected = true
        holder.itemView.setOnClickListener(View.OnClickListener {
            listener.onPDFSelected(
                pdfFile[holder.adapterPosition],
                pdfFile[holder.adapterPosition].name,
                holder.adapterPosition
            )
            //                Toast.makeText(context, ""+pdfFile.get(holder.getAdapterPosition()), Toast.LENGTH_SHORT).show();
        })


        holder.shareThis.setOnClickListener {
            val file = pdfFile[holder.adapterPosition]

            val fileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + ".provider", file
                )
            } else {
                Uri.fromFile(file)
            }

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "application/pdf"
            intent.putExtra(Intent.EXTRA_STREAM, fileUri)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                val resInfoList: List<ResolveInfo> =
                    context.packageManager.queryIntentActivities(intent, 0)
                for (resolveInfo in resInfoList) {
                    val packageName: String = resolveInfo.activityInfo.packageName
                    context.grantUriPermission(
                        packageName,
                        fileUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
            }

            context.startActivity(Intent.createChooser(intent, "Share PDF"))
        }



        holder.delete.setOnClickListener(View.OnClickListener {

            val alertDialogBuilder = AlertDialog.Builder(context)
            alertDialogBuilder.setMessage("Are you sure you want to delete this project? - ${pdfFile[holder.adapterPosition].name}")
            alertDialogBuilder.setPositiveButton("Yes") { dialog, which ->
                // Handle deletion logic here
                listener.onDelete(
                    pdfFile[holder.adapterPosition],
                    holder.adapterPosition
                )
            }
            alertDialogBuilder.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            alertDialogBuilder.setOnCancelListener { dialog ->
                dialog.dismiss()
            }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()


        })
        holder.openPDF.setOnClickListener(View.OnClickListener {
            listener.inExternalApp(
                pdfFile[holder.getAdapterPosition()],
                holder.openPDF.getContext()
            )
        })
        var lastPosition = -1
        val animation: Animation = AnimationUtils.loadAnimation(
            context,
            if (position > lastPosition) R.anim.down_to_up else R.anim.up_to_down
        )
        holder.itemView.startAnimation(animation)
        lastPosition = position
    }

    override fun getItemCount(): Int {
        return pdfFile.size
    }

    init {
        this.listener = listener
    }

    inner class PdfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fileName: TextView
        var delete: ImageView
        var openPDF: ImageView
        var shareThis: ImageView

        init {
            fileName = itemView.findViewById(R.id.fileName)
            delete = itemView.findViewById(R.id.delete)
            openPDF = itemView.findViewById(R.id.openPDF)
            shareThis = itemView.findViewById(R.id.shareThis)
        }
    }
}