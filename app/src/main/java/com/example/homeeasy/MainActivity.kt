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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }

        binding.securitySwitch.setOnCheckedChangeListener { _, isChecked ->
            setComponentData(
                isChecked,
                getString(R.string.component_child_secured),
                getString(R.string.enabled),
                getString(R.string.disabled),
                getString(R.string.security_failed_toast),
                binding.securityIndicator
            )
        }
        binding.doorSwitch.setOnCheckedChangeListener { _, isChecked ->
            setComponentData(
                isChecked,
                getString(R.string.component_child_door),
                getString(R.string.locked),
                getString(R.string.unlocked),
                getString(R.string.door_failed_toast),
                binding.doorIndicator
            )
        }
        binding.sensorSwitch.setOnCheckedChangeListener { _, isChecked ->
            setComponentData(
                isChecked,
                getString(R.string.component_child_ldr),
                getString(R.string.enabled),
                getString(R.string.disabled),
                getString(R.string.sensor_failed_toast),
                binding.sensorIndicator
            )
        }
        binding.bedroomSlider.addOnChangeListener { _, value, _ ->
            setPwmLedData(value, getString(R.string.led_child_bedroom))
        }
        binding.livingRoomSlider.addOnChangeListener { _, value, _ ->
            setPwmLedData(value, getString(R.string.led_child_livingRoom))
        }
        databaseListener()
    }

    private fun setPwmLedData(value: Float, childNode: String) {
        database = FirebaseDatabase.getInstance().getReference(getString(R.string.led_parent_node))
        database.child(childNode).setValue(value).addOnFailureListener {
            Toast.makeText(this, getString(R.string.pwm_failed_toast), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setComponentData(
        isChecked: Boolean,
        childNode:String,
        checkedTxtMsg:String,
        unCheckedTxtMsg:String,
        failureMsg:String,
        txtView: TextView,
        checkedTxtColor:String=getString(R.string.forest_green),
        unCheckedTxtColor:String=getString(R.string.red),
    ) {
        database = FirebaseDatabase.getInstance().getReference(getString(R.string.component_parent_node))
        database.child(childNode).setValue(isChecked).addOnSuccessListener {
            validateSwitchStatus(
                isChecked,
                checkedTxtMsg,
                checkedTxtColor,
                unCheckedTxtMsg,
                unCheckedTxtColor,
                txtView
            )
        }.addOnFailureListener {
            Toast.makeText(this, failureMsg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateSwitchStatus(
        isChecked: Boolean,
        checkedTxtMsg: String,
        checkedTxtColor: String,
        unCheckedTxtMsg: String,
        unCheckedTxtColor: String,
        txtView: TextView
    ) {
        if (isChecked) {
            textMsg = checkedTxtMsg
            textColor = checkedTxtColor
        } else {
            textMsg = unCheckedTxtMsg
            textColor = unCheckedTxtColor
        }
        txtView.text = textMsg
        txtView.setTextColor(Color.parseColor(textColor))
    }

    private fun databaseListener() {
        database = FirebaseDatabase.getInstance().reference
        val postListener = object :  ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val temperature = snapshot.child(getString(R.string.temp_child_node)).value.toString()
                val humidity = snapshot.child(getString(R.string.humidity_child_node)).value.toString()
                val motion = snapshot.child(getString(R.string.motion_child_node)).value.toString()
                val secured = snapshot.child(getString(R.string.secured_child_node)).value.toString()
                val doorLock = snapshot.child(getString(R.string.doorLocked_child_node)).value.toString()
                val ldr = snapshot.child(getString(R.string.ldr_child_node)).value.toString()

                checkAndAssignListenerData(temperature, humidity, secured, doorLock, ldr, motion)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, getString(R.string.listener_failed_toast), Toast.LENGTH_SHORT).show()
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
        binding.tvTemp.text = getString(R.string.temp_value, temperature)
        binding.tvHumidity.text = getString(R.string.humidity_value, humidity)

        binding.securitySwitch.isChecked = secured == "true"
        binding.doorSwitch.isChecked = doorLock == "true"
        binding.sensorSwitch.isChecked = ldr == "true"


        if (motion == "true" && secured == "true") motionTextBehavior(getString(R.string.detected), getString(R.string.red))
        else if (motion == "false" && secured == "true") motionTextBehavior(getString(R.string.enabled), getString(R.string.forest_green))
        else motionTextBehavior(getString(R.string.disabled), getString(R.string.black))
    }

    private fun motionTextBehavior(msg:String, color:String) {
        binding.motionIndicator.text = msg
        binding.motionIndicator.setTextColor(Color.parseColor(color))
    }
}