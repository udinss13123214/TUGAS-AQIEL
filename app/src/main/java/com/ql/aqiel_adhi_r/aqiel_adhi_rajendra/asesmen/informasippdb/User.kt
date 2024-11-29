package com.ql.aqiel_adhi_r.aqiel_adhi_rajendra.asesmen.informasippdb

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.*

class User : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var userPP: ImageView
    private lateinit var namaTextView: TextView
    private lateinit var nisTextView: TextView
    private var isNamaChanged = false
    private lateinit var nilaiRataTextView: TextView
    private lateinit var pilihan1TextView: TextView
    private lateinit var pilihan2TextView: TextView
    private lateinit var buttonLogout: Button
    private lateinit var buttonBeranda: Button
    private lateinit var buttonHapusData: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var buttonEditNama: ImageView
    private lateinit var btnsetting : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        // Inisialisasi Firebase Database dan SharedPreferences
        database = FirebaseDatabase.getInstance("https://informasippdb-a32b5-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("user")
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)

        // Ambil NIS yang tersimpan
        val nis = sharedPreferences.getString("NIS", "")
        if (!nis.isNullOrEmpty()) {
            fetchUserData(nis) // Memanggil function untuk deteksi nilai rata-rata dan nis
        } else {
            redirectToLogin() // Kembali ke login jika NIS tidak ada
        }

        // Inisialisasi View
        btnsetting = findViewById(R.id.tombolsetting)
        buttonEditNama = findViewById(R.id.button_edit_Nama)
        userPP = findViewById(R.id.userPP)
        namaTextView = findViewById(R.id.isinama)
        nisTextView = findViewById(R.id.tempatnis)
        nilaiRataTextView = findViewById(R.id.nilairata)
        pilihan1TextView = findViewById(R.id.pilihan1)
        pilihan2TextView = findViewById(R.id.pilihan2)
        buttonLogout = findViewById(R.id.button_logout)
        buttonBeranda = findViewById(R.id.Keberanda)
        buttonHapusData = findViewById(R.id.hapusdata)



        // Listener untuk tombol logout
        buttonLogout.setOnClickListener {
            clearLoginStatus()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        //editnama
        buttonEditNama.setOnClickListener {
            editNama() // Panggil fungsi edit nama
        }
        btnsetting.setOnClickListener{
            showSettingsDialog()
        }

        // Listener untuk tombol beranda
        buttonBeranda.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Listener untuk tombol hapus data
        buttonHapusData.setOnClickListener {
            val nis = sharedPreferences.getString("NIS", "")
            showDeleteConfirmationDialog(nis)
        }
    }
