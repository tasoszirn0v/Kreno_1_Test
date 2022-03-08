package com.example.kreno_1_test

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.kreno_1_test.databinding.ActivityE2eeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.functions.FirebaseFunctions
import com.virgilsecurity.android.common.callback.OnGetTokenCallback
import com.virgilsecurity.android.ethree.interaction.EThree
import com.virgilsecurity.common.callback.OnResultListener

class E2EE: AppCompatActivity() {

    var binding : ActivityE2eeBinding? = null
    var auth: FirebaseAuth? = null
    var database: FirebaseDatabase? = null





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityE2eeBinding.inflate(layoutInflater)
        setContentView(binding!!.root)



        binding!!.cryptoBtn.setOnClickListener (View.OnClickListener {
            // Fetch Virgil JWT token from Firebase function
            val tokenCallback =
                object : OnGetTokenCallback {
                    override fun onGetToken(): String {
                        val data = FirebaseFunctions.getInstance()
                            .getHttpsCallable("getVirgilJwt")
                            .call()
                            .result
                            ?.data as kotlin.collections.Map<String, String>

                        return data["token"]!!
                    }
                }

            val initializeListener =
                object : OnResultListener<EThree> {
                    override fun onSuccess(result: EThree) {
                        // Init done!
                        // Save the eThree instance
                    }

                    override fun onError(throwable: Throwable) {
                        // Error handling
                    }
                }
            // Initialize EThree SDK with JWT token from Firebase Function
            EThree.initialize(this, tokenCallback).addCallback(initializeListener)
        })

        //EThree.register().addCallback(object : OnCompleteListener {
            //override fun onSuccess() {
                // Done
            //}

            //override fun onError(throwable: Throwable) {
                // Error handling
            //}
        //})

// Generates new keypair for the user. Saves private key to the
// device and publishes public key to the Virgil's Cloud














    }

}