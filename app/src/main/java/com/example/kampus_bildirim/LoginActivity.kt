package com.example.kampus_bildirim

import android.content.Intent
import android.os.Bundle
import android.util.Log // Hata takibi için
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
// Firestore ile rol bilgisini çekmek için
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity" // Logcat etiketi

    // Bileşen tanımlama
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore // (Rol bilgisi için)
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var errorTextView: TextView
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var registerTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Firebase nesnelerini başlatma
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Bileşenlerini Koda Bağlama
        emailEditText = findViewById(R.id.edit_text_email)
        passwordEditText = findViewById(R.id.edit_text_password)
        loginButton = findViewById(R.id.button_login)
        errorTextView = findViewById(R.id.text_view_error)
        forgotPasswordTextView = findViewById(R.id.text_view_forgot_password)
        registerTextView = findViewById(R.id.text_view_register)

        errorTextView.visibility = View.GONE

        // butona tıklayınca ne olur
        loginButton.setOnClickListener {
            handleLogin()
        }

        forgotPasswordTextView.setOnClickListener {
            simulatePasswordReset() // şifre sıfırlama
        }

        registerTextView.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        //  Alan Kontrolü
        if (email.isEmpty() || password.isEmpty()) {
            showError("Lütfen e-posta ve şifrenizi giriniz.")
            return
        }

        // Giriş işlemi
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Giriş Başarılı
                    Log.d(TAG, "signInWithEmail:success")


                    fetchUserRoleAndNavigate()

                } else {
                    // Giriş Başarısız
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    showError("Giriş başarısız. E-posta veya şifrenizi kontrol edin.")
                }
            }
    }

    private fun fetchUserRoleAndNavigate() {
        // uid giriş yapan kullanıcının ıdsi
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                // Firestore'daki 'role' alanı
                val role = document.getString("role") ?: "User" //  yoksa varsayılan user

                // başarılı
                showSuccess(role)

                // Ana Sayfaya yönlendirme
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("USER_ROLE", role)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                // varsayılan rol ile devam et ve hata bildir.
                showError("Giriş başarılı ancak rol bilgisi alınamadı. Varsayılan kullanıcı rolü atanıyor.")
                // user kabul edip AnaSayfaya yönlendir
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("USER_ROLE", "User")
                startActivity(intent)
                finish()
            }
    }

    private fun simulatePasswordReset() {
        val email = emailEditText.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Şifre sıfırlama işlemi için lütfen e-posta alanını doldurunuz.", Toast.LENGTH_LONG).show()
            return
        }

        // Firebase Şifre Sıfırlama
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Şifre sıfırlama bilgi ekranı
                    Toast.makeText(this, "Şifre sıfırlama bağlantısı $email adresine gönderildi.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Şifre sıfırlama başarısız: " + task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun showError(message: String) {
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
    }

    private fun showSuccess(role: String) {
        errorTextView.visibility = View.GONE
        Toast.makeText(this, "$role rolüyle başarılı giriş. Ana Sayfaya yönlendiriliyor...", Toast.LENGTH_LONG).show()
    }
}