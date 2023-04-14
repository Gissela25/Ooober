package com.ooober.driver.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.oAuthProvider
import com.ooober.driver.R
import com.ooober.driver.databinding.ActivityMapBinding
import com.ooober.driver.providers.AuthProvider

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding
    private val authProvider = AuthProvider()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        if (authProvider.existsSession()) {
            goToMap()
        }
    }

    private fun goToMap() {

    }
}