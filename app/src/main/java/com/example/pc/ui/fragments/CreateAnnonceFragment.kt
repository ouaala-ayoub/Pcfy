package com.example.pc.ui.fragments

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pc.R
import com.example.pc.data.models.local.LoggedInUser
import com.example.pc.data.models.network.CategoryEnum
import com.example.pc.data.models.network.Status
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.CreateAnnonceRepository
import com.example.pc.databinding.FragmentCreateAnnonceBinding
import com.example.pc.ui.activities.LoginActivity
import com.example.pc.ui.activities.MainActivity
import com.example.pc.ui.viewmodels.AuthModel
import com.example.pc.ui.viewmodels.CreateAnnonceModel
import com.example.pc.utils.OnDialogClicked
import com.example.pc.utils.makeDialog
import com.example.pc.utils.toast
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.*


private const val TAG = "CreateAnnonceFragment"
private const val ERROR_MSG = "Erreur l'annonce n'est pas ajoutée"
private const val SUCCESS_MSG = "Annonce ajoutée avec succes"

class CreateAnnonceFragment : Fragment() {

    private var binding: FragmentCreateAnnonceBinding? = null
    private lateinit var viewModel: CreateAnnonceModel
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var authModel: AuthModel
    private lateinit var currentUser: LoggedInUser
    private val retrofitService = RetrofitService.getInstance()
    private var imagesUris = listOf<Uri>()

    //add the livedata validation

    override fun onCreate(savedInstanceState: Bundle?) {


        viewModel = CreateAnnonceModel(CreateAnnonceRepository(retrofitService))
        authModel = AuthModel(retrofitService, null)

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val data: Intent? = result.data
                    Log.i(TAG, "image retrieved")
                    Log.i(TAG, "data is ${data?.data}: ")
                    updateImageText(data?.clipData?.itemCount)

                    if (data?.clipData != null) {
                        imagesUris = getImagesUris(data.clipData!!)
                        Log.i(TAG, "imagesUris: $imagesUris")

                    }
                }
            }

        binding = FragmentCreateAnnonceBinding.inflate(
            inflater,
            container,
            false
        )
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        authModel.apply {

            auth(requireContext())
            auth.observe(viewLifecycleOwner) {

                if (isAuth()) {

                    Log.i(TAG, "isAuth: $it")
                    val payload = getPayload()!!
                    currentUser = LoggedInUser(payload.id, payload.name)
                    Log.i(TAG, "user id: $currentUser")


                    setTheStatueEditTextView()
                    setTheCategoriesEditText()
                    validateTheData()

                    binding!!.apply {

                        addButton.setOnClickListener {

                            makeDialog(
                                requireContext(),
                                object : OnDialogClicked {
                                    override fun onPositiveButtonClicked() {
                                        var imagesList = mutableListOf<String>()

                                        if (imagesUris.isNotEmpty()) {
                                            imagesList = uploadImages(imagesUris)
                                        }

                                        val filePath = imagesUris[0].path
                                        val file = filePath?.let { it1 -> File(it1) }
                                        Log.i(TAG, "file Path : $file")

                                        val reqBody =
                                            filePath!!.toRequestBody("image/*".toMediaTypeOrNull())
                                        Log.i(TAG, "reqBody: $reqBody")

                                        val annonceToAdd = HashMap<String, String>()

                                        annonceToAdd["title"] =
                                            viewModel.titleLiveData.value!!
                                        annonceToAdd["price"] =
                                            binding!!.priceEditText.text.toString()
                                        annonceToAdd["category"] =
                                            binding!!.categoryEditText.text.toString()
                                        annonceToAdd["status"] =
                                            binding!!.statusEditText.text.toString()
                                        annonceToAdd["mark"] =
                                            binding!!.markEditText.text.toString()
                                        annonceToAdd["description"] =
                                            binding!!.descriptionEditText.text.toString()
                                        annonceToAdd["seller[id]"] =
                                            currentUser.userId
                                        annonceToAdd["seller[name]"] =
                                            currentUser.userName


                                        Log.i(TAG, "$annonceToAdd")

                                        viewModel.apply {
                                            //to change
                                            addAnnonce(
                                                currentUser.userId,
                                                annonceToAdd,
//                                                reqBody
                                            ).observe(viewLifecycleOwner) { requestSuccess ->
                                                isTurning.observe(viewLifecycleOwner) { isVisible ->
                                                    progressBar.isVisible = isVisible
                                                }
                                                Log.i(TAG, "response succes from fragment $it")
                                                if (requestSuccess) doOnSuccess()
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

                } else {
                    Log.i(TAG, "user not connected auth body $it")

                    binding!!.apply {
                        createAnnonceForm.apply {
                            isActivated = false
                            isVisible = false
                        }

                        addButton.apply {
                            isVisible = false
                            isActivated = false
                        }

                        noUserConnected.isVisible = true
                        loginFromUserInfo.apply {
                            isVisible = true
                            setOnClickListener {
                                goToLoginActivity()
                            }
                        }
                    }
                }

            }

        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setTheStatueEditTextView() {
        //default is new
        binding!!.statusTextField.editText?.setText(Status.NEW.status)

        //to change !!!!!!!!!!!!??
        //set the adapter
        val items = Status.values()
        val values = mutableListOf<String>()

        for (element in items) {
            values.add(element.status)
        }
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, values)
        (binding!!.statusTextField.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setTheCategoriesEditText() {
        binding!!.categoryTextField.editText?.setText(CategoryEnum.GAMER.title)

        //to change !!!!!!!!!!!!
        //set the adapter
        val values = CategoryEnum.values().map { category ->
            category.title
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, values)
        (binding!!.categoryTextField.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setTheUploadImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        resultLauncher.launch(intent)
    }

    private fun updateImageText(quantity: Int?) {
        if (quantity == 1) binding!!.imageNames.text = "1 Image Selectionnée "
        else binding!!.imageNames.text = "$quantity Images Selectionnées "
    }

    private fun uploadImages(uriList: List<Uri>): MutableList<String> {

        //to implement
        return mutableListOf()
    }

    private fun getImagesUris(clipData: ClipData): List<Uri> {
        val imagesList = mutableListOf<Uri>()
        for (i in 0 until clipData.itemCount) {
            imagesList.add(clipData.getItemAt(i).uri)
        }
        return imagesList
    }

    private fun goToHomeFragment() {
        val action = CreateAnnonceFragmentDirections.actionCreateAnnonceFragmentToHomeFragment()
        findNavController().navigate(action)
    }

    private fun validateTheData() {

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

            viewModel.isValidInput.observe(viewLifecycleOwner) { isActive ->
                binding!!.addButton.isEnabled = isActive
            }
        }
    }

    private fun doOnSuccess() {
        requireContext().toast(SUCCESS_MSG, Toast.LENGTH_SHORT)
        //go to home fragment after the toast disappears
        goToHomeFragment()
    }

    private fun doOnFail() {
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