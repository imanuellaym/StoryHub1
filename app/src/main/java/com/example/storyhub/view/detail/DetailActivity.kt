package com.example.storyhub.view.detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.example.storyhub.R
import com.example.storyhub.data.response.Story
import com.example.storyhub.data.result.ResultState
import com.example.storyhub.databinding.ActivityDetailBinding
import com.example.storyhub.view.MainViewModel
import com.example.storyhub.view.ViewModelFactory

class DetailActivity : AppCompatActivity() {

    private val detailViewModel: MainViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private lateinit var detailBinding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detailBinding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(detailBinding.root)

        configureActionBar()
        processIntentData()
    }

    private fun configureActionBar() {
        supportActionBar?.apply {
            title = getString(R.string.description)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun processIntentData() {
        val storyId = intent.getStringExtra(EXTRA_ID).orEmpty()
        if (storyId.isNotEmpty()) {
            toggleLoading(true)
            detailViewModel.retrieveUserSession().observe(this) { session ->
                session?.token?.let { token ->
                    detailViewModel.fetchStoryDetail(token, storyId)
                    detailViewModel.storyDetail.observe(this) { resultState ->
                        when (resultState) {
                            is ResultState.Loading -> {
                                toggleLoading(true)
                            }
                            is ResultState.Success -> {
                                resultState.data?.let { story ->
                                    populateStoryDetails(story)
                                    toggleLoading(false)
                                }
                            }
                            is ResultState.Error -> {
                                showError(resultState.exception.message.orEmpty())
                                toggleLoading(false)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun populateStoryDetails(story: Story) {
        Glide.with(this)
            .load(story.photoUrl)
            .into(detailBinding.imagePreview)
        detailBinding.textName.text = story.name
        detailBinding.textDescription.text = story.description
    }

    private fun toggleLoading(isVisible: Boolean) {
        detailBinding.progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_ID = "extra_id"
    }
}
