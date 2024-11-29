package com.ql.aqiel_adhi_r.aqiel_adhi_rajendra.asesmen.informasippdb

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import android.widget.ImageView
import com.ql.aqiel_adhi_r.aqiel_adhi_rajendra.asesmen.informasippdb.databinding.ActivityDaftarJurusanBinding

class Daftar_jurusan : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var adapter: JurusanAdapter
    private lateinit var binding: ActivityDaftarJurusanBinding
    private lateinit var keberanda: ImageView

    private val schoolList = listOf("SMAN_2", "SMAN_3", "SMAN_Batik", "SMK 4", "SMK 5", "SMK 6", "SMK 7")
    private val departmentMap = mapOf(
        "SMAN_2" to listOf("IPA", "IPS"),
        "SMAN_3" to listOf("IPA", "IPS"),
        "SMAN_Batik" to listOf("IPA", "IPS"),
        "SMK 4" to listOf("AKL", "RPL", "ULP"),
        "SMK 5" to listOf("AKL", "RPL", "ULP"),
        "SMK 6" to listOf("AKL", "RPL", "ULP"),
        "SMK 7" to listOf("AKL", "RPL", "ULP")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaftarJurusanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        keberanda = findViewById(R.id.kembali)

        keberanda.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        database = FirebaseDatabase.getInstance("https://informasippdb-a32b5-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("sekolah")

        // Setup Spinner Sekolah
        val schoolAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, schoolList)
        schoolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.schoolSpinner.adapter = schoolAdapter

        // Setup RecyclerView dengan LinearLayoutManager vertikal dan adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = JurusanAdapter(listOf())
        binding.recyclerView.adapter = adapter

        // Listener Spinner Sekolah
        binding.schoolSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedSchool = schoolList[position]
                loadDepartments(selectedSchool) // Muat jurusan sesuai sekolah yang dipilih
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Listener Spinner Jurusan
        binding.departmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedSchool = binding.schoolSpinner.selectedItem.toString()
                val selectedDepartment = binding.departmentSpinner.selectedItem.toString()
                fetchData(selectedSchool, selectedDepartment) // Tampilkan data berdasarkan sekolah & jurusan
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadDepartments(school: String) {
        val departments = departmentMap[school] ?: listOf()
        val departmentAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departments)
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.departmentSpinner.adapter = departmentAdapter
        binding.departmentSpinner.visibility = View.VISIBLE
    }

    private fun fetchData(school: String, department: String) {
        database.child(school).child(department).addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val jurusanList = mutableListOf<Jurusan>()

                // Periksa apakah snapshot memiliki data
                if (!snapshot.exists()) {
                    Log.e("fetchData", "Data tidak ditemukan untuk sekolah $school dan jurusan $department")
                    // Kosongkan tampilan jika jurusan tidak memiliki data
                    adapter.updateData(emptyList())
                    return
                }

                // Menambahkan data jurusan ke dalam list
                for (nisSnapshot in snapshot.children) {
                    val nilaiTotal = nisSnapshot.child("nilaiRataRata").getValue(String::class.java)?.toFloatOrNull() ?: 0.0f
                    val nama = nisSnapshot.child("nama").getValue(String::class.java) ?: "Tidak Ada Nama"

                    // Menambahkan jurusan ke dalam list
                    jurusanList.add(Jurusan(0, nama, nilaiTotal)) // No sementara diatur 0 dulu
                }

                // Mengurutkan list jurusan berdasarkan nilaiTotal tertinggi
                val sortedJurusanList = jurusanList.sortedByDescending { it.nilaiTotal }

                // Mengupdate nomor urut setelah data diurutkan
                val updatedJurusanList = sortedJurusanList.mapIndexed { index, jurusan ->
                    jurusan.copy(no = index + 1)  // Nomor urut dimulai dari 1
                }

                // Update adapter dengan data yang sudah diurutkan dan nomor urut yang benar
                adapter.updateData(updatedJurusanList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Tangani kesalahan jika terjadi
            }
        })
    }
}
