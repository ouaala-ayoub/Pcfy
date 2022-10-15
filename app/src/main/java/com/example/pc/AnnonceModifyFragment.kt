package com.example.pc

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pc.data.models.local.Detail
import com.example.pc.data.models.network.Annonce
import com.example.pc.data.models.network.CategoryEnum
import com.example.pc.data.models.network.Status
import com.example.pc.databinding.AddDetailBinding
import com.example.pc.databinding.FragmentAnnonceModifyBinding
import com.example.pc.ui.activities.AnnonceModifyActivity
import com.example.pc.ui.activities.MainActivity
import com.example.pc.ui.adapters.AddDetailsAdapter
import com.example.pc.ui.adapters.ImagesModifyAdapter
import com.example.pc.ui.viewmodels.AnnonceModifyModel
import com.example.pc.utils.*
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

private const val TAG = "AnnonceModifyActivity"
private const val ADDED_IMAGES = "Images Ajoutée avec succes"
private const val ERROR_GET_ANNONCE = "Erreur innatendue"
private const val ERROR_SET_ANNONCE = "Erreur de La modification de l'annonce"
private const val SUCCESS_SET_ANNONCE = "Annonce modifié avec succes"

class AnnonceModifyFragment : Fragment() {

    private lateinit var binding: FragmentAnnonceModifyBinding
    private lateinit var viewModel: AnnonceModifyModel
    private lateinit var annonceToModifyId: String
    private lateinit var imageResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {

        val annonceActivity = (requireActivity() as AnnonceModifyActivity)
        viewModel = annonceActivity.viewModel
        annonceToModifyId = annonceActivity.intent.getStringExtra("id")!!

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAnnonceModifyBinding.inflate(inflater, container, false)

        imageResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    viewModel.apply {

                        if (data?.clipData != null) {
                            val requestBody =
                                getRequestBody(data.clipData!!)

                            viewModel.apply {
                                addPictures(annonceToModifyId, requestBody)
                                addedImages.observe(viewLifecycleOwner) { added ->
                                    Log.i(TAG, "addedImages: $added")
                                    if (added) {
                                        requireContext().toast(ADDED_IMAGES, Toast.LENGTH_SHORT)
                                        getAnnonce(annonceToModifyId)
                                    } else {
                                        requireContext().toast(ERROR_MSG, Toast.LENGTH_SHORT)
                                        requireActivity().finish()
                                    }
                                }
                            }
                        }
                    }
                }
            }

        binding.apply {
            viewModel.apply {
                getAnnonce(annonceToModifyId).observe(viewLifecycleOwner) { annonce ->

                    isTurning.observe(viewLifecycleOwner) {
                        annonceModifyProgressBar.isVisible = it
                    }

                    // on annonce retrieved fail
                    if (annonce == null) {
                        doOnFail(ERROR_GET_ANNONCE)
                    } else {

                        Log.i(TAG, "annonce retrieved : $annonce")
                        var details = annonce.details

                        // on annonce retrieved success
                        //to add image adding and deleting

                        addDetails.setOnClickListener {
                            // open the dialog
                            val detailsViewBinding = AddDetailBinding.inflate(layoutInflater)

                            Log.i(TAG, "details: $details")
                            val detailsAddAdapter = AddDetailsAdapter(
                                details as MutableList<Detail>
                            )

                            detailsViewBinding.apply {
                                detailsAddAdapter.apply {
                                    filterDetailsList()
                                    isEmpty.observe(viewLifecycleOwner) { isEmpty ->
                                        noDetails.isVisible = isEmpty
                                    }
                                    addDetail.setOnClickListener {
                                        addEmptyField()
                                        noDetails.isVisible = false
                                    }
                                }

                                addDetailsRv.adapter = detailsAddAdapter
                                addDetailsRv.layoutManager =
                                    LinearLayoutManager(requireContext())
                            }

                            val dialog = makeDialog(
                                requireContext(),
                                object : OnDialogClicked {
                                    override fun onPositiveButtonClicked() {
                                        detailsAddAdapter.apply {
                                            filterDetailsList()
                                            details = detailsList
                                        }

                                        Log.i(TAG, "details : $details")
                                    }

                                    override fun onNegativeButtonClicked() {
                                        Log.i(TAG, "onNegativeButtonClicked: clicked")
                                    }
                                },
                                getString(R.string.detail_title),
                                null,
                                detailsViewBinding.root
                            )
                            dialog.show()
                            dialog.window!!.clearFlags(
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                        or
                                        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                            )
                        }


                        imagesRv.apply {

                            adapter = ImagesModifyAdapter(
                                annonce.pictures,
                                object : ImagesModifyAdapter.OnImageModifyClicked {
                                    override fun onImageClicked(
                                        imageIndex: Int,
                                        imagesList: List<String>
                                    ) {
                                        goToImageModifyFragment(
                                            imageIndex,
                                            imagesList.toTypedArray()
                                        )
                                    }

                                    override fun onAddClicked() {
                                        openGallery()
                                    }
                                }
                            )

                            layoutManager = LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        }
                        titleEditText.setText(annonce.title)
                        priceEditText.setText(annonce.price.toString())
                        setTheCategoriesEditText(annonce.category)
                        setTheStatueEditTextView(annonce.status)
                        markEditText.setText(annonce.mark)
                        descriptionEditText.setText(annonce.description)

                        validateTheData()

                        submitChanges.setOnClickListener {
                            val newAnnonce = Annonce(
                                title = titleEditText.text.toString(),
                                price = priceEditText.text.toString().toInt(),
                                category = categoryEditText.text.toString(),
                                status = statusEditText.text.toString(),
                                mark = markEditText.text.toString(),
                                description = descriptionEditText.text.toString(),
                                details = details,
                            )
                            updateAnnonceInfo(annonceToModifyId, newAnnonce)
                                .observe(viewLifecycleOwner) { annonceModified ->

                                    //on annonce modification fail
                                    if (!annonceModified) {
                                        doOnFail(ERROR_SET_ANNONCE)
                                    } else {
                                        doOnSuccess(SUCCESS_SET_ANNONCE)
                                    }
                                }
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private fun openGallery(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        imageResultLauncher.launch(intent)
    }

    private fun getRequestBody(clipData: ClipData): MultipartBody {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        val pathHelper = URIPathHelper()

        for (i in 0 until clipData.itemCount){
            val currentItemUri = clipData.getItemAt(i).uri
            val filePath = pathHelper.getPath(requireContext(), currentItemUri)

            val file = File(filePath!!)
            Log.i(TAG, "file $i: $file")
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())

            builder.apply {
                addFormDataPart(
                    "pictures",
                    file.name,
                    requestFile
                )
            }
        }

        return builder.build()
    }

    private fun goToImageModifyFragment(imageIndex: Int, imagesList: Array<String>) {
        val action = AnnonceModifyFragmentDirections
            .actionAnnonceModifyFragmentToImageModifyFragment(imageIndex, imagesList)
        findNavController().navigate(action)
    }

    private fun setTheStatueEditTextView(default: String) {
        //default is the annonce attribute value
        binding.statusTextField.editText?.setText(default)

        //set the adapter
        val values = Status.values().map {
            it.status
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, values)
        (binding.statusTextField.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setTheCategoriesEditText(default: String) {

        //default is the annonce attribute value
        binding.categoryTextField.editText?.setText(default)

        //set the adapter
        val values = CategoryEnum.values().map { category ->
            category.title
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, values)
        (binding.categoryTextField.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun validateTheData() {

        //by default the button is disabled
        binding.apply {

            //initialise the value of the live data to check
            submitChanges.isEnabled = true

            viewModel.apply {
                titleLiveData.value = titleEditText.text.toString()
                priceLiveData.value = priceEditText.text.toString()
            }

            titleEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.titleLiveData.value = text.toString()
            }

            priceEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.priceLiveData.value = text.toString()
            }

            viewModel.isValidInput.observe(viewLifecycleOwner) { isActive ->
                binding.submitChanges.isEnabled = isActive
            }
        }
    }

    private fun geToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
    }

    private fun doOnFail(message: String) {
        requireContext().toast(message, Toast.LENGTH_SHORT)
        geToMainActivity()
    }

    private fun doOnSuccess(message: String) {
        requireContext().toast(message, Toast.LENGTH_SHORT)
        requireActivity().finish()
    }

}