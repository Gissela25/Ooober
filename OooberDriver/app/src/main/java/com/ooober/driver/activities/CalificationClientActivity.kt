package com.ooober.driver.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.ooober.driver.databinding.ActivityCalificationClientBinding
import com.ooober.driver.models.History
import com.ooober.driver.providers.HistoryProvider

class CalificationClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalificationClientBinding
    private var extraPrice= 0.0
    private var historyProvider = HistoryProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalificationClientBinding.inflate(layoutInflater)
        setContentView(binding.root)
        extraPrice=intent.getDoubleExtra("price", 0.0)
        binding.textViewPrice.text = "Precio: $extraPrice"
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