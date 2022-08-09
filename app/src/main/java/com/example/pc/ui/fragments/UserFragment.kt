package com.example.pc.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.example.pc.R
import com.example.pc.data.models.local.SellerType
import com.example.pc.data.models.network.Status
import com.example.pc.data.models.network.User
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.UserRepository
import com.example.pc.databinding.FragmentUserBinding
import com.example.pc.ui.viewmodels.UserModel
import com.example.pc.utils.toast
import com.google.android.material.textfield.MaterialAutoCompleteTextView

private const val SGN_SUCCESS = "Compte Créé avec succès"
private const val SGN_FAILED = "Erreur lors du creation du compte"
private const val IMAGE_NOT_SELECTED = "Aucune image selectionnée"
private const val IMAGE_SELECTED = "Une image selectionnée"
private const val TAG = "UserFragment"

class UserFragment : Fragment() {

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var binding: FragmentUserBinding? = null
    private val retrofitService = RetrofitService.getInstance()
    private val repository = UserRepository(retrofitService)
    private val viewModel = UserModel(repository)
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                updateImageText(data?.clipData?.itemCount)
                imageUri = data?.data
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUserBinding.inflate(layoutInflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpTheTypeEditText()

        binding!!.apply {
            imageName.text = "Aucune image selectionnée"
            signUpButton.isEnabled = false

            nameEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    nameLiveData.value = text.toString()
                    nameHelperText.observe(viewLifecycleOwner){
                        nameTextField.helperText = it
                    }
                }
            }

            phoneEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    phoneLiveData.value = text.toString()
                    phoneHelperText.observe(viewLifecycleOwner){
                        phoneTextField.helperText = it
                    }
                }
            }

            emailEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    emailLiveData.value = text.toString()
                    emailHelperText.observe(viewLifecycleOwner){
                        emailTextField.helperText = it
                    }
                }
            }

            passwordEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    passwordLiveData.value = text.toString()
                    passwordHelperText.observe(viewLifecycleOwner){
                        passwordTextField.helperText = it
                    }
                }
            }

            retypePasswordEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.apply {
                    retypedPasswordLiveData.value = text.toString()
                    retypedPasswordHelperText.observe(viewLifecycleOwner){
                        retypePasswordTextField.helperText = it
                    }
                }
            }

            signUpButton.setOnClickListener {
                // to add a dialog ??

                val imageUrl = uploadImage(imageUri)

                viewModel.apply {
                    val userToAdd = User(
                        binding!!.nameEditText.text.toString(),
                        binding!!.phoneEditText.text.toString(),
                        binding!!.emailEditText.text.toString(),
                        binding!!.passwordEditText.text.toString(),
                        binding!!.cityEditText.text.toString(),
                        binding!!.userTypeEditText.text.toString(),
                        binding!!.organisationNameEditText.text.toString(),
                        imageUrl
                    )
                    val response = signUp(userToAdd)

                    if (response == null){
                        Log.e(TAG, "something went wrong in the request" )
                        requireActivity().toast(SGN_FAILED, Toast.LENGTH_LONG)

                        //go to home fragment after fail
                        goToHomeFragment()
                    }else{
                        Log.i(TAG, "signup successful")
                        requireActivity().toast(SGN_SUCCESS, Toast.LENGTH_LONG)

                        //go to home fragment after sign up
                        goToHomeFragment()
                    }
                }
            }

            imageSelection.setOnClickListener {
                //image selection intent
                setTheUploadImage()
            }
        }

        viewModel.isValidInput.observe(viewLifecycleOwner){ isActive ->
            Log.i(TAG, "$isActive")
            binding!!.signUpButton.isEnabled = isActive
        }
    }

    private fun goToHomeFragment(){
        val action = UserFragmentDirections.actionGlobalHomeFragment()
        findNavController().navigate(action)
    }

    private fun setUpTheTypeEditText(){
        binding!!.userTypeTextField.editText?.setText(Status.NEW.status)

        //to change !!!!!!!!!!!!??
        //set the adapter
        val items = SellerType.values()
        val values = mutableListOf<String>()

        for (element in items){
            values.add(element.type)
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, values)
        (binding!!.userTypeTextField.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setTheUploadImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private fun updateImageText(data: Any?){
        if (data == null) binding!!.imageName.text = IMAGE_NOT_SELECTED
        else binding!!.imageName.text = IMAGE_SELECTED
    }

    private fun uploadImage(uri: Uri?): String?{
        //to implement
        return if (uri == null) null
        else ""
    }
}