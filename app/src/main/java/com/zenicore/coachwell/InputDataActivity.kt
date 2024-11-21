package com.zenicore.coachwell

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.zenicore.coachwell.databinding.ActivityInputDataBinding

class InputDataActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var binding: ActivityInputDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_data)

        binding = ActivityInputDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance("https://coachwell-ab27a-default-rtdb.asia-southeast1.firebasedatabase.app").reference


        binding.btnNext.setOnClickListener {

            Log.d("InputDataActivity", "Button clicked")
            val nama = binding.etNama.text.toString()
            val umur = binding.etUmur.text.toString().toIntOrNull() ?: 0
            val tinggi = binding.etTinggi.text.toString().toIntOrNull() ?: 0
            val berat = binding.etBerat.text.toString().toIntOrNull() ?: 0

            val atletId = database.child("atlit").push().key
            val atlit = Atlit(id = atletId ?: "", nama = nama, umur = umur, tinggi = tinggi, berat = berat)

            database.child("atlit").child(atletId ?: "").setValue(atlit).addOnCompleteListener {
                if (it.isSuccessful) {
                    val intent = Intent(this, PertanyaanActivity::class.java)
                    intent.putExtra("atletId", atletId)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Gagal menyimpan data atlet", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

