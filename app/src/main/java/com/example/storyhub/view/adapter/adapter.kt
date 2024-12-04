package com.example.storyhub.view.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storyhub.data.response.ListStoryItem
import com.example.storyhub.databinding.ListStoryBinding // Ganti ke binding yang benar
import com.example.storyhub.view.detail.DetailActivity

class Adapter : PagingDataAdapter<ListStoryItem, Adapter.StoryViewHolder>(StoryComparator) {

    class StoryViewHolder(private val itemBinding: ListStoryBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(storyItem: ListStoryItem) {
            with(itemBinding) {
                usernameTextView.text = storyItem.name
                description.text = storyItem.description
                Glide.with(itemView.context)
                    .load(storyItem.photoUrl)
                    .into(ivPriview)
                story.setOnClickListener {
                    val context = it.context
                    val detailIntent = Intent(context, DetailActivity::class.java).apply {
                        putExtra(DetailActivity.EXTRA_ID, storyItem.id)
                    }
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity)
                    context.startActivity(detailIntent, options.toBundle())
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListStoryBinding.inflate(layoutInflater, parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { holder.bind(it) }
    }

    companion object {
        val StoryComparator = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
