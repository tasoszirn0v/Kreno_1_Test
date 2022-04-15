package com.example.kreno_1_test

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.kreno_1_test.adapter.MessageAdapter
import com.example.kreno_1_test.databinding.ActivityChatBinding
import com.example.kreno_1_test.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

const val testKey1: String = "Xp2s5v8y/B?E(H+MbQeThWmYq3t6w9z\$"
const val ivTest: String = "E8+7YvHsvM5uD3eu"


class Chat : AppCompatActivity() {

    var binding: ActivityChatBinding? = null
    var adapter: MessageAdapter? = null
    var messages: ArrayList<Message>? = null
    var senderRoom: String? = null
    var receiverRoom: String? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var dialog: ProgressDialog? = null
    var senderUid: String? = null
    var receiverUid: String? = null





    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)





        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog = ProgressDialog(this@Chat)
        dialog!!.setMessage("Uploading image...")
        dialog!!.setCancelable(false)
        messages = ArrayList<Message>()




        //ENCRYPT & DECRYPT METHODS FOR SYMMETRIC PROTOCOL

        fun encrypt(strToEncrypt: String): ByteArray {
            val keySpec = SecretKeySpec(testKey1.toByteArray(), "AES")
            val iv = IvParameterSpec(ivTest.toByteArray())
            val plainText = strToEncrypt.toByteArray(Charsets.UTF_8)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
            return cipher.doFinal(plainText)
        }

        fun decrypt(dataToDecrypt: ByteArray): ByteArray {
            val iv = IvParameterSpec(ivTest.toByteArray())
            val keySpec = SecretKeySpec(testKey1.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
            return cipher.doFinal(dataToDecrypt)
        }






        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("image")
        binding!!.name.text = name

        Glide.with(this@Chat).load(profile)
            .placeholder(R.drawable.avatar)
            .into(binding!!.profile01)
        binding!!.imageView.setOnClickListener(View.OnClickListener { finish() })
        receiverUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence").child(receiverUid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        if (! status !!.isEmpty()) {
                            if (status == "offline") {
                                binding !!.status.visibility = View.GONE
                            } else {
                                binding !!.status.text = status
                                binding !!.status.visibility = View.VISIBLE
                            }
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {}

            })



        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid
        adapter = MessageAdapter(this@Chat, messages, senderRoom!!, receiverRoom!!)

        binding!!.chatRecyclerView.layoutManager = LinearLayoutManager(this@Chat)
        binding!!.chatRecyclerView.adapter = adapter
        database!!.reference.child("chats")
            .child(senderRoom!!)
            .child("messages")
            .addValueEventListener(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                    for (snapshot1 in snapshot.children) {
                        val message: Message? = snapshot1.getValue(Message::class.java)
                        //here will be decrypted the message
                        val pref1: SharedPreferences = getSharedPreferences("PREFERENCE_AES_KRENO", MODE_PRIVATE)
                        val aesKeyString = pref1.getString("AES_KrenoKey", "DEFAULT")
                        val aesKey = Base64.getDecoder().decode(aesKeyString)
                        val ivCiphertext: ByteArray = Base64.getDecoder().decode(message!!.message)
                        val decryptedBytes = decryptAes(aesKey, ivCiphertext)
                        val decryptedMessage = String(decryptedBytes, Charsets.UTF_8)
                        val message1 = Message(decryptedMessage, message.senderId, message.timeStamp)
                        message1.messageId = snapshot1.key
                        //message!!.messageId = snapshot1.key
                        messages!!.add(message1)


                    }
                    adapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        binding!!.sentButton.setOnClickListener(View.OnClickListener {

            val messageTxt: String = binding!!.messageBox.text.toString()
            // here will be encrypted the message
            val pref1: SharedPreferences = getSharedPreferences("PREFERENCE_AES_KRENO", MODE_PRIVATE)
            val aesKeyString = pref1.getString("AES_KrenoKey", "DEFAULT")
            val aesKey = Base64.getDecoder().decode(aesKeyString)
            val plaintext = messageTxt.toByteArray(Charsets.UTF_8)
            val ivCipherText = encryptAes(aesKey, plaintext)
            val ivCipherTextEncrypted = String(Base64.getEncoder().encode(ivCipherText))
            val date = Date()
            val message = Message(ivCipherTextEncrypted, senderUid, date.time)


            binding !!.messageBox.setText("")
            val randomKey = database!!.reference.push().key
            val lastMsgObj = HashMap<String, Any>()
            lastMsgObj["lastMsg"] = message.message!!
            lastMsgObj["lastMsgTime"] = date.time

            database!!.reference.child("chats").child(senderRoom!!)
                .updateChildren(lastMsgObj)
            database!!.reference.child("chats").child(receiverRoom!!)
                .updateChildren(lastMsgObj)
            database!!.reference.child("chats").child(senderRoom!!)
                .child("messages")
                .child(randomKey!!)
                .setValue(message).addOnSuccessListener {
                    database!!.reference.child("chats")
                        .child(receiverRoom!!)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message)
                        .addOnSuccessListener { }
                }

        })
        binding!!.attach.setOnClickListener(View.OnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        })

        val handler = Handler()
        binding!!.messageBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                database!!.reference.child("presence")
                    .child(senderUid!!)
                    .setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }

            var userStoppedTyping = Runnable {
                database!!.reference.child("presence")
                    .child(senderUid!!)
                    .setValue("Online")
            }

        })
        supportActionBar?.setDisplayShowTitleEnabled(false)





        binding!!.setTrust.text = "Επαλήθευση του κωδικού ασφαλείας του χρήστη $name"
        binding!!.setTrust.setOnClickListener {

            val intent = Intent(this@Chat, TrustVerify::class.java)
            intent.putExtra("receiverUid", receiverUid)
            startActivity(intent)


        }
    }






    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25) {
            if (data != null) {
                if (data.data!= null) {
                    val selectedImage = data.data
                    val calendar = Calendar.getInstance()
                    val reference = storage!!.reference.child("chats")
                        .child(calendar.timeInMillis.toString() + "")
                    dialog!!.show()
                    reference.putFile(selectedImage!!)
                        .addOnCompleteListener { task ->
                            dialog!!.dismiss()
                            if (task.isSuccessful) {
                                reference.downloadUrl.addOnSuccessListener { uri ->
                                    val filePath = uri.toString()
                                    val messageTxt: String = binding!!.messageBox.text.toString()
                                    val date = Date()
                                    val message = Message(messageTxt, senderUid, date.time)
                                    message.message = "photo"
                                    message.imageUrl = filePath
                                    binding!!.messageBox.setText("")
                                    val randomKey = database!!.reference.push().key
                                    val lastMsgObj = HashMap<String, Any>()
                                    lastMsgObj["lastMsg"] = message.message!!
                                    lastMsgObj["lastMsgTime"] = date.time
                                    database!!.reference.child("chats")
                                        .updateChildren(lastMsgObj)
                                    database!!.reference.child("chats")
                                        .child(receiverRoom!!)
                                    database!!.reference.child("chats")
                                        .child(senderRoom!!)
                                        .child("messages")
                                        .child(randomKey!!)
                                        .setValue(message).addOnSuccessListener {
                                            database!!.reference.child("chats")
                                                .child(receiverRoom!!)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message)
                                                .addOnSuccessListener {

                                                }
                                        }


                                }
                            }

                        }
                }
            }
        }

    }






    private fun encryptAes(aesKey: ByteArray, plaintext: ByteArray): ByteArray {
        val secretKeySpec = SecretKeySpec(aesKey, "AES")
        val iv = ByteArray(12) // Create random IV, 12 bytes for GCM
        SecureRandom().nextBytes(iv)
        val gCMParameterSpec = GCMParameterSpec(128, iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gCMParameterSpec)
        val ciphertext = cipher.doFinal(plaintext)
        val ivCiphertext = ByteArray(iv.size + ciphertext.size) // Concatenate IV and ciphertext (the MAC is implicitly appended to the ciphertext)
        System.arraycopy(iv, 0, ivCiphertext, 0, iv.size)
        System.arraycopy(ciphertext, 0, ivCiphertext, iv.size, ciphertext.size)
        return ivCiphertext
    }

    private fun decryptAes(aesKey: ByteArray, ivCiphertext: ByteArray): ByteArray {
        val secretKeySpec = SecretKeySpec(aesKey, "AES")
        val iv = ivCiphertext.copyOfRange(0, 12) // Separate IV
        val ciphertext = ivCiphertext.copyOfRange(12, ivCiphertext.size) // Separate ciphertext (the MAC is implicitly separated from the ciphertext)
        val gCMParameterSpec = GCMParameterSpec(128, iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gCMParameterSpec)
        return cipher.doFinal(ciphertext)
    }




    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId !!)
            .setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!)
            .setValue("offline")
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}
