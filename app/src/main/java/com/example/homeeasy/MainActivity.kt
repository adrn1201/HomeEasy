package com.example.homeeasy

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.homeeasy.databinding.ActivityMainBinding
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private lateinit var textMsg: String
    private lateinit var textColor: String
    private lateinit var toastMsg: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }

        binding.securitySwitch.setOnCheckedChangeListener { _, isChecked ->
            setData(
                isChecked,
                "secured",
                "Enabled",
                "Security is Enabled",
                "Disabled",
                "Security is Disabled",
                "FAILED to set new Security Value",
                binding.securityIndicator
            )
        }
        binding.doorSwitch.setOnCheckedChangeListener { _, isChecked ->
            setData(
                isChecked,
                "doorLocked",
                "Locked",
                "Door is Locked",
                "Unlocked",
                "Door is Unlocked",
                "FAILED to set new Door Lock Value",
                binding.doorIndicator
            )
        }
        binding.sensorSwitch.setOnCheckedChangeListener { _, isChecked ->
            setData(
                isChecked,
                "ldrEnabled",
                "Enabled",
                "Sensor is Enabled",
                "Disabled",
                "Sensor is Disabled",
                "FAILED to set new Sensor Value",
                binding.sensorIndicator
            )
        }
        databaseListener()
    }

    private fun setData(
        isChecked: Boolean,
        childNode:String,
        checkedTxtMsg:String,
        checkedToastMsg:String,
        unCheckedTxtMsg:String,
        unCheckedToastMsg:String,
        failureMsg:String,
        txtView: TextView,
        checkedTxtColor:String="#00FF00",
        unCheckedTxtColor:String="#FF0000",
    ) {
        database = FirebaseDatabase.getInstance().getReference("componentStatus")
        database.child(childNode).setValue(isChecked).addOnSuccessListener {
            validateSwitchStatus(
                isChecked,
                checkedTxtMsg,
                checkedTxtColor,
                checkedToastMsg,
                unCheckedTxtMsg,
                unCheckedTxtColor,
                unCheckedToastMsg,
                txtView
            )
            Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, failureMsg  , Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateSwitchStatus(
        isChecked: Boolean,
        checkedTxtMsg: String,
        checkedTxtColor: String,
        checkedToastMsg: String,
        unCheckedTxtMsg: String,
        unCheckedTxtColor: String,
        unCheckedToastMsg: String,
        txtView: TextView
    ) {
        if (isChecked) {
            textMsg = checkedTxtMsg
            textColor = checkedTxtColor
            toastMsg = checkedToastMsg
        } else {
            textMsg = unCheckedTxtMsg
            textColor = unCheckedTxtColor
            toastMsg = unCheckedToastMsg
        }
        txtView.text = textMsg
        txtView.setTextColor(Color.parseColor(textColor))
    }

    private fun databaseListener() {
        database = FirebaseDatabase.getInstance().reference
        val postListener = object :  ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val temperature = snapshot.child("sensor/temperature").value.toString()
                val humidity = snapshot.child("sensor/humidity").value.toString()
                val motion = snapshot.child("sensor/motion").value.toString()
                val secured = snapshot.child("componentStatus/secured").value.toString()
                val doorLock = snapshot.child("componentStatus/doorLocked").value.toString()
                val ldr = snapshot.child("componentStatus/ldrEnabled").value.toString()

                checkAndAssignListenerData(temperature, humidity, secured, doorLock, ldr, motion)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to read data", Toast.LENGTH_SHORT).show()
            }
        }
        database.addValueEventListener(postListener)
    }

    private fun checkAndAssignListenerData(
        temperature: String,
        humidity: String,
        secured: String,
        doorLock: String,
        ldr: String,
        motion: String
    ) {
        binding.tvTemp.text = "$temperature°C"
        binding.tvHumidity.text = "$humidity%"

        binding.securitySwitch.isChecked = secured == "true"
        binding.doorSwitch.isChecked = doorLock == "true"
        binding.sensorSwitch.isChecked = ldr == "true"


        if (motion == "true" && secured == "true") motionTextBehavior("Detected", "#FF0000")
        else if (motion == "false" && secured == "true") motionTextBehavior("Enabled", "#00FF00")
        else motionTextBehavior("Disabled", "#000000")
    }

    private fun motionTextBehavior(msg:String, color:String) {
        binding.motionIndicator.text = msg
        binding.motionIndicator.setTextColor(Color.parseColor(color))
    }
}