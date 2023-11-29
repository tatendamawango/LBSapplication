package edu.ktu.networks.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.ktu.networks.R
import edu.ktu.networks.models.RSS

class RSSAdapter(private val taskList: MutableList<RSS>, private val onDeleteClicked: (Int) -> Unit): RecyclerView.Adapter<RSSAdapter.ViewHolder>() {


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val macAddress: TextView = view.findViewById(R.id.macAddress)
        val s1: TextView = view.findViewById(R.id.s1)
        val s2: TextView = view.findViewById(R.id.s2)
        val s3: TextView = view.findViewById(R.id.s3)
        private val deleteButton: ImageView = view.findViewById(R.id.deleteBtn)

        init {
            deleteButton.setOnClickListener {
                onDeleteClicked(adapterPosition)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.each_rss, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = taskList[position]
        holder.macAddress.text = data.macAddress
        holder.s1.text = data.s1.toString()
        holder.s2.text = data.s2.toString()
        holder.s3.text = data.s3.toString()
    }
}