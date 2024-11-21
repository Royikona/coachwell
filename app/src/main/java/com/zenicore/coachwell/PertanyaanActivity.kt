package com.zenicore.coachwell

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.zenicore.coachwell.databinding.ActivityPertanyaanBinding

class PertanyaanActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var atletId: String
    private lateinit var binding: ActivityPertanyaanBinding
    private val pertanyaanList = mutableListOf<pertanyaan>()
    private var currentIndex = 0
    private val jawabanMap = mutableMapOf<String, MutableMap<String, Boolean>>()
    private val answeredQuestions = mutableMapOf<Int, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPertanyaanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mendapatkan ID atlet dari intent
        atletId = intent.getStringExtra("atletId") ?: ""
        database = FirebaseDatabase.getInstance("https://coachwell-ab27a-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("pertanyaan")

        // Memuat data pertanyaan dari Firebase
        loadPertanyaanFromFirebase()

        // Setup RecyclerView untuk grid angka
        setupRecyclerView()

        // Listener untuk tombol berikutnya
        binding.btnNextPertanyaan.setOnClickListener {
            val jawaban = binding.rgJawaban.checkedRadioButtonId == R.id.rbYes
            val pertanyaan = pertanyaanList[currentIndex]

            // Simpan jawaban berdasarkan kategori dan subkategori
            val kategoriMap = jawabanMap.getOrPut(pertanyaan.kategori) { mutableMapOf() }
            kategoriMap[pertanyaan.subkategori] = jawaban

            // Update answeredQuestions
            answeredQuestions[currentIndex] = true

            if (currentIndex < pertanyaanList.size - 1) {
                currentIndex++
                tampilkanPertanyaan()
            } else {
                simpanJawabanKeDatabase()
            }

            // Notifikasi agar RecyclerView diperbarui
            binding.rvQuestionGrid.adapter?.notifyDataSetChanged()
        }
    }

    // Memuat data pertanyaan dari Firebase
    private fun loadPertanyaanFromFirebase() {
        database.get().addOnSuccessListener { snapshot ->
            pertanyaanList.clear()
            for (child in snapshot.children) {
                val pertanyaan = child.getValue(pertanyaan::class.java)
                if (pertanyaan != null) {
                    pertanyaanList.add(pertanyaan)
                }
            }
            if (pertanyaanList.isNotEmpty()) {
                tampilkanPertanyaan()
                setupRecyclerView()
            } else {
                Toast.makeText(this, "Tidak ada pertanyaan tersedia", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.e("PertanyaanActivity", "Failed to load questions: ${exception.message}")
            Toast.makeText(this, "Gagal memuat pertanyaan: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Menampilkan pertanyaan berdasarkan indeks saat ini
    private fun tampilkanPertanyaan() {
        val pertanyaan = pertanyaanList[currentIndex]
        "Kategori: ${pertanyaan.subkategori}".also { binding.tvKategoriPertanyaan.text = it }
        binding.tvPertanyaan.text = pertanyaan.teksPertanyaan
        binding.rgJawaban.clearCheck()

        // Menampilkan jawaban yang sudah dipilih
        val jawaban = jawabanMap[pertanyaan.kategori]?.get(pertanyaan.subkategori)
        if (jawaban != null) {
            if (jawaban) {
                binding.rgJawaban.check(R.id.rbYes)
            } else {
                binding.rgJawaban.check(R.id.rbNo)
            }
        }

        binding.btnNextPertanyaan.text = if (currentIndex == pertanyaanList.size - 1) "Analisa" else "Berikutnya"
    }

    // Menyimpan jawaban ke Firebase
    private fun simpanJawabanKeDatabase() {
        // Menghitung jumlah jawaban 'Yes'
        var jumlahYes = 0
        for (kategoriMap in jawabanMap.values) {
            for (jawaban in kategoriMap.values) {
                if (jawaban) jumlahYes++
            }
        }

        // Menentukan kategori hasil
        val hasilKategori = when {
            jumlahYes <= 7 -> "Rendah"
            jumlahYes in 8..15 -> "Sedang"
            else -> "Tinggi"
        }

        // Simpan jawaban ke database
        val atletDatabase = FirebaseDatabase.getInstance("https://coachwell-ab27a-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("atlit")
            .child(atletId)

        atletDatabase.child("jawaban").setValue(jawabanMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("PertanyaanActivity", "Answers saved successfully")
                val intent = Intent(this, HasilAnalisisActivity::class.java)
                intent.putExtra("atletId", atletId)
                intent.putExtra("hasilKategori", hasilKategori)
                startActivity(intent)
                finish()
            } else {
                Log.e("PertanyaanActivity", "Failed to save answers: ${task.exception?.message}")
                Toast.makeText(this, "Gagal menyimpan jawaban: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Setup RecyclerView untuk grid angka
    private fun setupRecyclerView() {
        val gridAdapter = QuestionGridAdapter(pertanyaanList.size, answeredQuestions) { index ->
            currentIndex = index
            tampilkanPertanyaan()
        }

        binding.rvQuestionGrid.apply {
            layoutManager = GridLayoutManager(this@PertanyaanActivity, 5) // 5 kolom
            adapter = gridAdapter
        }
    }
}
