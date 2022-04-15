package com.example.kreno_1_test


import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.kreno_1_test.databinding.ActivityTrustVerifyBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.spongycastle.jce.ECNamedCurveTable
import org.spongycastle.jce.interfaces.ECPrivateKey
import org.spongycastle.jce.interfaces.ECPublicKey
import org.spongycastle.jce.spec.ECPublicKeySpec
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import javax.crypto.KeyAgreement


var binding : ActivityTrustVerifyBinding? = null
var database: FirebaseDatabase? = null
var auth: FirebaseAuth? = null
var senderUid :String? = null
var receiverUid :String? = null
//const val TAG:String = "MyActivity"










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
            //it?.apply { isEnabled = false; postDelayed({ isEnabled = true }, 400) }

            // Add BouncyCastle
            Security.removeProvider("BC");
            Security.addProvider(org.spongycastle.jce.provider.BouncyCastleProvider())


            val generator = KeyPairGenerator.getInstance("ECDH")
            val ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1")
            generator.initialize(ecSpec)
            val keyPair = generator.generateKeyPair()
            val publicKey = keyPair.public as ECPublicKey
            val publicKeyBytes = publicKey.q.getEncoded(true)


            val privateKey = keyPair.private as ECPrivateKey
            val privateKeyBytes = privateKey.encoded


            val privateKeyString = Base64.getEncoder().encodeToString(privateKeyBytes)
            val pref: SharedPreferences = getSharedPreferences("PREFERENCE_PRIVATE", MODE_PRIVATE)
            val editor: SharedPreferences.Editor = pref.edit()
            editor.putString("PrivateKey", privateKeyString)
            editor.apply()



            val pubKeyString: String = Base64.getEncoder().encodeToString(publicKeyBytes)
            val currentId = FirebaseAuth.getInstance().uid
            database!!.reference.child("KrenoEcies")
                .child(currentId!!).setValue(pubKeyString)
        }
        binding!!.okBtn.setOnClickListener {

            receiverUid = intent.getStringExtra("receiverUid")
            database!!.reference.child("KrenoEcies")
                .child(receiverUid!!)
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot){
                        val pubKeyString2 = snapshot.getValue(String::class.java)
                        val pubKeyBytes = Base64.getDecoder().decode(pubKeyString2!!)
                        val refactoredPublicKey = getPublicKey(pubKeyBytes)


                        val pref: SharedPreferences = getSharedPreferences("PREFERENCE_PRIVATE", Context.MODE_PRIVATE)
                        val privateKeyString = pref.getString("PrivateKey", "DEFAULT")
                        val refactoredPrivateKey = getPrivateKey(privateKeyString!!)

                        val aliceAndBobShared = getSharedSecret(refactoredPrivateKey, refactoredPublicKey)

                        //val aliceAndBobSharedString = Base64.getEncoder().encodeToString(aliceAndBobShared)
                        //binding!!.test.text = aliceAndBobSharedString

                        val keyEcies = getAESKey(aliceAndBobShared)

                        val keyEciesString = Base64.getEncoder().encodeToString(keyEcies)
                        database!!.reference.child("ecies").child(FirebaseAuth.getInstance().uid!!).setValue(keyEciesString)

                        val pref1: SharedPreferences = getSharedPreferences("PREFERENCE_AES_KRENO", MODE_PRIVATE)
                        val editor: SharedPreferences.Editor = pref1.edit()
                        editor.putString("AES_KrenoKey", keyEciesString)
                        editor.apply()




                        finish()
                    }



                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@TrustVerify, "OTINANAI", Toast.LENGTH_SHORT).show()

                    }

                })
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
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(sharedSecret).copyOfRange(0, 32)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPublicKey(compressedPublicKey: ByteArray): PublicKey {
        // Add BouncyCastle
        Security.removeProvider("BC")
        Security.addProvider(org.spongycastle.jce.provider.BouncyCastleProvider())
        val ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1")
        val point = ecSpec.curve.decodePoint(compressedPublicKey)
        val publicKeySpec = ECPublicKeySpec(point, ecSpec)
        val keyFactory = KeyFactory.getInstance("ECDH")
        return keyFactory.generatePublic(publicKeySpec)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPrivateKey (key:String): PrivateKey{
        val privKeyBytes = Base64.getDecoder().decode(key)
        val temp1 = PKCS8EncodedKeySpec(privKeyBytes)
        val refactoredKey = KeyFactory.getInstance("ECDH")
        return refactoredKey.generatePrivate(temp1)

    }
}