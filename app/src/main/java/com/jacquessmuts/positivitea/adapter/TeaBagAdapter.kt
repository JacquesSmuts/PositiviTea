package com.jacquessmuts.positivitea.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jacquessmuts.positivitea.R
import com.jacquessmuts.positivitea.model.TeaBag
import kotlinx.android.synthetic.main.item_teabag.view.*

class TeaBagAdapter internal constructor(
        context: Context
) : RecyclerView.Adapter<TeaBagAdapter.WordViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    var teaBags = emptyList<TeaBag>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val itemView = inflater.inflate(R.layout.item_teabag, parent, false)
        return WordViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val teaBag = teaBags[position]
        holder.update(teaBag)
    }

    override fun getItemCount() = teaBags.size

    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun update(teaBag: TeaBag) {

            itemView.textViewTitle.text = teaBag.title
            itemView.textViewMessage.text = teaBag.message

        }
    }
}