package com.ooober.user.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.ooober.user.databinding.ActivityCalificationBinding
import com.ooober.user.models.History
import com.ooober.user.providers.HistoryProvider

class CalificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalificationBinding
    private var historyProvider = HistoryProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getHistory()
    }

    private fun getHistory(){
        historyProvider.getLastHistory().get().addOnSuccessListener {
                query ->
            if(query != null){
                if(query.documents.size >0)
                {
                    val history = query.documents[0].toObject(History::class.java)
                    Log.d("FIRESTORE","History: ${history?.toJson()}")
                }
                else{
                    Toast.makeText(this,"No se encontró un historial",Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}