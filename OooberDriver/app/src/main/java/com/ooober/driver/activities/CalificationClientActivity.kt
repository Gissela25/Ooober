package com.ooober.driver.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ooober.driver.R
import com.ooober.driver.databinding.ActivityCalificationClientBinding

class CalificationClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalificationClientBinding
    private var extraPrice= 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalificationClientBinding.inflate(layoutInflater)
        setContentView(binding.root)
        extraPrice=intent.getDoubleExtra("price", 0.0)
        binding.textViewPrice.text = "Precio: $extraPrice"
    }
}