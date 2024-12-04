package com.example.storyhub.view.welcome

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.storyhub.databinding.ActivityWelcomeBinding
import com.example.storyhub.view.login.LoginActivity
import com.example.storyhub.view.signup.SignupActivity

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()

        binding.btnLogin.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
        binding.btnSignup.setOnClickListener {
            val registerIntent = Intent(this, SignupActivity::class.java)
            startActivity(registerIntent)
        }

        runInitialAnimations()
    }

    private fun runInitialAnimations() {
        val imageViewAnimator = ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }

        val fadeInDuration = 100L
        val titleFadeIn = ObjectAnimator.ofFloat(binding.tvTittle, View.ALPHA, 1f).setDuration(fadeInDuration)
        val descriptionFadeIn = ObjectAnimator.ofFloat(binding.tvDesc, View.ALPHA, 1f).setDuration(fadeInDuration)
        val loginButtonFadeIn = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(fadeInDuration)
        val signupButtonFadeIn = ObjectAnimator.ofFloat(binding.btnSignup, View.ALPHA, 1f).setDuration(fadeInDuration)

        val buttonFadeInAnimations = AnimatorSet().apply {
            playTogether(loginButtonFadeIn, signupButtonFadeIn)
        }

        AnimatorSet().apply {
            playSequentially(titleFadeIn, descriptionFadeIn, buttonFadeInAnimations)
            start()
        }

        imageViewAnimator.start()
    }
}
