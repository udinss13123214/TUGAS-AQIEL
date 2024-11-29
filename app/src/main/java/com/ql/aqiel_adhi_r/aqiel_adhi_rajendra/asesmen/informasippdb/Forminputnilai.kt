package com.ql.aqiel_adhi_r.aqiel_adhi_rajendra.asesmen.informasippdb

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.io.File
import java.util.*

class Forminputnilai : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var storageRef: StorageReference
    private lateinit var sharedPreferences: SharedPreferences

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forminputnilai)

        database = FirebaseDatabase.getInstance("https://informasippdb-a32b5-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("user")
        storageRef = FirebaseStorage.getInstance("gs://informasippdb-a32b5.appspot.com").reference
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)

        val inputAlamat = findViewById<EditText>(R.id.inputAlamat)
        val inputNamaOrtu = findViewById<EditText>(R.id.inputNamaOrtu)
        val inputNoHp = findViewById<EditText>(R.id.inputNoHp)
        val inputBahasaInggris = findViewById<EditText>(R.id.inputBahasaInggris)
        val inputMatematika = findViewById<EditText>(R.id.inputMatematika)
        val inputBahasaIndonesia = findViewById<EditText>(R.id.inputBahasaIndonesia)
        val inputIPA = findViewById<EditText>(R.id.inputIPA)
        val textRata = findViewById<TextView>(R.id.Totalnilai)
        val buttonSave = findViewById<Button>(R.id.buttonSave)
        val imageUploadRapot = findViewById<ImageView>(R.id.image_upload_rapot)

        val spinnerAgama = findViewById<Spinner>(R.id.spinnerAgama)
        val agamaOptions = resources.getStringArray(R.array.agama_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, agamaOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAgama.adapter = adapter

        // Referensi RadioGroup untuk jenis kelamin
        val radioGroupGender = findViewById<RadioGroup>(R.id.radioGroupGender)

        // Fungsi untuk menghitung rata-rata
        fun updateRataRata() {
            val nilaiInggris = inputBahasaInggris.text.toString().toDoubleOrNull() ?: 0.0
            val nilaiMtk = inputMatematika.text.toString().toDoubleOrNull() ?: 0.0
            val nilaiIndo = inputBahasaIndonesia.text.toString().toDoubleOrNull() ?: 0.0
            val nilaiIpa = inputIPA.text.toString().toDoubleOrNull() ?: 0.0

            val rataRata = (nilaiInggris + nilaiMtk + nilaiIndo + nilaiIpa) / 4
            textRata.text = rataRata.toString()
        }

        // Fungsi untuk memvalidasi input dalam rentang 0-100
        fun validateInput(editText: EditText) {
            val value = editText.text.toString().toIntOrNull()
            if (value != null) {
                if (value < 0) editText.setText("0")
                if (value > 100) editText.setText("100")
            }
        }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateRataRata()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateInput(inputBahasaInggris)
                validateInput(inputMatematika)
                validateInput(inputBahasaIndonesia)
                validateInput(inputIPA)
            }
        }

        inputBahasaInggris.addTextChangedListener(textWatcher)
        inputMatematika.addTextChangedListener(textWatcher)
        inputBahasaIndonesia.addTextChangedListener(textWatcher)
        inputIPA.addTextChangedListener(textWatcher)

        // Membuka galeri untuk memilih foto rapor
        imageUploadRapot.setOnClickListener {
            openImagePicker()
        }

        // RadioGroup event listener
        radioGroupGender.setOnCheckedChangeListener { group, checkedId ->
            val nis = sharedPreferences.getString("NIS", "") ?: ""
            if (nis.isNotEmpty()) {
                val gender = when (checkedId) {
                    R.id.radioMale -> "Cowok"
                    R.id.radioFemale -> "Cewek"
                    else -> ""
                }
                if (gender.isNotEmpty()) {
                    database.child(nis).child("kelamin").setValue(gender)
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal memperbarui jenis kelamin", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "NIS tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }


        // Simpan data ke Firebase saat tombol simpan diklik
        buttonSave.setOnClickListener {
            val nis = sharedPreferences.getString("NIS", "") ?: ""
            if (nis.isNotEmpty()) {
                val alamat = inputAlamat.text.toString().trim()
                val agama = spinnerAgama.selectedItem.toString().trim()
                val namaOrtu = inputNamaOrtu.text.toString().trim()
                val noHp = inputNoHp.text.toString().trim()
                val nilaiInggris = inputBahasaInggris.text.toString().trim()
                val nilaiMtk = inputMatematika.text.toString().trim()
                val nilaiIndo = inputBahasaIndonesia.text.toString().trim()
                val nilaiIpa = inputIPA.text.toString().trim()
                val nilaiRataRata = textRata.text.toString().trim()

                if (alamat.isEmpty() || agama.isEmpty() || namaOrtu.isEmpty() || noHp.isEmpty() ||
                    nilaiInggris.isEmpty() || nilaiMtk.isEmpty() || nilaiIndo.isEmpty() || nilaiIpa.isEmpty()) {
                    Toast.makeText(this, "Semua data harus diisi", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (noHp.length != 12) {
                    Toast.makeText(this, "Nomor HP harus 12 digit", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (namaOrtu.length > 20) {
                    Toast.makeText(this, "Nama Orang Tua tidak boleh lebih dari 20 karakter", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (::imageUri.isInitialized) {
                    val fileSize = getFileSize(imageUri)
                    if (fileSize > 1_000_000) {
                        Toast.makeText(this, "Ukuran gambar terlalu besar. Pilih gambar yang lebih kecil dari 1MB.", Toast.LENGTH_SHORT).show()
                    } else {
                        uploadImageToFirebase(imageUri) { imageUrl ->
                            saveDataToFirebase(nis, alamat, agama, namaOrtu, noHp, nilaiInggris, nilaiMtk, nilaiIndo, nilaiIpa, nilaiRataRata, imageUrl)
                        }
                    }
                } else {
                    saveDataToFirebase(nis, alamat, agama, namaOrtu, noHp, nilaiInggris, nilaiMtk, nilaiIndo, nilaiIpa, nilaiRataRata, "")
                }
            } else {
                Toast.makeText(this, "NIS tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.data!!
            Picasso.get().load(imageUri).into(findViewById<ImageView>(R.id.image_upload_rapot))
        }
    }

    private fun getFileSize(imageUri: Uri): Long {
        val file = File(imageUri.path ?: "")
        return file.length()
    }

    private fun uploadImageToFirebase(imageUri: Uri, onComplete: (String) -> Unit) {
        val nis = sharedPreferences.getString("NIS", "") ?: ""
        val imageRef = storageRef.child("image_rapot/$nis/${UUID.randomUUID()}")
        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    onComplete(uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengunggah gambar", Toast.LENGTH_SHORT).show()
                onComplete("")
            }
    }

    private fun saveDataToFirebase(
        nis: String,
        alamat: String,
        agama: String,
        namaOrtu: String,
        noHp: String,
        nilaiInggris: String,
        nilaiMtk: String,
        nilaiIndo: String,
        nilaiIpa: String,
        nilaiRataRata: String,
        imageUrl: String
    ) {
        val userMap = mapOf(
            "alamat" to alamat,
            "agama" to agama,
            "namaOrangtua" to namaOrtu,
            "noHp" to noHp,
            "nilaiInggris" to nilaiInggris,
            "nilaiMtk" to nilaiMtk,
            "nilaiIndo" to nilaiIndo,
            "nilaiIpa" to nilaiIpa,
            "nilaiRataRata" to nilaiRataRata,
            "imageUrl" to imageUrl
        )

        database.child(nis).updateChildren(userMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()

                // Berpindah ke layout User
                val intent = Intent(this, User::class.java)
                startActivity(intent)

                finish() // Menutup Forminputnilai agar tidak bisa kembali ke halaman ini
            } else {
                Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