//end here ganti pasword
private fun showSettingsDialog() {
    val options = arrayOf("Ganti Password")

    val builder = AlertDialog.Builder(this)
    builder.setTitle("SETTINGS")
    builder.setItems(options) { dialog, which ->
        when (which) {
            0 -> showChangePasswordDialog() // Tampilkan dialog kedua
        }
    }
    builder.setNegativeButton("Batal") { dialog, _ ->
        dialog.dismiss()
    }
    builder.show()
}

    private fun showChangePasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val input = EditText(this)
        input.hint = "Masukkan Password Baru"
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        builder.setTitle("Ganti Password")
        builder.setView(input)

        builder.setPositiveButton("Simpan") { dialog, _ ->
            val newPassword = input.text.toString().trim()

            if (newPassword.isEmpty()) {
                Toast.makeText(this, "Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            } else {
                // Simpan password baru ke Firebase
                saveNewPassword(newPassword)
                Toast.makeText(this, "Password berhasil diganti!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun saveNewPassword(newPassword: String) {
        // Ambil NIS dari SharedPreferences
        val sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)
        val nis = sharedPreferences.getString("NIS", "") ?: "" // Ambil NIS yang sudah tersimpan

        if (nis.isNotEmpty()) {
            // Dapatkan referensi ke Firebase Realtime Database
            val databaseRef = FirebaseDatabase.getInstance("https://informasippdb-a32b5-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("user")

            // Tentukan path berdasarkan NIS, NIS sekarang menjadi child dari "user"
            val path = "$nis" // NIS adalah child dari "user", misalnya "user/008"

            // Update password di Firebase
            val updates = mapOf("password" to newPassword)

            databaseRef.child(path).updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Password berhasil diganti!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal mengganti password: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "NIS tidak ditemukan!", Toast.LENGTH_SHORT).show()
        }
    }



    //endhere

    private fun fetchUserData(nis: String) {
        database.child(nis).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val nama = snapshot.child("nama").getValue(String::class.java) ?: "Nama tidak ditemukan"
                    val nilaiRata = snapshot.child("nilaiRataRata").getValue(String::class.java) ?: "Kosong"
                    val nilaiIndo = snapshot.child("nilaiIndo").getValue(String::class.java) ?: "-"
                    val nilaiInggris = snapshot.child("nilaiInggris").getValue(String::class.java) ?: "-"
                    val nilaiIpa = snapshot.child("nilaiIpa").getValue(String::class.java) ?: "-"
                    val nilaiMtk = snapshot.child("nilaiMtk").getValue(String::class.java) ?: "-"

                    namaTextView.text = nama
                    nisTextView.text = nis
                    nilaiRataTextView.text = nilaiRata

                    // Menampilkan nilai individu
                    findViewById<TextView>(R.id.outputIndo).text = nilaiIndo
                    findViewById<TextView>(R.id.outputInggris).text = nilaiInggris
                    findViewById<TextView>(R.id.outputIpa).text = nilaiIpa
                    findViewById<TextView>(R.id.outputMat).text = nilaiMtk

                    sharedPreferences.edit().putString("nilaiRataRata", nilaiRata).apply()

                    // Tampilkan alert jika nilai rata-rata kosong
                    if (nilaiRata == "Kosong") {
                        showNilaiKosongAlert()
                    } else {
                        // Menampilkan pilihan sekolah jika nilai tidak kosong
                        fetchSchoolSelection(nis)
                    }
                } else {
                    Toast.makeText(this@User, "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@User, "Terjadi kesalahan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchSchoolSelection(nis: String) {
        val schoolPath = database.root.child("sekolah")

        schoolPath.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var pilihan1Text = "Tidak ada pilihan 1"
                var pilihan2Text = "Tidak ada pilihan 2"

                // Iterasi pada SMAN_2, SMAN_3, dan SMAN_4 serta SMA Batik
                val smaSchools = listOf("SMAN_2", "SMAN_3", "SMAN_Batik")
                smaSchools.forEach { school ->
                    val smaNode = dataSnapshot.child(school)
                    listOf("IPA", "IPS").forEach { jurusan ->
                        val jurusanNode = smaNode.child(jurusan)
                        if (jurusanNode.hasChild(nis)) {
                            val selectedSchool = "$school Jurusan $jurusan"
                            if (pilihan1Text == "Tidak ada pilihan 1") {
                                pilihan1Text = selectedSchool
                            } else if (pilihan2Text == "Tidak ada pilihan 2") {
                                pilihan2Text = selectedSchool
                            }
                        }
                    }
                }

                // Iterasi pada SMK4 hingga SMK7
                for (i in 4..7) {
                    val smkNode = dataSnapshot.child("SMK $i")
                    listOf("AKL", "RPL", "ULP").forEach { jurusan ->
                        val jurusanNode = smkNode.child(jurusan)
                        if (jurusanNode.hasChild(nis)) {
                            val selectedSchool = "SMK $i Jurusan $jurusan"
                            if (pilihan1Text == "Tidak ada pilihan 1") {
                                pilihan1Text = selectedSchool
                            } else if (pilihan2Text == "Tidak ada pilihan 2") {
                                pilihan2Text = selectedSchool
                            }
                        }
                    }
                }

                // Set nilai pilihan ke TextView
                pilihan1TextView.text = pilihan1Text
                pilihan2TextView.text = pilihan2Text
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@User, "Gagal mengambil data sekolah: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun showDeleteConfirmationDialog(nis: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Penghapusan")
        builder.setMessage("Apakah Anda ingin menghapus data ini?")
        builder.setPositiveButton("Ya") { dialog: DialogInterface, _: Int ->
            if (!nis.isNullOrEmpty()) {
                deleteUserData(nis)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Tidak") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        builder.show()
    }
    //delete user data
    private fun deleteUserData(nis: String) {
        // Hapus data dari path 'user'
        database.child(nis).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Hapus data dari path 'sekolah' berdasarkan NIS
                val schoolPath = database.root.child("sekolah")
                schoolPath.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        dataSnapshot.children.forEach { schoolSnapshot ->
                            schoolSnapshot.children.forEach { jurusanSnapshot ->
                                if (jurusanSnapshot.hasChild(nis)) {
                                    // Hapus data user pada jurusan yang sesuai
                                    jurusanSnapshot.child(nis).ref.removeValue()
                                }
                            }
                        }
                        Toast.makeText(this@User, "Data berhasil dihapus dari user dan sekolah", Toast.LENGTH_SHORT).show()
                        clearLoginStatus()
                        redirectToLogin()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@User, "Gagal menghapus data sekolah: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "Gagal menghapus data user: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun clearLoginStatus() {
        sharedPreferences.edit().clear().apply()
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, Login::class.java))
        finish()
    }

    private fun showNilaiKosongAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Peringatan")
        builder.setMessage("Nilai rata-rata Anda kosong.")
        builder.setPositiveButton("OK") { _, _ ->
            val intent = Intent(this, Forminputnilai::class.java)
            startActivity(intent)
            finish()
        }
        builder.show()
    }



    //editnama
    // Fungsi untuk mengedit nama
    private fun editNama() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ganti Nama")

        val input = EditText(this)
        input.hint = "Masukkan nama baru"
        builder.setView(input)

        builder.setPositiveButton("Simpan") { dialog, _ ->
            val namaBaru = input.text.toString().trim()

            if (namaBaru.isNotEmpty()) {
                val nis = sharedPreferences.getString("NIS", "")

                if (!nis.isNullOrEmpty()) {
                    val userRef = database.child(nis).child("nama")
                    userRef.setValue(namaBaru).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            namaTextView.text = namaBaru
                            Toast.makeText(this, "Nama berhasil diperbarui di user", Toast.LENGTH_SHORT).show()

                            // Update nama di semua sekolah/jurusan
                            updateNamaDenganNIS(namaBaru, nis)
                        } else {
                            Toast.makeText(this, "Gagal memperbarui nama di user: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "NIS tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }


    private fun updateNamaDenganNIS(namaBaru: String, nis: String) {
        val databaseReff = FirebaseDatabase.getInstance().reference // Inisialisasi Firebase Realtime Database
        val schools = listOf("SMAN_2", "SMAN_3", "SMAN_Batik", "SMK 4", "SMK 5", "SMK 6", "SMK 7")
        val jurusanMap = mapOf(
            "SMAN_2" to listOf("IPS", "IPA"),
            "SMAN_3" to listOf("IPS", "IPA"),
            "SMAN_Batik" to listOf("IPS", "IPA"),
            "SMK_4" to listOf("AKL", "RPL", "ULP"),
            "SMK_5" to listOf("AKL", "RPL", "ULP"),
            "SMK_6" to listOf("AKL", "RPL", "ULP"),
            "SMK_7" to listOf("AKL", "RPL", "ULP")
        )

        val updatedPaths = mutableSetOf<String>() // Menyimpan path yang sudah diperbarui

        fun scanAndUpdate(ignorePaths: Set<String>) {
            var nisDitemukan = false

            for (jenis in schools) {
                val jurusanList = jurusanMap[jenis] ?: continue

                for (jurusan in jurusanList) {
                    val jurusanRef = databaseReff.child("sekolah").child(jenis).child(jurusan).child(nis)

                    val currentPath = "$jenis/$jurusan/$nis"
                    if (ignorePaths.contains(currentPath)) continue // Abaikan path yang sudah diperbarui sebelumnya

                    jurusanRef.get().addOnCompleteListener { task ->
                        if (task.isSuccessful && task.result.exists()) {
                            // Jika NIS ditemukan, update nama
                            jurusanRef.child("nama").setValue(namaBaru).addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Log.d("UpdateNama", "Nama berhasil diperbarui di $jenis jurusan $jurusan untuk NIS $nis")
                                    Toast.makeText(this, "Nama berhasil diperbarui di $jenis ($jurusan)", Toast.LENGTH_SHORT).show()
                                } else {
                                    Log.e("UpdateNama", "Gagal memperbarui nama di $jenis jurusan $jurusan: ${updateTask.exception?.message}")
                                    Toast.makeText(this, "Gagal memperbarui nama di $jenis ($jurusan)", Toast.LENGTH_SHORT).show()
                                }
                            }
                            updatedPaths.add(currentPath)
                            nisDitemukan = true
                            return@addOnCompleteListener
                        } else if (!task.isSuccessful) {
                            Log.e("UpdateNama", "Gagal membaca data di $jenis jurusan $jurusan: ${task.exception?.message}")
                        }
                    }

                    if (nisDitemukan) break // Jika NIS ditemukan, hentikan pencarian di jurusan lain
                }

                if (nisDitemukan) break // Jika NIS ditemukan, hentikan pencarian di sekolah lain
            }

            if (!nisDitemukan) {
                Log.e("UpdateNama", "NIS $nis tidak ditemukan di database.")
                Toast.makeText(this, "NIS tidak ditemukan di database.", Toast.LENGTH_SHORT).show()
            }
        }

        // Pindai pertama kali
        scanAndUpdate(emptySet())

        // Tambahkan jeda waktu atau logika untuk memindai ulang setelah update pertama selesai
        Handler(Looper.getMainLooper()).postDelayed({
            // Pindai ulang, abaikan path yang sudah diperbarui
            scanAndUpdate(updatedPaths)
        }, 2000) // Delay untuk memastikan update pertama selesai
    }




}

