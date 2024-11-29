package com.ql.aqiel_adhi_r.aqiel_adhi_rajendra.asesmen.informasippdb

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.ImageView

class regis : AppCompatActivity() {

    private lateinit var inputNIS: EditText
    private lateinit var inputPW: EditText
    private lateinit var inputNama: EditText
    private lateinit var buttonDaftar: Button
    private lateinit var database: DatabaseReference
    private lateinit var keberanda: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_regis)

        val loginTextView: TextView = findViewById(R.id.regis)
        loginTextView.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        database = FirebaseDatabase.getInstance("https://informasippdb-a32b5-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child("user")

        inputNIS = findViewById(R.id.inputNIS)
        inputPW = findViewById(R.id.inputPW)
        inputNama = findViewById(R.id.inputNama)
        buttonDaftar = findViewById(R.id.button_login)
        keberanda = findViewById(R.id.kembali)

        // Membatasi input NIS hanya 5 digit
        val filter = InputFilter.LengthFilter(5) // Membatasi hanya 5 karakter
        inputNIS.filters = arrayOf(filter)

        buttonDaftar.setOnClickListener {
            saveUserData()
        }

        keberanda.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveUserData() {
        val nis = inputNIS.text.toString().trim()
        val password = inputPW.text.toString().trim()
        val nama = inputNama.text.toString().trim()

        if (nis.isEmpty() || password.isEmpty() || nama.isEmpty()) {
            Toast.makeText(this, "Lengkapi semua field", Toast.LENGTH_SHORT).show()
            return
        }

        // Cek apakah NIS sudah ada di database
        database.child(nis).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                if (dataSnapshot.exists()) {
                    // Jika NIS sudah ada
                    Toast.makeText(this, "NIS sudah terdaftar!", Toast.LENGTH_SHORT).show()
                } else {
                    // Jika NIS belum ada, simpan data
                    val user = User(
                        nis = nis,
                        password = password,
                        nama = nama,
                        nilaiRataRata = "Kosong",
                        nilaiIpa = "Kosong",
                        nilaiMtk = "Kosong",
                        nilaiIndo = "Kosong",
                        nilaiInggris = "Kosong",
                        noHp = "Kosong",
                        namaOrangTua = "Kosong",
                        agama = "Kosong",
                        imageUrl = "Kosong",
                        kelamin = "Kosong"
                    )

                    database.child(nis).setValue(user)
                        .addOnCompleteListener { saveTask ->
                            if (saveTask.isSuccessful) {
                                Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                                navigateToLogin()
                            } else {
                                Toast.makeText(this, "Registrasi gagal: ${saveTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                Toast.makeText(this, "Gagal memeriksa NIS: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun navigateToLogin() {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }

    // Kelas data untuk struktur data pengguna
    data class User(
        val nis: String = "",
        val password: String = "",
        val nama: String = "",
        val nilaiRataRata: String = "Kosong",
        val nilaiIpa: String = "Kosong",
        val nilaiMtk: String = "Kosong",
        val nilaiIndo: String = "Kosong",
        val nilaiInggris: String = "Kosong",
        val noHp: String = "Kosong",
        val namaOrangTua: String = "Kosong",
        val agama: String = "Kosong",
        val imageUrl: String = "Kosong",
        val kelamin: String = "Kosong" // Menambahkan field kelamin
    )
}
