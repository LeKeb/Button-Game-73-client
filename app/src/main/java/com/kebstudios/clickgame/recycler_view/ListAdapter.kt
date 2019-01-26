package com.kebstudios.clickgame.recycler_view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.kebstudios.clickgame.R
import com.kebstudios.clickgame.api.objects.Winner

class ListAdapter(private var data: List<Winner>): RecyclerView.Adapter<WinnerHolder>() {

    fun updateData(newData: List<Winner>) {
        data = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): WinnerHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.winner_item, parent, false) as LinearLayout

        return WinnerHolder(layout)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: WinnerHolder, index: Int) {
        holder.root.findViewById<TextView>(R.id.name_field).text = data[index].user.name

        when (data[index].amount) {
            500 -> holder.root.findViewById<ImageView>(R.id.trophy_icon).setImageResource(R.drawable.gold_trophy)
            200 -> holder.root.findViewById<ImageView>(R.id.trophy_icon).setImageResource(R.drawable.silver_trophy)
            else -> holder.root.findViewById<ImageView>(R.id.trophy_icon).setImageResource(R.drawable.bronze_trophy)
        }
    }
}