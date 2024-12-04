package com.example.storyhub.view.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyhub.R
import com.example.storyhub.databinding.ActivityMainBinding
import com.example.storyhub.view.adapter.Adapter
import com.example.storyhub.view.adapter.LoadingAdapter
import com.example.storyhub.view.add.UploadActivity
import com.example.storyhub.view.login.LoginActivity
import com.example.storyhub.view.maps.MapsActivity
import com.example.storyhub.view.welcome.WelcomeActivity
import com.example.storyhub.view.MainViewModel
import com.example.storyhub.view.ViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.rvStory.layoutManager = LinearLayoutManager(this)

        showLoading(true)

        // Observe user session
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.retrieveUserSession().observe(this@MainActivity) { user ->
                    if (!user.isLogin) {
                        navigateToWelcomeActivity()
                    } else {
                        setupRecyclerView(user.token)
                    }
                }
            }
        }

        // Floating Action Button to add new story
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this@MainActivity, UploadActivity::class.java))
        }

        // Handle back button to close the app
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })
    }

    private fun setupRecyclerView(token: String) {
        val adapter = Adapter().apply {
            withLoadStateFooter(LoadingAdapter { retry() })
        }
        binding.rvStory.adapter = adapter
        viewModel.getRetrieveStories(token).observe(this) { pagingData ->
            adapter.submitData(lifecycle, pagingData)
            showLoading(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val logoutItem = menu.findItem(R.id.action_logout)
        logoutItem.icon?.setTint(ContextCompat.getColor(this, R.color.white))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                lifecycleScope.launch {
                    viewModel.userLogout()
                    navigateToLoginActivity()
                }
                true
            }
            R.id.action_maps -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun navigateToWelcomeActivity() {
        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
