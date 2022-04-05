package com.example.kreno_1_test


import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kreno_1_test.adapter.UserAdapter
import com.example.kreno_1_test.databinding.ActivityMainBinding
import com.example.kreno_1_test.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    var binding :ActivityMainBinding? =null
    var database :FirebaseDatabase? = null
    var users : ArrayList<User>? = null
    lateinit var auth: FirebaseAuth
    var usersAdapter : UserAdapter? = null
    var dialog : ProgressDialog? = null
    var user : User? = null






    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        dialog = ProgressDialog(this@MainActivity)
        dialog!!.setMessage("Uploading Image...")
        dialog!!.setCancelable(false)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        users = ArrayList<User>()
        usersAdapter = UserAdapter(this@MainActivity, users!!)
        val layoutManager = GridLayoutManager(this@MainActivity, 2)
        binding!!.mRec.layoutManager = layoutManager
        database!!.reference.child("users")
            .child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        usersAdapter = UserAdapter(this, users!!)
        binding!!.mRec.layoutManager = layoutManager
        binding!!.mRec.adapter = usersAdapter
        database!!.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users!!.clear()
                for (snapshot1 in snapshot.children) {
                    val user: User? = snapshot1.getValue(User::class.java)
                    if (!user!!.uid.equals(FirebaseAuth.getInstance().uid)) users!!.add(user)
                }
                usersAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}



        })


    }


    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!).setValue("Online")
    }
    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!).setValue("Offline")
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.logout){
            // code for logout

            auth.signOut()
            val intent = Intent(this@MainActivity, Krypto::class.java)

            startActivity(intent)
            finish()
            return true
        }
        if (item.itemId == R.id.help){
            // code for help
            Toast.makeText(this, "Help Clicked", Toast.LENGTH_LONG).show()

            return true
        }

        return super.onOptionsItemSelected(item)
    }

}