package com.ql.aqiel_adhi_r.aqiel_adhi_rajendra.asesmen.informasippdb

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import android.widget.ImageView

class smk : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var nis: String
    private lateinit var nilaiRataRata: String
    private var nama: String = ""

    private lateinit var btnDaftarSMA1: Button
    private lateinit var btnDaftarSMA2: Button
    private lateinit var btnDaftarBatik: Button
    private lateinit var btnDaftarSMA3: Button
    private lateinit var progressDialog: ProgressDialog
    private lateinit var keberanda: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smk)

        // Inisialisasi Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)

        // Ambil data dari SharedPreferences
        nis = sharedPreferences.getString("NIS", "") ?: ""
        nilaiRataRata = sharedPreferences.getString("nilaiRataRata", "") ?: ""

        // Inisialisasi tombol Daftar
        btnDaftarSMA1 = findViewById(R.id.buttondaftar)
        btnDaftarSMA2 = findViewById(R.id.daftarsma2)
        btnDaftarBatik = findViewById(R.id.daftarbatik)
        btnDaftarSMA3 = findViewById(R.id.daftarsma3)
        keberanda = findViewById(R.id.kembali)

        // Inisialisasi ProgressDialog
        progressDialog = ProgressDialog(this).apply {
            setMessage("Menyimpan data, harap tunggu...")
            setCancelable(false)
        }
        keberanda.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Cek apakah user sudah memilih 2 sekolah
        val selectedSchoolCount = sharedPreferences.getInt("selectedSchoolCount", 0)
        if (selectedSchoolCount >= 2) {
            hideAllButtons()
            showSelectionAlert()
        } else {
            // Set OnClickListener untuk tombol Daftar
            btnDaftarSMA1.setOnClickListener { handleSchoolSelection("SMK 4", btnDaftarSMA1) }
            btnDaftarSMA2.setOnClickListener { handleSchoolSelection("SMK 5", btnDaftarSMA2) }
            btnDaftarBatik.setOnClickListener { handleSchoolSelection("SMK 6", btnDaftarBatik) }
            btnDaftarSMA3.setOnClickListener { handleSchoolSelection("SMK 7", btnDaftarSMA3) }
        }
    }

    private fun showSelectionAlert() {
        AlertDialog.Builder(this)
            .setMessage("Anda sudah memilih 2 sekolah")
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun hideAllButtons() {
        btnDaftarSMA1.visibility = View.GONE
        btnDaftarSMA2.visibility = View.GONE
        btnDaftarBatik.visibility = View.GONE
        btnDaftarSMA3.visibility = View.GONE
    }

    private fun handleSchoolSelection(schoolName: String, button: Button) {
        val selectedSchoolCount = sharedPreferences.getInt("selectedSchoolCount", 0)

        if (selectedSchoolCount >= 2) {
            showSelectionAlert()
            hideAllButtons()
        } else {
            // Ambil nama user sebelum melanjutkan pendaftaran
            fetchUserName(schoolName)
        }
    }

    private fun fetchUserName(schoolName: String) {
        database.child("user").child(nis).child("nama").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                nama = snapshot.getValue(String::class.java) ?: "Nama tidak ditemukan"
                showJurusanDialog(schoolName)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@smk, "Gagal mengambil nama: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showJurusanDialog(schoolName: String) {
        val jurusanOptions = arrayOf("AKL", "RPL", "ULP")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pilih jurusan")
        builder.setItems(jurusanOptions) { dialog, which ->
            val selectedJurusan = jurusanOptions[which]
            Toast.makeText(this, "Anda memilih jurusan: $selectedJurusan di $schoolName", Toast.LENGTH_SHORT).show()
            saveToDatabase(schoolName, selectedJurusan)

            incrementSchoolSelectionCount()

            val updatedSelectedSchoolCount = sharedPreferences.getInt("selectedSchoolCount", 0)
            if (updatedSelectedSchoolCount >= 2) {
                showSelectionAlert()
                hideAllButtons()
            }
        }
        builder.setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun saveToDatabase(schoolName: String, jurusan: String) {
        progressDialog.show()

        val jurusanPath = "sekolah/$schoolName/$jurusan"
        val userData = UserData(nis, nama, nilaiRataRata, "Belum Disetujui")

        database.child(jurusanPath).child(nis).setValue(userData)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun incrementSchoolSelectionCount() {
        val selectedSchoolCount = sharedPreferences.getInt("selectedSchoolCount", 0) + 1
        sharedPreferences.edit().putInt("selectedSchoolCount", selectedSchoolCount).apply()
    }

    data class UserData(
        val nis: String,
        val nama: String,
        val nilaiRataRata: String,
        val status: String // Status "Belum Disetujui"
    )
}
