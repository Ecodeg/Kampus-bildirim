package com.example.kampus_bildirim
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


// Firebase kütüphaneleri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    // Arayüz bileşenleri
    private lateinit var etNameSurname: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etUnit: EditText
    private lateinit var btnSignUp: Button


    // Firebase değişkenleri
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tasarım dosyası tanımlama
        setContentView(R.layout.activity_sign_up)

        // Bileşenleri ID'leri ile eşleştirme
        etNameSurname = findViewById(R.id.etNameSurname)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etUnit = findViewById(R.id.etUnit)
        btnSignUp = findViewById(R.id.btnSignUp)


        // Firebase nesnelerini başlatma
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Butona tıklandığında ne olacak
        btnSignUp.setOnClickListener {
            handleSignUp()
        }
    }

    private fun handleSignUp() {
        val nameSurname = etNameSurname.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val unit = etUnit.text.toString().trim()

        // Doğrulama kontrolü
        if (nameSurname.isEmpty() || email.isEmpty() || password.isEmpty() || unit.isEmpty() || password.length < 6) {
            Toast.makeText(this, "Lütfen tüm alanları doğru doldurunuz.", Toast.LENGTH_SHORT).show()
            return
        }

        // Kullanıcı Oluşturma
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Kayıt başarılı olursa
                    val user = auth.currentUser
                    Toast.makeText(this, "E-posta ile kimlik başarıyla oluşturuldu.", Toast.LENGTH_SHORT).show()

                    // Firestore'a kayıt
                    saveUserProfile(user, nameSurname, unit)
                }
                else {
                    // Kayıt başarısız olursa
                    val errorMessage = task.exception?.message ?: "Bilinmeyen bir hata oluştu."
                    Toast.makeText(this, "Kayıt Başarısız: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }

    // Firestore'a kayıt fonksiyonu
    private fun saveUserProfile(user: FirebaseUser?, nameSurname: String, unit: String) {
        if (user == null) return

        // Yeni kayıtlar : user
        val userData = hashMapOf(
            "name_surname" to nameSurname,
            "email" to user.email,
            "unit" to unit,
            "role" to "User"
        )

        // Kullanıcının UID'si ile users kısmına ekleme
        db.collection("users").document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Kullanıcı bilgileri başarıyla kaydedildi! Kayıt tamamlandı.", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Bilgi kaydı hatası: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}