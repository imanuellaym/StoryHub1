package com.example.storyhub.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.storyhub.databinding.ItemLoadingBinding

class LoadingAdapter(private val retry: () -> Unit) : LoadStateAdapter<LoadingAdapter.LoadingStateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadingStateViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemLoadingBinding.inflate(layoutInflater, parent, false)
        return LoadingStateViewHolder(binding, retry)
    }

    override fun onBindViewHolder(holder: LoadingStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class LoadingStateViewHolder(private val binding: ItemLoadingBinding, onRetryClick: () -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.retryButton.setOnClickListener {
                onRetryClick()
            }
        }

        fun bind(loadState: LoadState) {
            when (loadState) {
                is LoadState.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.retryButton.isVisible = false
                    binding.errorMsg.isVisible = false
                }
                is LoadState.Error -> {
                    binding.progressBar.isVisible = false
                    binding.retryButton.isVisible = true
                    binding.retryButton.isVisible = true
                    binding.errorMsg.text = loadState.error.localizedMessage
                }
                else -> {
                    binding.progressBar.isVisible = false
                    binding.retryButton.isVisible = false
                    binding.errorMsg.isVisible = false
                }
            }
        }
    }
}
