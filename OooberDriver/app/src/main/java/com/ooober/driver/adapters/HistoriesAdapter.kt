package com.ooober.driver.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
//import com.google.api.ResourceDescriptor.History
import com.ooober.driver.R
import com.ooober.driver.activities.HistoriesDetailActivity
import com.ooober.driver.models.History
import com.ooober.driver.utils.RelativeTime

class HistoriesAdapter(val context: Activity, val histories: ArrayList<History>): RecyclerView.Adapter<HistoriesAdapter.HistoriesAdapterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoriesAdapterViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_history, parent, false)
        return HistoriesAdapterViewHolder(view)
    }

    // El tamano de la lista que vamos a mostrar
    override fun getItemCount(): Int {
        return histories.size
    }
    //Establecer la informacion
    override fun onBindViewHolder(holder: HistoriesAdapterViewHolder, position: Int) {

        val history = histories[position] //Un solo historial
        holder.textViewOrigin.text = history.origin
        holder.textViewDestination.text = history.destination
        if(history.timestamp != null){
            holder.textViewDate.text = RelativeTime.getTimeAgo(history.timestamp!!, context)
        }

        holder.itemView.setOnClickListener{ goToDetail(history?.id!!) }
    }

    private fun goToDetail(idHistory: String){
        val i = Intent(context, HistoriesDetailActivity::class.java)
        i.putExtra("id", idHistory)
        context.startActivity(i)
    }

    class HistoriesAdapterViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val textViewOrigin: TextView
        val textViewDestination: TextView
        val textViewDate: TextView

        init {
            textViewOrigin = view.findViewById(R.id.textViewOrigin)
            textViewDestination = view.findViewById(R.id.textViewDestination)
            textViewDate = view.findViewById(R.id.textViewDate)
        }

    }


}