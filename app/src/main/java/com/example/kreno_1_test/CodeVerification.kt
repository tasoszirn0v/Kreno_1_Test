package com.example.kreno_1_test

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kreno_1_test.databinding.ActivityCodeVerificationBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class CodeVerification : AppCompatActivity() {

    var binding: ActivityCodeVerificationBinding? = null
    var verificationId: String? = null
    var auth: FirebaseAuth? = null
    var dialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCodeVerificationBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        dialog = ProgressDialog(this@CodeVerification)
        dialog!!.setMessage("Αποστολή κωδικού επαλήθευσης...")
        dialog!!.setCancelable(false)
        dialog!!.show()
        auth = FirebaseAuth.getInstance()
        supportActionBar?.hide()
        val phoneNumber = intent.getStringExtra("phoneNumber")
        binding!!.phoneLble.text = "Επαλήθευση του $phoneNumber"

//verifyPhoneNumber method https://firebase.google.com/docs/auth/android/phone-auth?authuser=0
        val options = PhoneAuthOptions.newBuilder(auth!!)
            .setPhoneNumber(phoneNumber!!)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this@CodeVerification)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


                override fun onCodeSent(
                    verifyId: String,
                    forceResendingToken: PhoneAuthProvider.ForceResendingToken
                ) {
                    super.onCodeSent(verifyId, forceResendingToken)
                    dialog!!.dismiss()
                    verificationId = verifyId
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    binding!!.otpView.requestFocus()
                }


                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    TODO("Not yet implemented")
                }


                override fun onVerificationFailed(p0: FirebaseException) {
                    TODO("Not yet implemented")
                }

            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        binding!!.otpView.setOtpCompletionListener { code ->
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            auth!!.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this@CodeVerification, SetupProfile::class.java)
                        startActivity(intent)
                        finishAffinity()
                    } else {
                        Toast.makeText(this@CodeVerification,
                            "Αποτυχημένη προσπάθεια",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}








