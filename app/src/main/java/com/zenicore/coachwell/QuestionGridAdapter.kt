package com.zenicore.coachwell

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class QuestionGridAdapter(
    private val totalQuestions: Int,
    private val answeredQuestions: Map<Int, Boolean>, // Menyimpan status jawaban
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<QuestionGridAdapter.QuestionViewHolder>() {

    inner class QuestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.tvQuestionNumber)

        init {
            view.setOnClickListener {
                onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question_number, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val isAnswered = answeredQuestions[position] == true
        holder.textView.text = (position + 1).toString()

        // Ubah warna latar belakang dan teks jika pertanyaan sudah dijawab
        if (isAnswered) {
            holder.textView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.blue))
            holder.textView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
        } else {
            holder.textView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
            holder.textView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
        }
    }

    override fun getItemCount(): Int = totalQuestions
}
