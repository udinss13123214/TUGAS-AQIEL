package com.ql.aqiel_adhi_r.aqiel_adhi_rajendra.asesmen.informasippdb

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import android.view.animation.AnimationUtils


class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var usericon: ImageView
    private lateinit var runningText: TextView
    private lateinit var cardSekolah: CardView
    private lateinit var cardsmk: CardView
    private lateinit var btnjurusan : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set layout activity_main
        setContentView(R.layout.activity_main)



        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false) // Mengambil status login
        val nilaiRata = sharedPreferences.getString("nilaiRataRata", "Kosong") // Mengambil nilai rata-rata dari SharedPreferences

        btnjurusan = findViewById(R.id.btnInfo)
        cardsmk = findViewById(R.id.buttonsmk)
        cardSekolah = findViewById(R.id.cardSekolah)

        //button jurusan
        btnjurusan.setOnClickListener {
            startActivity(Intent(this, Daftar_jurusan::class.java))
            finish()
        }

        // Cek status login dan nilai rata-rata saat klik cardsmk
        cardsmk.setOnClickListener {
            if (!isLoggedIn) {
                showLoginAlertDialog()
            } else if (nilaiRata == "Kosong") {
                showNilaiRataAlertDialog() // Tampilkan peringatan input nilai rata-rata
            } else {
                val intent = Intent(this, smk::class.java)
                startActivity(intent)
            }
        }

        // Cek status login dan nilai rata-rata saat klik cardSekolah
        cardSekolah.setOnClickListener {
            if (!isLoggedIn) {
                showLoginAlertDialog()
            } else if (nilaiRata == "Kosong") {
                showNilaiRataAlertDialog() // Tampilkan peringatan input nilai rata-rata
            } else {
                val intent = Intent(this, layoutsma::class.java)
                startActivity(intent)
            }
        }

        // Inisialisasi TextView untuk Running Text
        runningText = findViewById(R.id.running_text)
        runningText.isSelected = true // Set fokus agar marquee berjalan

        // Periksa status login saat aplikasi dijalankan
        if (!isLoggedIn) {
            Toast.makeText(this, "Anda belum login", Toast.LENGTH_SHORT).show()
        }

        // Inisialisasi icon user dan listener
        usericon = findViewById(R.id.user_icon)
        usericon.setOnClickListener {
            if (isLoggedIn) {
                val intent = Intent(this, User::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            }
        }
    }

    // Fungsi untuk menampilkan dialog peringatan login
    private fun showLoginAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Peringatan")
        builder.setMessage("Anda belum login, apakah Anda ingin login?")
        builder.setPositiveButton("Ya") { dialog, _ ->
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            dialog.dismiss()
        }
        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    // Fungsi untuk menampilkan dialog peringatan input nilai rata-rata
    private fun showNilaiRataAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Peringatan")
        builder.setMessage("Mohon input nilai rata-rata di profil pengguna.")
        builder.setPositiveButton("OK") { dialog, _ ->
            val intent = Intent(this, User::class.java)
            startActivity(intent) // Buka halaman profil pengguna untuk input nilai rata-rata
            dialog.dismiss()
        }
        builder.show()
    }
}
