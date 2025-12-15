package com.example.kampus_bildirim

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    class MainActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            // activity_main.xml'deki buton
            val btnGoToSignUp = findViewById<Button>(R.id.btnGoToSignUp)

            btnGoToSignUp.setOnClickListener {
                //: Bu Activity'den SignUpActivity'ye geçme
                val intent = Intent(this, SignUpActivity::class.java)

                // Oluşturulan Intent çalıştır
                startActivity(intent)

            }
        }
    }}