package com.example.pc.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
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
import com.example.pc.utils.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

private const val TAG = "CreateAnnonceFragment"
private const val ERROR_MSG = "Erreur l'annonce n'est pas ajoutée"
private const val SUCCESS_MSG = "Annonce ajoutée avec succes"

class CreateAnnonceFragment : Fragment() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var binding: FragmentCreateAnnonceBinding? = null
    private val retrofitService = RetrofitService.getInstance()
    private lateinit var viewModel: CreateAnnonceModel
    private lateinit var authModel: AuthModel
    private lateinit var currentUser: LoggedInUser
    private var imagesUris = listOf<Uri>()

    //add the livedata validation

    override fun onCreate(savedInstanceState: Bundle?) {

        viewModel = CreateAnnonceModel(CreateAnnonceRepository(retrofitService))
        authModel = AuthModel(retrofitService, null)

        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->

                Log.i(TAG, "isGranted: $isGranted")

                if (isGranted) {
                    setTheUploadImage()
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.

                    val snackBar = makeSnackBar(
                        requireView(),
                        getString(R.string.permission),
                        Snackbar.LENGTH_INDEFINITE
                    )
                    snackBar.setAction(R.string.ok) {
                        snackBar.dismiss()
                    }.show()

                }
            }

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

            isTurning.observe(viewLifecycleOwner) {
                binding!!.progressBar.isVisible = it
            }

            auth(requireContext())
            auth.observe(viewLifecycleOwner) {

                if (isAuth()) {

                    Log.i(TAG, "isAuth: $it")
                    val payload = getPayload()!!
                    currentUser = LoggedInUser(payload.id, payload.name)
                    Log.i(TAG, "user : $currentUser")

                    showForm()
                    setTheStatueEditTextView()
                    setTheCategoriesEditText()
                    validateTheData()

                    binding!!.apply {

                        addButton.setOnClickListener {

                            makeDialog(
                                requireContext(),
                                object : OnDialogClicked {
                                    override fun onPositiveButtonClicked() {

                                        val imagesPart = getImagesRequestBody()

                                        val builder = MultipartBody.Builder()
                                            .setType(MultipartBody.FORM)
                                            .addFormDataPart(
                                                "title",
                                                viewModel.titleLiveData.value!!
                                            )
                                            .addFormDataPart(
                                                "price",
                                                binding!!.priceEditText.text.toString()
                                            )
                                            .addFormDataPart(
                                                "category",
                                                binding!!.categoryEditText.text.toString()
                                            )
                                            .addFormDataPart(
                                                "status",
                                                binding!!.statusEditText.text.toString()
                                            )
                                            .addFormDataPart(
                                                "mark",
                                                binding!!.markEditText.text.toString()
                                            )
                                            .addFormDataPart(
                                                "description",
                                                binding!!.descriptionEditText.text.toString()
                                            )
                                            .addFormDataPart("seller[id]", currentUser.userId)
                                            .addFormDataPart("seller[name]", currentUser.userName)

                                        var i = 0
                                        for (body in imagesPart) {
                                            val imageName = body.key
                                            builder.addFormDataPart(
                                                "pictures",
                                                imageName,
                                                imagesPart[imageName]!!
                                            )
                                            i++
                                        }
                                        val annonceToAdd = builder.build()


                                        viewModel.apply {
                                            //to change
                                            addAnnonce(
                                                currentUser.userId,
                                                annonceToAdd,
                                            ).observe(viewLifecycleOwner) { requestSuccess ->
                                                isTurning.observe(viewLifecycleOwner) { isVisible ->
                                                    progressBar.isVisible = isVisible
                                                }
                                                Log.i(
                                                    TAG,
                                                    "response succes from fragment $requestSuccess"
                                                )
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
                    }

                } else {
                    Log.i(TAG, "user not connected auth body $it")
                    showNoUserConnected()
                }
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun showForm() {
        binding!!.apply {
            createAnnonceForm.apply {
                isActivated = true
                isVisible = true
            }

            addButton.apply {
                isVisible = true
                isActivated = true
            }
            imageSelection.setOnClickListener {
//                setTheUploadImage()

                when {
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        // You can use the API that requires the permission.
                        setTheUploadImage()
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                        // In an educational UI, explain to the user why your app requires this
                        // permission for a specific feature to behave as expected. In this UI,
                        // include a "cancel" or "no thanks" button that allows the user to
                        // continue using your app without granting the permission.
                        Log.i(TAG, "shouldShowRequestPermissionRationale: true")
                        showInContextUI(
                            object: OnDialogClicked {
                                override fun onPositiveButtonClicked() {
                                    requestPermissionLauncher.launch(
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    )
                                }

                                override fun onNegativeButtonClicked() {
                                    //cancel the dialog without doing nothing
                                }
                            }
                        )
                    }
                    else -> {
                        // You can directly ask for the permission.
                        // The registered ActivityResultCallback gets the result of this request.
                        Log.i(TAG, "shouldShowRequestPermissionRationale: false")
                        Log.i(TAG, "request Permission Launcher ")
                        requestPermissionLauncher.launch(
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    }
                }
            }
        }
    }

    private fun showInContextUI(onDialogClicked: OnDialogClicked) {
        makeDialog(
            requireContext(),
            onDialogClicked,
            getString(R.string.permission_required),
            getString(R.string.you_cant),
            negativeText = getString(R.string.no_thanks),
            positiveText = getString(R.string.authorise)

        ).show()
    }

    private fun showNoUserConnected() {
        binding!!.apply {
            noUserConnected.isVisible = true
            loginFromUserInfo.apply {
                isVisible = true
                setOnClickListener {
                    goToLoginActivity()
                }
            }
        }
    }

    private fun setTheStatueEditTextView() {
        //default is new
        binding!!.statusTextField.editText?.setText(Status.NEW.status)

        //set the adapter
        val values = Status.values().map { status ->
            status.status
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, values)
        (binding!!.statusTextField.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setTheCategoriesEditText() {
        binding!!.categoryTextField.editText?.setText(CategoryEnum.GAMER.title)

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

    private fun getImagesRequestBody(): HashMap<String, RequestBody> {

        val partsList = HashMap<String, RequestBody>()
        val uriPathHelper = URIPathHelper()

        for (uri in imagesUris) {
            val filePath = uriPathHelper.getPath(requireContext(), uri)
            val file = File(filePath)
            Log.i(TAG, "getImagesRequestBody: file $file")
            val requestFile: RequestBody =
                file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

            partsList[file.name] = requestFile
        }
        Log.i(TAG, "getRequestBody: $partsList")
        return partsList

    }

}