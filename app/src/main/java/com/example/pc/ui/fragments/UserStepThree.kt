package com.example.pc.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.children
import com.example.pc.R
import com.example.pc.data.models.HandleSubmitInterface
import com.example.pc.data.models.local.SellerType
import com.example.pc.databinding.FragmentUserStepThreeBinding
import com.example.pc.ui.activities.UserCreateActivity
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

private const val TAG = "UserStepThree"

class UserStepThree : Fragment(), HandleSubmitInterface {

    private lateinit var binding: FragmentUserStepThreeBinding
    private lateinit var userCreateActivity: UserCreateActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentUserStepThreeBinding.inflate(inflater, container, false)
        setTheTypeEditText()

        return binding.root
    }

    override fun onNextClicked() {
        binding.apply {
            userCreateActivity = requireActivity() as UserCreateActivity

            val cityField = cityEditText.text.toString()
            val typeField = userTypeEditText.text.toString()
            val organization = organisationNameEditText.text.toString()

            userCreateActivity.requestBody.apply {
                if (cityField.isNotBlank()) {
                    addFormDataPart("city", cityField)
                }
                if (typeField.isNotBlank()) {
                    addFormDataPart("type", typeField)
                }
                if (organization.isNotBlank()) {
                    addFormDataPart("brand", organization)
                }
                for (v in linearLayout4.children){
                    v.isEnabled = false
                }
            }
        }
    }

    override fun onBackClicked() {
        //do Nothing no need
    }

    private fun setTheTypeEditText() {
        binding.userTypeTextField.editText?.setText(SellerType.SOLO.type)

        val values = SellerType.values().map { sellerType ->
            sellerType.type
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, values)
        (binding.userTypeTextField.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }
}