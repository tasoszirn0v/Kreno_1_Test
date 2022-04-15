package com.example.kreno_1_test


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.kreno_1_test.databinding.ActivityTrustVerifyBinding
import com.example.kreno_1_test.model.KrenoEcies
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyPairGeneratorSpi.getInstance
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Security
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.KeyAgreement

var binding : ActivityTrustVerifyBinding? = null
var database: FirebaseDatabase? = null
var auth: FirebaseAuth? = null
var senderUid :String? = null
var receiverUid :String? = null
const val TAG:String = "MyActivity"










class TrustVerify : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrustVerifyBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        senderUid = FirebaseAuth.getInstance().uid
        val intent = intent
        receiverUid = intent.getStringExtra("receiverUid")



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




        binding!!.keysBtn.setOnClickListener {
            it?.apply { isEnabled = false; postDelayed({ isEnabled = true }, 400) }
            // Add BouncyCastle
            Security.removeProvider("BC")
            Security.addProvider(BouncyCastleProvider())

            val kpg = getInstance("EC")
            kpg.initialize(ECGenParameterSpec("secp521r1"))
            val keyPair = kpg.generateKeyPair()
            val privateKey1 = keyPair.private
            val privateKeyBytes = privateKey1.encoded
            /*
            for (byte in privateKeyBytes) {
                Log.i("myactivity", String.format("0x%20x", byte))
            }
             */
            val privateKeyString = Base64.getEncoder().encodeToString(privateKeyBytes)
            val pref: SharedPreferences = getSharedPreferences("PREFERENCE_PRIVATE", Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = pref.edit()
            editor.putString("PrivateKey", privateKeyString)
            editor.apply()

            val publicKey1 = keyPair.public
            val publicKeyBytes: ByteArray = publicKey1.encoded

            val pubKeyString: String = Base64.getEncoder().encodeToString(publicKeyBytes)
            database !!.reference.child("KrenoEcies")
                .child(FirebaseAuth.getInstance().uid!!)
                .push().setValue(pubKeyString)
        }
        binding!!.okBtn.setOnClickListener {


            database !!.reference.child("KrenoEcies")
                .child(receiverUid !!)
                .addListenerForSingleValueEvent(object : ValueEventListener {


                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val key = snapshot.getValue(KrenoEcies::class.java)

                        val pubKeyString2 = key!!.key

                        val refactoredPublicKey = getPublicKey(pubKeyString2!!)

                        val pref: SharedPreferences = getSharedPreferences("PREFERENCE_PRIVATE", Context.MODE_PRIVATE)
                        val privateKeyString = pref.getString("PrivateKey", "DEFAULT")
                        val refactoredPrivateKey = getPrivateKey(privateKeyString!!)




                        val aliceAndBobShared = getSharedSecret(refactoredPrivateKey, refactoredPublicKey!!)


                        val keyEcies = getAESKey(aliceAndBobShared)
                        val keyEciesString = Base64.getEncoder().encodeToString(keyEcies)

                        database!!.reference.child("ecies")
                            .push().setValue(keyEciesString)
                        }


                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@TrustVerify, "OTINANAI", Toast.LENGTH_SHORT).show()

                    }
                })

            val intent1 = Intent(this@TrustVerify, Chat::class.java)
            startActivity(intent1)
            finish()

        }

    }



    fun String.sha(algorithm: Int): String {
        val digest = MessageDigest.getInstance("SHA-${algorithm.toString()}")
        val bytes = digest.digest(this.toByteArray(Charsets.UTF_8))
        return bytes.fold("") { str, it -> str + "%02x".format(it)}
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getSharedSecret(privateKey: PrivateKey, publicKey: PublicKey): ByteArray{
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(publicKey, true)
        return keyAgreement.generateSecret()
    }

    private fun getAESKey(sharedSecret: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-512")
        return digest.digest(sharedSecret).copyOfRange(0, 32)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPublicKey (key:String): PublicKey?{
        val pubKeyBytes = Base64.getDecoder().decode(key)
        val temp1 = X509EncodedKeySpec(pubKeyBytes)
        val refactoredKey = java.security.KeyFactory.getInstance("EC")
        return refactoredKey.generatePublic(temp1)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPrivateKey (key:String): PrivateKey{
        val privKeyBytes = Base64.getDecoder().decode(key)
        val temp1 = PKCS8EncodedKeySpec(privKeyBytes)
        val refactoredKey = java.security.KeyFactory.getInstance("EC")
        return refactoredKey.generatePrivate(temp1)


    }




}