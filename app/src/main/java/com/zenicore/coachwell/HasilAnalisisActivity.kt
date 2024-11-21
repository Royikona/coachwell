package com.zenicore.coachwell

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.zenicore.coachwell.databinding.ActivityHasilAnalisisBinding

class HasilAnalisisActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var radarChart: RadarChart
    private lateinit var binding: ActivityHasilAnalisisBinding
    private lateinit var atletId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHasilAnalisisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        atletId = intent.getStringExtra("atletId") ?: ""
        radarChart = binding.radarChart
        database = FirebaseDatabase.getInstance("https://coachwell-ab27a-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("atlit")
            .child(atletId)

        binding.btnBackToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        ambilDataAnalisis()
    }

    private fun ambilDataAnalisis() {
        database.child("jawaban").get().addOnSuccessListener { snapshot ->
            val jawabanMap = snapshot.getValue(object : GenericTypeIndicator<Map<String, Map<String, Boolean>>>() {})
            if (jawabanMap == null) {
                Log.e("HasilAnalisisActivity", "jawabanMap is null or empty")
                return@addOnSuccessListener
            }
            Log.d("HasilAnalisisActivity", "jawabanMap: $jawabanMap")

            val kategoriMap = categorizeAnswers(jawabanMap)

            tampilkanGrafikRadar(kategoriMap)
            tampilkanDetailHasil(kategoriMap)
        }.addOnFailureListener { exception ->
            Log.e("HasilAnalisisActivity", "Error fetching data", exception)
        }
    }

    private fun categorizeAnswers(jawabanMap: Map<String, Map<String, Boolean>>): Map<String, Int> {
        val kategoriCounts = mutableMapOf<String, Int>()

        // Ambil data dari setiap kategori dengan key yang sesuai
        kategoriCounts["Physical"] = jawabanMap["Physical Symptom"]?.count { it.value } ?: 0
        kategoriCounts["Cognitive"] = jawabanMap["Cognitive Symptom"]?.count { it.value } ?: 0
        kategoriCounts["Emotional"] = jawabanMap["Emotional Symptom"]?.count { it.value } ?: 0
        kategoriCounts["Behavioral"] = jawabanMap["Behavioral Symptom"]?.count { it.value } ?: 0

        Log.d("HasilAnalisisActivity", "kategoriCounts: $kategoriCounts") // Log untuk cek hasil perhitungan

        return kategoriCounts
    }

    private fun tampilkanGrafikRadar(kategoriCounts: Map<String, Int>) {
        val entries = kategoriCounts.map { RadarEntry(it.value.toFloat()) }
        val radarDataSet = RadarDataSet(entries, "Tingkat Stres")
        radarDataSet.color = Color.BLUE
        radarDataSet.fillColor = Color.BLUE
        radarDataSet.setDrawFilled(true)

        val radarData = RadarData(radarDataSet)
        radarChart.data = radarData

        val labels = listOf("Physical", "Cognitive", "Emotional", "Behavioral")
        radarChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        radarChart.invalidate() // Refresh chart
    }

    private fun tampilkanDetailHasil(kategoriCounts: Map<String, Int>) {
        val totalYes = kategoriCounts.values.sum()
        val hasilKategori = when {
            totalYes <= 7 -> "Rendah"
            totalYes in 8..15 -> "Sedang"
            else -> "Tinggi"
        }

        // Tampilkan hasil kategori dan detail
        binding.tvHasilKategori.text = "Tingkat Stres: $hasilKategori"
        binding.tvDetailHasil.text = """
            Jumlah Jawaban "Ya":
            Physical: ${kategoriCounts["Physical"]}
            Cognitive: ${kategoriCounts["Cognitive"]}
            Emotional: ${kategoriCounts["Emotional"]}
            Behavioral: ${kategoriCounts["Behavioral"]}
        """.trimIndent()

        // Rekomendasi berdasarkan hasil kategori
        binding.tvRekomendasi.text = when (hasilKategori) {
            "Rendah" -> "Atlet dalam kondisi baik. Teruskan dengan latihan."
            "Sedang" -> "Perhatikan kebutuhan mental atlet. Berikan dukungan."
            "Tinggi" -> "Pertimbangkan untuk melakukan intervensi profesional."
            else -> "Tidak ada rekomendasi."
        }
    }
}
