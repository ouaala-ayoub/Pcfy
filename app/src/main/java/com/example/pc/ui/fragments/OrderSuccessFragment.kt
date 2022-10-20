package com.example.pc.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pc.R
import com.example.pc.databinding.FragmentOrderSuccessBinding

class OrderSuccessFragment : Fragment() {

    private lateinit var binding: FragmentOrderSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderSuccessBinding.inflate(inflater, container, false)

        binding.backToHome.setOnClickListener {
            requireActivity().finish()
        }

        return binding.root
    }

}