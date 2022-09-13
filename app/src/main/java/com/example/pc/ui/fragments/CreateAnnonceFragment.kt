package com.example.pc.ui.fragments

import android.app.Activity
import android.content.ClipData
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
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.pc.R
import com.example.pc.data.models.local.LoggedInUser
import com.example.pc.data.models.network.CategoryEnum
import com.example.pc.data.models.network.Status
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.CreateAnnonceRepository
import com.example.pc.data.repositories.LoginRepository
import com.example.pc.databinding.FragmentCreateAnnonceBinding
import com.example.pc.ui.activities.LoginActivity
import com.example.pc.ui.activities.MainActivity
import com.example.pc.ui.viewmodels.CreateAnnonceModel
import com.example.pc.ui.viewmodels.CreateAnnonceModelFactory
import com.example.pc.utils.OnDialogClicked
import com.example.pc.utils.makeDialog
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.example.pc.utils.toast

private const val TAG = "CreateAnnonceFragment"
private const val ERROR_MSG = "Erreur l'annonce n'est pas ajoutée"
private const val SUCCESS_MSG = "Annonce ajoutée avec succes"

class CreateAnnonceFragment : Fragment() {

    private var binding: FragmentCreateAnnonceBinding? = null
    private lateinit var viewModel: CreateAnnonceModel
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private val retrofitService = RetrofitService.getInstance()
    private lateinit var loginRepository: LoginRepository
    private lateinit var currentUser: LoggedInUser
    private var imagesUris = listOf<Uri>()

    //add the livedata validation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loginRepository = LoginRepository(
            retrofitService,
            requireContext().applicationContext
        )

        if (loginRepository.user != null) {
            resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val data: Intent? = result.data
                    Log.i(TAG, "image retrieved")
                    Log.i(TAG, "data is ${data?.data}: ")
                    updateImageText(data?.clipData?.itemCount)

                    if (data?.clipData != null) {
                        imagesUris = getImagesUris(data.clipData!!)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCreateAnnonceBinding.inflate(
            inflater,
            container,
            false
        )
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            CreateAnnonceModelFactory(CreateAnnonceRepository(retrofitService))
        )[CreateAnnonceModel::class.java]

        val isLoggedIn = loginRepository.isLoggedIn
        Log.i(TAG, "isLoggedIn: ${isLoggedIn.value}")

        isLoggedIn.observe(viewLifecycleOwner) { isLogged ->

            if (loginRepository.user == null) {
                Log.i(TAG, "isLogged in : $isLogged")

                makeDialog(
                    requireActivity(),
                    object: OnDialogClicked{
                        override fun onPositiveButtonClicked() {
                            goToLoginActivity()
                        }

                        override fun onNegativeButtonClicked() {
                            reloadActivity()
                        }
                    },
                    getString(R.string.confirm_login_title),
                    getString(R.string.confirm_login_message)
                ).show()
            }
            else {

                if (loginRepository.user == null) return@observe
                currentUser = loginRepository.user!!
                Log.i(TAG, "user id : $currentUser")

                setTheStatueEditTextView()
                setTheCategoriesEditText()
                validateTheData()

                binding!!.apply {

                    addButton.setOnClickListener {

                        makeDialog(
                            requireContext(),
                            object: OnDialogClicked {
                                override fun onPositiveButtonClicked() {
                                    var imagesList = mutableListOf<String>()

                                    if (imagesUris.isNotEmpty()){
                                        imagesList = uploadImages(imagesUris)
                                    }

                                    val annonceToAdd = viewModel.getTheAnnonce(
                                        viewModel.titleLiveData.value!!,
                                        binding!!.priceEditText.text.toString().toFloat(),
                                        imagesList,
                                        binding!!.categoryEditText.text.toString(),
                                        binding!!.statusEditText.text.toString(),
                                        binding!!.markEditText.text.toString(),
                                        binding!!.descriptionEditText.text.toString(),
                                        currentUser
                                    )
                                    Log.i(TAG, "$annonceToAdd")

                                    viewModel.apply {

                                        //to change
                                        addAnnonce(currentUser.userId, annonceToAdd).observe(viewLifecycleOwner){
                                            isTurning.observe(viewLifecycleOwner){ isVisible->
                                                progressBar.isVisible = isVisible
                                            }
                                            Log.i(TAG, "response succes from fragment $it")
                                            if(it) doOnSuccess()
                                            else doOnFail()
                                        }
                                    }
                                }

                                override fun onNegativeButtonClicked() {
//                                    _,_ -> null
                                }
                            },
                            title = getString(R.string.confirm_annonce_title),
                            message = getString(R.string.confirm_annonce_message)
                            ).show()

                    }

                    imageSelection.setOnClickListener {
                        setTheUploadImage()
                    }
                }
            }
        }
    }

    private fun setTheStatueEditTextView(){
        //default is new
        binding!!.statusTextField.editText?.setText(Status.NEW.status)

        //to change !!!!!!!!!!!!??
        //set the adapter
        val items = Status.values()
        val values = mutableListOf<String>()

        for (element in items){
            values.add(element.status)
        }
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, values)
        (binding!!.statusTextField.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setTheCategoriesEditText(){
        binding!!.categoryTextField.editText?.setText(CategoryEnum.GAMER.title)

        //to change !!!!!!!!!!!!
        //set the adapter
        val values = CategoryEnum.values().map {
            category -> category.title
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, values)
        (binding!!.categoryTextField.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setTheUploadImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        resultLauncher.launch(intent)
    }

    private fun updateImageText(quantity: Int?){
        if (quantity == 1) binding!!.imageNames.text = "1 Image Selectionnée "
        else binding!!.imageNames.text = "$quantity Images Selectionnées "
    }

    private fun uploadImages(uriList: List<Uri>): MutableList<String> {

        //to implement
        return mutableListOf()
    }

    private fun getImagesUris(clipData: ClipData): List<Uri>{
        val imagesList = mutableListOf<Uri>()
        for (i in 0 until clipData.itemCount){
            imagesList.add(clipData.getItemAt(i).uri)
        }
        return imagesList
    }

    private fun goToHomeFragment(){
        val action = CreateAnnonceFragmentDirections.actionCreateAnnonceFragmentToHomeFragment()
        findNavController().navigate(action)
    }

    private fun validateTheData(){

        //by default the button is disabled
        binding!!.apply {

            addButton.isEnabled = false

            titleEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.titleLiveData.value = text.toString()
            }

            priceEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.priceLiveData.value = text.toString()
            }

            imageNames.doOnTextChanged { text, _, _, _ ->
                viewModel.imagesLiveData.value = text.toString()
            }

            viewModel.isValidInput.observe(viewLifecycleOwner){ isActive ->
                binding!!.addButton.isEnabled = isActive
            }
        }
    }

    private fun doOnSuccess(){
        requireContext().toast(SUCCESS_MSG, Toast.LENGTH_SHORT)
        //go to home fragment after the toast disappears
        goToHomeFragment()
    }
    private fun doOnFail(){
        requireContext().toast(ERROR_MSG, Toast.LENGTH_SHORT)
        //go to home fragment after the toast disappears
        goToHomeFragment()
    }

    private fun goToLoginActivity() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

    private fun reloadActivity() {
        val i = Intent(requireActivity(), MainActivity::class.java)
        requireActivity().finish()
        requireActivity().overridePendingTransition(0, 0)
        startActivity(i)
        requireActivity().overridePendingTransition(0, 0)
    }
}