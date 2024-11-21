package com.zenicore.coachwell

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var atlitAdapter: AtlitAdapter
    private lateinit var tvNoData: TextView
    private lateinit var fabAdd: FloatingActionButton
    private val atlitList = mutableListOf<Atlit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        tvNoData = findViewById(R.id.tvNoData)
        fabAdd = findViewById(R.id.fabAdd)

        recyclerView.layoutManager = LinearLayoutManager(this)
        database = FirebaseDatabase.getInstance("https://coachwell-ab27a-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("atlit")
        FirebaseApp.initializeApp(this)

        // Setup adapter untuk RecyclerView
        atlitAdapter = AtlitAdapter(atlitList) { atlet ->
            val intent = Intent(this, HasilAnalisisActivity::class.java)
            intent.putExtra("atletId", atlet.id) // Mengirimkan ID atlet
            startActivity(intent)
        }
        recyclerView.adapter = atlitAdapter

        fabAdd.setOnClickListener {
            val intent = Intent(this, InputDataActivity::class.java)
            startActivity(intent)
        }

        fetchAtlitData()
    }

    private fun fetchAtlitData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                atlitList.clear()
                for (data in snapshot.children) {
                    val atlit = data.getValue(Atlit::class.java)
                    if (atlit != null) {
                        atlit.id = data.key ?: ""
                        atlitList.add(atlit)
                    }
                }
                atlitAdapter.notifyDataSetChanged()
                tvNoData.visibility = if (atlitList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

