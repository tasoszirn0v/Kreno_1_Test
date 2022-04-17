package com.example.kreno_1_test

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kreno_1_test.databinding.ActivityTrustVerifyBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.security.MessageDigest
import java.util.*

var binding : ActivityTrustVerifyBinding? = null
var database: FirebaseDatabase? = null
var auth: FirebaseAuth? = null
var senderUid :String? = null
var receiverUid :String? = null







class TrustVerify : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrustVerifyBinding.inflate(layoutInflater)
        setContentView(binding!!.root)


        val intent2 = intent
        receiverUid = intent2.getStringExtra("receiverUid")

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        senderUid = FirebaseAuth.getInstance().uid

        val uidByte1 = senderUid!!.toByteArray(Charsets.UTF_8)
        val uidByte2 = receiverUid!!.toByteArray(Charsets.UTF_8)
        val result1 = uidByte1.sum()
        val result2 = uidByte2.sum()
        val sumOfUid = result1 + result2
        val numString = sumOfUid.toString()
        val myTrustNumber= numString.sha(256)



        binding!!.a1.text = myTrustNumber.subSequence(0,4)
        binding!!.a2.text = myTrustNumber.subSequence(4,8)
        binding!!.a3.text = myTrustNumber.subSequence(8,12)
        binding!!.a4.text = myTrustNumber.subSequence(12,16)
        binding!!.a5.text = myTrustNumber.subSequence(16,20)
        binding!!.a6.text = myTrustNumber.subSequence(20,24)
        binding!!.a7.text = myTrustNumber.subSequence(24,28)
        binding!!.a8.text = myTrustNumber.subSequence(28,32)
        binding!!.a9.text = myTrustNumber.subSequence(32,36)
        binding!!.a10.text = myTrustNumber.subSequence(36,40)
        binding!!.a11.text = myTrustNumber.subSequence(40,44)
        binding!!.a12.text = myTrustNumber.subSequence(44,48)
        binding!!.a13.text = myTrustNumber.subSequence(48,52)
        binding!!.a14.text = myTrustNumber.subSequence(52,56)
        binding!!.a15.text = myTrustNumber.subSequence(56,60)
        binding!!.a16.text = myTrustNumber.subSequence(60,64)


        binding!!.okBtn.setOnClickListener {
            val intent = Intent(this@TrustVerify, Chat::class.java)
            startActivity(intent)
            finish()
        }

    }
    fun String.sha(algorithm: Int): String {
        val digest = MessageDigest.getInstance("SHA-${algorithm.toString()}")
        val bytes = digest.digest(this.toByteArray(Charsets.UTF_8))
        return bytes.fold("") { str, it -> str + "%02x".format(it)}
    }


}