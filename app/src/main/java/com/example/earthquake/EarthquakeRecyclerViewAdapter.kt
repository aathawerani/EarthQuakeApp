package com.example.earthquake

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.earthquake.databinding.ListItemEarthquakeBinding
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class EarthquakeRecyclerViewAdapter(private val mEarthquakes: ArrayList<Earthquake>?) :
    RecyclerView.Adapter<EarthquakeRecyclerViewAdapter.ViewHolder>() {

    private val TIME_FORMAT: SimpleDateFormat = SimpleDateFormat("HH:mm", Locale.US)
    private val MAGNITUDE_FORMAT: NumberFormat = DecimalFormat("0.0")
    //private lateinit var binding : ListItemEarthquakeBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemEarthquakeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val earthquake = mEarthquakes?.get(position)
        holder.binding.setEarthquake(earthquake);
        holder.binding.executePendingBindings();
    }

    override fun getItemCount(): Int {
        return mEarthquakes?.size ?: 0
    }

    inner class ViewHolder(val binding: ListItemEarthquakeBinding) : RecyclerView.ViewHolder(binding.getRoot()) {

        init {
            binding.setTimeformat(TIME_FORMAT)
            binding.setMagnitudeformat(MAGNITUDE_FORMAT)
        }

        override fun toString(): String {
            return super.toString() + " '" + (binding.details.text ?: "") + "'"
        }
    }
}