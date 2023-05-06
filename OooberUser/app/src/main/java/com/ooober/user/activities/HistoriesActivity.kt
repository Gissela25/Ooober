package com.ooober.user.activities

import android.graphics.Color
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ooober.user.R
import com.ooober.user.adapters.HistoriesAdapter
import com.ooober.user.databinding.ActivityHistoriesBinding
import com.ooober.user.databinding.ActivitySettingsBinding
import com.ooober.user.models.History
import com.ooober.user.providers.HistoryProvider

class HistoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoriesBinding
    private var historyProvider = HistoryProvider()
    private var histories = ArrayList<History>()
    private lateinit var adapter: HistoriesAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        val linearLayouManager = LinearLayoutManager(this)
        binding.recyclerViewHistories.layoutManager = linearLayouManager

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Historial de Viajes"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(Color.WHITE)

        getHistories()
    }

    private fun getHistories() {
        histories.clear()

        historyProvider.getHistory().get().addOnSuccessListener { query ->
            if(query != null){
                if(query.documents.size > 0){
                    val documents = query.documents

                    for(d in documents) {
                        var history = d.toObject(History::class.java)
                        history?.id = d.id
                        histories.add(history!!)
                    }

                    adapter = HistoriesAdapter(this@HistoriesActivity, histories)
                    binding.recyclerViewHistories.adapter = adapter
                }
            }
        }
    }
}