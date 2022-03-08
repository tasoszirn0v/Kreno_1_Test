package com.example.kreno_1_test

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kreno_1_test.databinding.ActivityAuthenticateBinding
import com.google.firebase.auth.FirebaseAuth

class Authenticate : AppCompatActivity() {

    var binding : ActivityAuthenticateBinding? = null
    var auth : FirebaseAuth? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticateBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        auth = FirebaseAuth.getInstance()
        if (auth!!.currentUser != null){
            val intent = Intent(this@Authenticate, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        supportActionBar?.hide()

        binding!!.editNumber.requestFocus()
        binding!!.continueBtn.setOnClickListener {
            val intent = Intent(this@Authenticate, CodeVerification::class.java)
            intent.putExtra("phoneNumber",binding!!.editNumber.text.toString())
            startActivity(intent)

        }
    }
}