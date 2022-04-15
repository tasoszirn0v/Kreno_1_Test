package com.example.kreno_1_test


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kreno_1_test.databinding.ActivityKryptoBinding


class Krypto: AppCompatActivity() {


    var binding: ActivityKryptoBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKryptoBinding.inflate(layoutInflater)
        setContentView(binding !!.root)







        binding !!.kryptoBtn.setOnClickListener {
            val intent = Intent(this@Krypto, Authenticate::class.java)
            startActivity(intent)
            finish()
        }

    }
}



