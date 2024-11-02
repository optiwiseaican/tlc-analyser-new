package com.aican.tlcanalyzer.adapterClasses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.dataClasses.userIDPASS.UserData

class RecentlyUserCreated(val context: Context, val arrayList: ArrayList<UserData>) :
    RecyclerView.Adapter<RecentlyUserCreated.ViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.recently_user_added, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = arrayList[position]

        holder.user_role.text = data.role
        holder.user_name.text = data.name
        holder.dateCreated.text = data.dateCreated
        holder.expiry_date.text = data.expiryDate

//        Source.toast(context,"added")

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val user_role = itemView.findViewById<TextView>(R.id.user_role)
        val user_name = itemView.findViewById<TextView>(R.id.user_name)
        val dateCreated = itemView.findViewById<TextView>(R.id.dateCreated)
        val expiry_date = itemView.findViewById<TextView>(R.id.expiry_date)
    }

}