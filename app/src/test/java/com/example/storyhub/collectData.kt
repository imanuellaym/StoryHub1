package com.example.storyhub

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import kotlinx.coroutines.Dispatchers

// Fungsi untuk mengonversi PagingData ke List
suspend fun <T : Any> PagingData<T>.collectData(): List<T> {
    val differ = AsyncPagingDataDiffer(
        diffCallback = object : DiffUtil.ItemCallback<T>() {
            override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem
            override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem
        },
        updateCallback = NoopListUpdateCallback(),
        mainDispatcher = Dispatchers.Main
    )
    differ.submitData(this)
    return differ.snapshot().items
}

// Callback kosong untuk AsyncPagingDataDiffer
private class NoopListUpdateCallback : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}
