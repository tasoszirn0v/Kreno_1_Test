package com.example.kreno_1_test.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kreno_1_test.Chat
import com.example.kreno_1_test.R
import com.example.kreno_1_test.databinding.ItemProfileBinding
import com.example.kreno_1_test.model.User

class UserAdapter(var context: Context, var userList:ArrayList<User>):
RecyclerView.Adapter<UserAdapter.UserViewerHolder>()

{
    inner class  UserViewerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemProfileBinding = ItemProfileBinding.bind(itemView)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewerHolder {
        var v = LayoutInflater.from(context).inflate(R.layout.item_profile,parent, false)
        return UserViewerHolder(v)
    }

    override fun onBindViewHolder(holder: UserViewerHolder, position: Int) {
       val user = userList[position]
        holder.binding.username.text = user.name
        Glide.with(context).load(user.profileImage)
            .placeholder(R.drawable.avatar)
            .into(holder.binding.profile)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, Chat::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("image", user.profileImage)
            intent.putExtra("uid", user.uid)
            context.startActivity(intent)

        }
    }

    override fun getItemCount(): Int = userList.size

}