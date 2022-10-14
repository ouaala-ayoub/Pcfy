package com.example.pc

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.AnnonceModifyRepository
import com.example.pc.databinding.FragmentImageModifyBinding
import com.example.pc.ui.activities.AnnonceModifyActivity
import com.example.pc.ui.adapters.ImagesAdapter
import com.example.pc.ui.viewmodels.AnnonceModifyModel
import com.example.pc.utils.*
import com.squareup.picasso.Picasso
import okhttp3.MultipartBody

private const val TAG = "ImageModifyFragment"
private const val DELETED_IMAGE = "Image supprimée avec succes"

class ImageModifyFragment : Fragment() {

    private lateinit var binding: FragmentImageModifyBinding
    private lateinit var viewModel: AnnonceModifyModel
    private lateinit var annonceId: String
    private lateinit var imagesList: List<String>
    private lateinit var imagesAdapter: ImagesAdapter
    private var imageIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {

        val args: ImageModifyFragmentArgs by navArgs()

        //to modify
        annonceId = requireActivity().intent.getStringExtra("id")!!
        imageIndex = args.index
        imagesList = args.imagesArray.toList()
        viewModel = (requireActivity() as AnnonceModifyActivity).viewModel


        Log.i(TAG, "imageIndex: $imageIndex")
        Log.i(TAG, "imageName: $imagesList")

        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentImageModifyBinding.inflate(inflater, container, false)

        binding.apply {

            imagesPager.apply {
                imagesAdapter = ImagesAdapter(
                    imagesList,
                    object : ImagesAdapter.OnImageClicked {
                        override fun onLeftClicked() {
                            currentItem -= 1
                        }

                        override fun onRightClicked() {
                            currentItem += 1
                        }
                    })
                adapter = imagesAdapter
                currentItem = imageIndex
            }

            viewModel.apply {

                isTurning.observe(viewLifecycleOwner) {
                    imagesModifyProgresBar.isVisible = it
                }

                delete.setOnClickListener {
                    val currentItem = imagesPager.currentItem
                    Log.i(TAG, "going to delete item : $currentItem")
                    val builder = MultipartBody
                        .Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("index", currentItem.toString())
                        .build()

                    makeDialog(
                        requireContext(),
                        object: OnDialogClicked {
                            override fun onPositiveButtonClicked() {
                                deleteImage(annonceId, builder)
                                deletedImage.observe(viewLifecycleOwner) { deleted ->
                                    // go back to the previous screen if deleted successfully
                                    if(deleted){
                                        requireContext().toast(DELETED_IMAGE, Toast.LENGTH_SHORT)
                                        findNavController().popBackStack()
                                    }
                                    else {
                                        requireContext().toast(ERROR_MSG, Toast.LENGTH_SHORT)
                                        (requireActivity() as AnnonceModifyActivity).finish()
                                    }
                                }
                            }

                            override fun onNegativeButtonClicked() {
                                //do nothing
                            }

                        },
                        getString(R.string.image_instructions_title),
                        getString(R.string.image_instructions_message)
                    ).show()
                }

                modify.setOnClickListener { }
            }
        }

        return binding.root
    }

}