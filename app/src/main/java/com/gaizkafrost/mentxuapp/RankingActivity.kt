package com.gaizkafrost.mentxuapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gaizkafrost.mentxuapp.data.remote.api.RetrofitClient
import com.gaizkafrost.mentxuapp.data.remote.dto.RankingItemResponse
import kotlinx.coroutines.launch

class RankingActivity : BaseMenuActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: RankingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!resources.getBoolean(R.bool.is_tablet)) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContentView(R.layout.activity_ranking)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.rvRanking)
        progressBar = findViewById(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RankingAdapter()
        recyclerView.adapter = adapter

        fetchRanking()
    }

    private fun fetchRanking() {
        // 1. Cargar caché primero (si existe) para mostrar algo rápido
        val cachedRanking = getCachedRanking()
        if (cachedRanking.isNotEmpty()) {
            adapter.submitList(cachedRanking)
        } else {
            progressBar.visibility = View.VISIBLE
        }

        // 2. Intentar actualizar desde la red
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.obtenerRanking()
                progressBar.visibility = View.GONE
                
                if (response.isSuccessful) {
                    response.body()?.let { rankingList ->
                        // Actualizar UI
                        adapter.submitList(rankingList)
                        // Guardar en caché para la próxima vez
                        saveRankingToCache(rankingList)
                    }
                } else {
                    if (cachedRanking.isEmpty()) {
                        Toast.makeText(this@RankingActivity, getString(R.string.error_loading_ranking), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                if (cachedRanking.isEmpty()) {
                    Toast.makeText(this@RankingActivity, getString(R.string.network_error) + e.message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@RankingActivity, getString(R.string.offline_mode), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // --- Métodos de Caché (SharedPreferences) ---

    private fun saveRankingToCache(rankingList: List<RankingItemResponse>) {
        try {
            val sharedPreferences = getSharedPreferences("mentxu_cache", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val gson = com.google.gson.Gson()
            val json = gson.toJson(rankingList)
            editor.putString("cached_ranking", json)
            editor.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getCachedRanking(): List<RankingItemResponse> {
        return try {
            val sharedPreferences = getSharedPreferences("mentxu_cache", MODE_PRIVATE)
            val json = sharedPreferences.getString("cached_ranking", null)
            
            if (json != null) {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<List<RankingItemResponse>>() {}.type
                gson.fromJson(json, type)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

class RankingAdapter : RecyclerView.Adapter<RankingAdapter.ViewHolder>() {
    private var list: List<RankingItemResponse> = emptyList()

    fun submitList(newList: List<RankingItemResponse>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ranking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun getItemCount() = list.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvPosicion: TextView = view.findViewById(R.id.tvPosicion)
        private val tvNombre: TextView = view.findViewById(R.id.tvNombreUsuario)
        private val tvStops: TextView = view.findViewById(R.id.tvStopsCompletados)
        private val tvPuntos: TextView = view.findViewById(R.id.tvPuntuacionTotal)

        fun bind(item: RankingItemResponse) {
            tvPosicion.text = item.posicion.toString()
            tvNombre.text = item.nombre
            tvStops.text = "Paradas: ${item.paradasCompletadas}"
            tvPuntos.text = item.puntuacionTotal.toString()
            
            // Colores especiales para el podio
            when (item.posicion) {
                1 -> tvPosicion.setBackgroundResource(R.drawable.circle_gold)
                2 -> tvPosicion.setBackgroundResource(R.drawable.circle_silver)
                3 -> tvPosicion.setBackgroundResource(R.drawable.circle_bronze)
                else -> tvPosicion.setBackgroundResource(R.drawable.circle_background)
            }
        }
    }
}
