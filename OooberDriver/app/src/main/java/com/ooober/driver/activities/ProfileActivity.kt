package com.ooober.driver.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import com.ooober.driver.databinding.ActivityProfileBinding
import com.ooober.driver.models.Driver
import com.ooober.driver.providers.AuthProvider
import com.ooober.driver.providers.DriverProvider

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    val driverProvider = DriverProvider()
    val authProvider = AuthProvider()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        binding.ivBack.setOnClickListener{
            finish()
        }
        getDriver()
    }
    private fun getDriver(){
        driverProvider.getDriver(authProvider.getId()).addOnSuccessListener { document ->
            if(document.exists()){
                val driver = document.toObject(Driver::class.java)
                binding.tvEmail.text = driver?.email
                binding.tfName.setText(driver?.name)
                binding.tflastname.setText(driver?.lastname)
                binding.tfPhone.setText(driver?.phone)
                binding.tfCarBrand.setText(driver?.brandCar)
                binding.tfCarColor.setText(driver?.colorCar)
                binding.tfCarPlate.setText(driver?.plateNumber)

            }
        }
    }


}