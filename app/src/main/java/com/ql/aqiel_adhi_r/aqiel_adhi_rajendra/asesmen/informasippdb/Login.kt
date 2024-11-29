package com.ql.aqiel_adhi_r.aqiel_adhi_rajendra.asesmen.informasippdb

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*
import android.widget.ImageView

import android.text.InputFilter

class Login : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var inputNIS: EditText
    private lateinit var inputPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var daftarTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var exiticon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inisialisasi Firebase Database
        database = FirebaseDatabase.getInstance("https://informasippdb-a32b5-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("user")

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)

        // Inisialisasi View
        inputNIS = findViewById(R.id.inputNIS)
        inputPassword = findViewById(R.id.inputPW)
        buttonLogin = findViewById(R.id.button_login)
        daftarTextView = findViewById(R.id.daftar)
        exiticon = findViewById(R.id.kembaliberanda)

        // Menambahkan InputFilter untuk membatasi input NIS menjadi 5 digit
        inputNIS.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(5))

        //kembali keberanda
        exiticon.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Listener untuk tombol login
        buttonLogin.setOnClickListener {
            val nis = inputNIS.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (nis.isNotEmpty() && password.isNotEmpty()) {
                checkLogin(nis, password)
            } else {
                Toast.makeText(this, "NIS dan Password kosong / Salah", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener untuk TextView daftar
        daftarTextView.setOnClickListener {
            // Navigasi ke halaman registrasi
            val intent = Intent(this, regis::class.java)
            startActivity(intent)
        }
    }

    private fun checkLogin(nis: String, password: String) {
        database.child(nis).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val storedPassword = snapshot.child("password").getValue(String::class.java)

                    if (storedPassword == password) {
                        Toast.makeText(this@Login, "Login Berhasil", Toast.LENGTH_SHORT).show()

                        // Simpan status login dan NIS di SharedPreferences
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("isLoggedIn", true)
                        editor.putString("NIS", nis)
                        editor.apply()

                        // Navigasi ke User Activity
                        val intent = Intent(this@Login, User::class.java)
                        startActivity(intent)
                        finish() // Tutup Login Activity agar tidak bisa kembali
                    } else {
                        Toast.makeText(this@Login, "Password Salah", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@Login, "NIS tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Login, "Terjadi kesalahan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
