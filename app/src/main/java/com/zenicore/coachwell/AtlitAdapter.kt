package com.zenicore.coachwell

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class AtlitAdapter(private val atlitList: MutableList<Atlit>, private val onClick: (Atlit) -> Unit) :
    RecyclerView.Adapter<AtlitAdapter.AtlitViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AtlitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_atlet_main, parent, false)
        return AtlitViewHolder(view)
    }

    override fun onBindViewHolder(holder: AtlitViewHolder, position: Int) {
        val atlit = atlitList[position]
        holder.tvNama.text = atlit.nama
        // Set other views if necessary, like image

        holder.itemView.setOnClickListener { onClick(atlit) }

        // Set delete icon click listener
        holder.imgDelete.setOnClickListener {
            // Remove from list
            atlitList.removeAt(position)
            notifyItemRemoved(position)
            // Delete from Firebase
            deleteAtlitFromDatabase(atlit.id)
        }
    }

    override fun getItemCount(): Int = atlitList.size

    private fun deleteAtlitFromDatabase(atlitId: String) {
        val database = FirebaseDatabase.getInstance("https://coachwell-ab27a-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("atlit")
        database.child(atlitId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("AtlitAdapter", "Atlit deleted successfully")
            } else {
                Log.e("AtlitAdapter", "Failed to delete atlit", task.exception)
            }
        }
    }

    inner class AtlitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val imgDelete: ImageView = itemView.findViewById(R.id.imgDelete)
    }
}
