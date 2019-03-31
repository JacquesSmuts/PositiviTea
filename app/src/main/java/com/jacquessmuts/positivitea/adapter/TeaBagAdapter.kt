package com.jacquessmuts.positivitea.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jacquessmuts.positivitea.R
import com.jacquessmuts.positivitea.model.TeaBag
import com.jacquessmuts.positivitea.util.AppUtils
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_teabag.view.*

class TeaBagAdapter internal constructor(
    context: Context,
    val teabagVotePublisher: PublishSubject<TeaBagVote>
) : RecyclerView.Adapter<TeaBagAdapter.TeaBagHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    var teaBags = emptyList<TeaBag>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeaBagHolder {
        val itemView = inflater.inflate(R.layout.item_teabag, parent, false)
        return TeaBagHolder(itemView)
    }

    override fun onBindViewHolder(holder: TeaBagHolder, position: Int) {
        val teaBag = teaBags[position]
        holder.update(teaBag)
    }

    override fun getItemCount() = teaBags.size

    inner class TeaBagHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun update(teaBag: TeaBag) {

            itemView.textViewTitle.text = teaBag.title
            itemView.textViewMessage.text = teaBag.message

            if (AppUtils.isModerator()) {
                itemView.imageUpvote.visibility = View.VISIBLE
                itemView.imageDownvote.visibility = View.VISIBLE

                itemView.imageUpvote.setOnClickListener {
                    teabagVotePublisher.onNext(TeaBagVote(teaBag, true))
                }

                itemView.imageDownvote.setOnClickListener {
                    teabagVotePublisher.onNext(TeaBagVote(teaBag, false))
                }
            }
        }
    }
}

data class TeaBagVote(val teaBag: TeaBag, val isApproved: Boolean)