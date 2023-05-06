package com.ooober.user.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.toObject
import com.ooober.user.R
import com.ooober.user.databinding.ActivityHistoriesDetailBinding
import com.ooober.user.models.Client
import com.ooober.user.models.Driver
import com.ooober.user.models.History
import com.ooober.user.providers.ClientProvider
import com.ooober.user.providers.DriverProvider
import com.ooober.user.providers.HistoryProvider
import com.ooober.user.utils.RelativeTime

class HistoriesDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoriesDetailBinding
    private var historyProvider = HistoryProvider()
    private var DriverProvider = DriverProvider()
    private var extraId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoriesDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        extraId = intent.getStringExtra("id")!!
        getHistory()

        binding.imageViewBack.setOnClickListener {finish()}
    }

    private fun getHistory(){
        historyProvider.getHistoryById(extraId).addOnSuccessListener { document ->

            if(document.exists()){
                val history = document.toObject(History::class.java)
                binding.textViewOrigin.text = history?.origin
                binding.textViewDestination.text = history?.destination
                binding.textViewDate.text = RelativeTime.getTimeAgo(history?.timestamp!!,this@HistoriesDetailActivity)
                binding.textViewPrice.text = "${String.format("%.1f",history?.price)}$"
                binding.textViewMyCalification.text = "${history?.calificationToDriver}$"
                binding.textViewClientCalification.text = "${history?.calificationToClient}$"
                binding.textViewTimeAndDistance.text = "${history?.time} Min - ${String.format("%.1f", history?.km)} km"

                getDriverInfo(history?.idDriver!!)
            }

        }
    }

    private fun getDriverInfo(id:String){
        DriverProvider.getDriver(id).addOnSuccessListener { document ->
            if(document.exists()){
                val driver = document.toObject(Driver::class.java)
                binding.textViewEmail.text = driver?.email
                binding.textViewName.text = "${driver?.name} ${driver?.lastname}"
                if(driver?.image != null){
                    if(driver?.image != ""){
                        Glide.with(this).load(driver?.image).into(binding.circleImageProfile)
                    }
                }
            }
        }
    }
}