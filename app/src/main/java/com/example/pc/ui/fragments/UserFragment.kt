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
import com.example.pc.R
import com.example.pc.data.models.local.SellerType
import com.example.pc.data.models.network.User
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.UserRepository
import com.example.pc.databinding.FragmentUserBinding
import com.example.pc.ui.activities.LoginActivity
import com.example.pc.ui.activities.MainActivity
import com.example.pc.ui.viewmodels.UserModel
import com.example.pc.utils.OnDialogClicked
import com.example.pc.utils.makeDialog
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
                makeDialog(
                    requireContext(),
                    object : OnDialogClicked {
                        override fun onPositiveButtonClicked() {

                            //val imageUrl = uploadImage(imageUri)
                            val imageUrl = "https://www.gravatar.com/avatar/205e460b479e2e5b48aec07710c08d50"

                            viewModel.apply {
                                val userToAdd = User(
                                    binding!!.nameEditText.text.toString(),
                                    binding!!.phoneEditText.text.toString(),
                                    binding!!.emailEditText.text.toString(),
                                    binding!!.passwordEditText.text.toString(),
                                    city = binding!!.cityEditText.text.toString(),
                                    userType = binding!!.userTypeEditText.text.toString(),
                                    brand = binding!!.organisationNameEditText.text.toString(),
                                    imageUrl = imageUrl
                                )

                                Log.i(TAG, "user to add : $userToAdd")

                                signUp(userToAdd).observe(viewLifecycleOwner){
                                    if(it.isNullOrBlank()){
                                        //dialog ?
                                        Log.i(TAG, "return : $it")
                                        requireContext().toast(SGN_FAILED, Toast.LENGTH_LONG)
                                        goToHomeFragment()
                                    }else {
                                        Log.i(TAG, "return : $it")
                                        requireContext().toast(SGN_SUCCESS, Toast.LENGTH_LONG)
                                        goToLoginPage()
                                    }
                                }
                            }
                        }

                        override fun onNegativeButtonClicked() {
                        }
                    },
                    getString(R.string.confirm_user_title),
                    getString(R.string.confirm_user_message)
                )
            }

            imageSelection.setOnClickListener {
                //image selection intent
                setTheUploadImage()
            }

            viewModel.isValidInput.observe(viewLifecycleOwner){ isActive ->
                Log.i(TAG, "$isActive")
                binding!!.signUpButton.isEnabled = isActive
            }
        }
    }

    private fun goToHomeFragment(){
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
    }

    private fun goToLoginPage(){
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

    private fun setUpTheTypeEditText(){

        binding!!.userTypeTextField.editText?.setText(SellerType.SOLO.type)

        //to change !!!!!!!!!!!!??
        //set the adapter
        val values = SellerType.values().map {
            sellerType -> sellerType.type
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