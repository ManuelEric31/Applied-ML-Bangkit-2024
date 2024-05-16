package com.dicoding.asclepius.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.asclepius.database.HistoryUser
import com.dicoding.asclepius.databinding.ItemNewsProfileBinding

class HistoryAdapter(private var historyList: List<HistoryUser>, private val onItemClick: (HistoryUser) -> Unit) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemNewsProfileBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.root.setOnClickListener(this)
        }

        fun bind(historyUser: HistoryUser) {
            binding.apply {
                tvTitle.text = historyUser.category
                tvDescription.text = historyUser.confident
                Glide.with(root)
                    .load(historyUser.uriImage)
                    .into(profileImage)
            }
        }

        override fun onClick(v: View?) {
            onItemClick(historyList[adapterPosition])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemNewsProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount() = historyList.size

    fun updateData(newList: List<HistoryUser>) {
        historyList = newList
        notifyDataSetChanged()
    }
}
