package com.dicoding.asclepius.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.asclepius.databinding.ItemNewsProfileBinding
import com.dicoding.asclepius.response.ArticlesItem

class DataAdapter(private val context: Context) :
    ListAdapter<ArticlesItem, DataAdapter.MyViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemNewsProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)

            holder.itemView.setOnClickListener {
                val url = getItem(holder.adapterPosition).url
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                context.startActivity(intent)
            }
        }

    }

    class MyViewHolder(val binding: ItemNewsProfileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(article: ArticlesItem) {
            binding.tvTitle.text = "${article.title}"
            binding.tvDescription.text = "${article.content}"
            Glide.with(binding.root.context)
                .load(article.urlToImage)
                .into(binding.profileImage)
        }

    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ArticlesItem>() {
            override fun areItemsTheSame(
                oldItem: ArticlesItem,
                newItem: ArticlesItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ArticlesItem,
                newItem: ArticlesItem
            ): Boolean {
                return oldItem == newItem
            }
        }

    }
}

