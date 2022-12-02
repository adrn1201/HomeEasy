package com.example.homeeasy

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.homeeasy.databinding.ActivityMainBinding
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        databaseListener()
    }

    private fun databaseListener() {
        database = FirebaseDatabase.getInstance().reference
        val postListener = object :  ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val temperature = snapshot.child("sensor/temperature").value.toString()
                val humidity = snapshot.child("sensor/humidity").value.toString()
                val motion = snapshot.child("sensor/motion").value.toString()
                binding.tvTemp.text = "$temperature°C"
                binding.tvHumidity.text = "$humidity%"

                if(motion == "true"){
                    binding.motionIndicator.text = "Detected"
                    binding.motionIndicator.setTextColor(Color.parseColor("#FF0000"))
                } else{
                    binding.motionIndicator.text = "Enabled"
                    binding.motionIndicator.setTextColor(Color.parseColor("#00FF00"))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to read data", Toast.LENGTH_SHORT).show()
            }
        }
        database.addValueEventListener(postListener)
    }


}