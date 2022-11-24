package com.example.pc.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.pc.R
import com.example.pc.databinding.ActivityFullOrdersBinding

private const val TAG = "FullOrdersActivity"

class FullOrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullOrdersBinding
    lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {

//        binding = ActivityFullOrdersBinding.inflate(layoutInflater)
        userId = intent.getStringExtra("id") as String

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_full_orders)
    }

}