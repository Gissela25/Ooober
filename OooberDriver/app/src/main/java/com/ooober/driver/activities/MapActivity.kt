package com.ooober.driver.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ooober.driver.R
import com.ooober.driver.databinding.ActivityMainBinding
import com.ooober.driver.providers.AuthProvider

class MapActivity : AppCompatActivity() {

    private val authProvider = AuthProvider()
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}