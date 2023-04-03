package com.example.carsellingandbuyingapp

import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ItemAdapter2(private val cars: MutableList<Item>, private val loggedInUser: Boolean, private val application: Username, private val username: String) :
    RecyclerView.Adapter<ItemAdapter2.ViewHolder>() {

    private var listener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null

    interface OnItemLongClickListener {
        fun onItemLongClick(view: View, position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val imageView2: ImageView = itemView.findViewById(R.id.imageView2)
        val imageView3: ImageView = itemView.findViewById(R.id.imageView3)
        val textView1: TextView = itemView.findViewById(R.id.textView1)
        val textView2: TextView = itemView.findViewById(R.id.textView2)
        val textView3: TextView = itemView.findViewById(R.id.textView3)
        val textView4: TextView = itemView.findViewById(R.id.textView4)
        val textView5: TextView = itemView.findViewById(R.id.textView5)
        val textView6: TextView = itemView.findViewById(R.id.textView6)
        val textView7: TextView = itemView.findViewById(R.id.reg)

        val removeButton = itemView.findViewById<ImageView>(R.id.removeButton)
        val soldButton = itemView.findViewById<ImageView>(R.id.soldButton)

        init {
            removeButton.visibility = View.INVISIBLE
            soldButton.visibility = View.INVISIBLE

            itemView.setOnClickListener {
                listener?.onItemClick(itemView, adapterPosition)
            }

            if(loggedInUser) {

                itemView.setOnLongClickListener { view ->
                    onItemLongClickListener?.onItemLongClick(view, adapterPosition)
                    itemView.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                    val animation: Animation = AlphaAnimation(0.0f, 1.0f)
                    animation.duration = 150L
                    removeButton.startAnimation(animation)
                    removeButton.visibility = View.VISIBLE

                    soldButton.startAnimation(animation)
                    soldButton.visibility = View.VISIBLE

                    soldButton.setOnClickListener {
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val item = cars[position]
                            val database = Firebase.database.getReference("cars")
                            val databaseUsers = Firebase.database.getReference("users")

                            application.sales += 1
                            val userRef = databaseUsers.child(username)

                            userRef.child("sales").get().addOnSuccessListener { snapshot ->
                                if (snapshot.exists()) {
                                    val currentSales = snapshot.value.toString().toInt()
                                    userRef.child("sales").setValue(currentSales + 1)
                                } else {
                                    // Handle the case where the "sales" field doesn't exist, if necessary
                                }
                            }.addOnFailureListener { exception ->
                                Log.e(TAG, "Failed to get the sales value: ${exception.message}")
                            }

                            database.child(item.text7).get().addOnSuccessListener { snapshot ->
                                if (snapshot.exists()) {

                                    val engineCapacity = snapshot.child("engineCapacity").value.toString().toInt()

                                    if (engineCapacity < 100) {
                                        application.ecoSales += 1
                                        userRef.child("ecoSales").get().addOnSuccessListener { snapshot ->
                                            if (snapshot.exists()) {
                                                val currentSales = snapshot.value.toString().toInt()
                                                userRef.child("ecoSales").setValue(currentSales + 1)
                                            } else {
                                                // Handle the case where the "sales" field doesn't exist, if necessary
                                            }
                                        }.addOnFailureListener { exception ->
                                            Log.e(TAG, "Failed to get the sales value: ${exception.message}")
                                        }
                                    }

                                    snapshot.ref.removeValue().addOnSuccessListener {
                                        cars.removeAt(position)
                                        notifyItemRemoved(position)
                                    }.addOnFailureListener { exception ->
                                        Log.e(
                                            TAG,
                                            "Failed to delete the database entry: ${exception.message}"
                                        )
                                    }
                                } else {
                                    Log.e(
                                        TAG,
                                        "Database entry not found for item at position $position"
                                    )
                                }
                            }
                        }
                    }

                    removeButton.setOnClickListener {
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val item = cars[position]
                            val database = Firebase.database.getReference("cars")

                            database.child(item.text7).get().addOnSuccessListener { snapshot ->
                                if (snapshot.exists()) {

                                    snapshot.ref.removeValue().addOnSuccessListener {
                                        cars.removeAt(position)
                                        notifyItemRemoved(position)
                                    }.addOnFailureListener { exception ->
                                        Log.e(
                                            TAG,
                                            "Failed to delete the database entry: ${exception.message}"
                                        )
                                    }
                                } else {
                                    Log.e(
                                        TAG,
                                        "Database entry not found for item at position $position"
                                    )
                                }
                            }
                        }
                    }

                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_card_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = cars[position]
        Glide.with(holder.itemView.context)
            .load(item.image2Url)
            .into(holder.imageView)
        holder.textView1.text = item.text1
        holder.textView2.text = item.text2
        holder.textView3.text = item.text3
        holder.textView4.text = item.text4
        holder.textView5.text = item.text5
        holder.textView6.text = item.text6
        holder.textView7.text = item.text7

        Glide.with(holder.itemView.context)
            .load(item.image1Url)
            .into(holder.imageView2)
        holder.textView1.text = item.text1
        holder.textView2.text = item.text2
        holder.textView3.text = item.text3
        holder.textView4.text = item.text4
        holder.textView5.text = item.text5
        holder.textView6.text = item.text6
        holder.textView7.text = item.text7

        Glide.with(holder.itemView.context)
            .load(item.image0Url)
            .into(holder.imageView3)
        holder.textView1.text = item.text1
        holder.textView2.text = item.text2
        holder.textView3.text = item.text3
        holder.textView4.text = item.text4
        holder.textView5.text = item.text5
        holder.textView6.text = item.text6
        holder.textView7.text = item.text7
    }

    override fun getItemCount(): Int {
        return cars.size
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.listener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        onItemLongClickListener = listener
    }

}


