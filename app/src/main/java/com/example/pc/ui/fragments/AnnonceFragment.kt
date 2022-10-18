package com.example.pc.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.pc.R
import com.example.pc.data.models.local.ImageLoader
import com.example.pc.data.models.local.LoadPolicy
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.AnnonceRepository
import com.example.pc.databinding.FragmentAnnonceBinding
import com.example.pc.databinding.QuantityLayoutBinding
import com.example.pc.ui.activities.AnnonceActivity
import com.example.pc.ui.activities.LoginActivity
import com.example.pc.ui.adapters.DetailsAdapter
import com.example.pc.ui.adapters.ImagesAdapter
import com.example.pc.ui.viewmodels.AnnonceModel
import com.example.pc.ui.viewmodels.AuthModel
import com.example.pc.utils.*
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso

private const val TAG = "AnnonceActivity"
private const val ERROR_TEXT = "Erreur inattendue"
private const val NO_USER = "Vous n'êtes pas connecté"
private const val SUCCESS_TEXT = "annonce ajoutée au favories avec succes"
private const val ORDER_SUCCESS = "Commande passée avec succes"
private const val SUCCESS_DEL_TEXT = "annonce supprimé des favories avec succes"


class AnnonceFragment : Fragment() {

    private var binding: FragmentAnnonceBinding? = null
    private lateinit var annonceId: String
    private lateinit var userId: String
    private lateinit var detailsAdapter: DetailsAdapter
    private val retrofitService = RetrofitService.getInstance()
    private lateinit var picasso: Picasso
    private val viewModel = AnnonceModel(
        AnnonceRepository(
            retrofitService
        )
    )
    private lateinit var authModel: AuthModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authModel = AuthModel(
            retrofitService,
            null
        )

        authModel.apply {
            auth(requireContext())
        }


        val activity = requireActivity() as AnnonceActivity
        annonceId = activity.intent.getStringExtra("id") as String
        picasso = activity.picasso

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAnnonceBinding.inflate(
            inflater,
            container,
            false
        )

        binding!!.apply {
            viewModel.apply {

                getAnnonceById(annonceId)

                annonceToShow.observe(viewLifecycleOwner) { annonce ->

                    if (annonceToShow.value != null) {
                        //bind the data to the views

                        try {
                            getSellerById(annonce.seller!!.userId)

                            imagesVp.apply {

                                val pictures = annonce.pictures
                                val imageLoader =
                                    pictures.map { url -> ImageLoader(url, LoadPolicy.Cache) }

                                offscreenPageLimit = pictures.size
                                adapter = ImagesAdapter(
                                    imageLoader,
                                    object : ImagesAdapter.OnImageClicked {
                                        override fun onLeftClicked() {
                                            currentItem -= 1
                                        }

                                        override fun onRightClicked() {
                                            currentItem += 1
                                        }
                                    },
                                    picasso
                                )
                                TabLayoutMediator(trackingTab, this, true) { _, _ -> }.attach()
                            }

                            productTitle.text = annonce.title
                            productPrice.text = getString(R.string.price, annonce.price.toString())
                            productStatus.text = getString(R.string.status, annonce.status)

                            val details = annonce.details
                            if (!details.isNullOrEmpty()) {
                                //setting the details recycler view
                                detailsAdapter = DetailsAdapter(
                                    annonce.details
                                )
                                detailsRv.apply {
                                    adapter = detailsAdapter
                                    isNestedScrollingEnabled = false
                                    setHasFixedSize(true)
                                    layoutManager = LinearLayoutManager(requireContext())
                                }
                            } else {
                                detailTv.isVisible = false
                            }

                            //the seller field
                            seller.observe(viewLifecycleOwner) { seller ->
                                sellerName.text = seller.name
                                if (seller.imageUrl.isNullOrBlank()) {
                                    selleerImage.setImageResource(R.drawable.ic_baseline_no_photography_24)
                                }
                                picasso
                                    .load("${USERS_AWS_S3_LINK}${seller.imageUrl}")
                                    .fit()
                                    .into(selleerImage)
                            }

                            productDescription.text = annonce.description

                        } catch (e: Throwable) {
                            Log.e(TAG, "binding error : ${e.message}")
                        }
                        //get the sellers name
                    } else {
                        requireContext().toast(ERROR_TEXT, Toast.LENGTH_SHORT)
                        Log.e(TAG, "error: something went wrong")
                        goToMainActivity()
                    }



                    authModel.apply {
                        auth.observe(viewLifecycleOwner) {

                            if (isAuth()) {
                                userId = getPayload()!!.id
                                updateIsAddedToFav(userId, annonceId)

                                isAddedToFav.observe(viewLifecycleOwner) { isFavChecked ->
                                    addToFav.apply {
                                        isChecked = isFavChecked
                                        if (!isFavChecked) {
                                            setOnClickListener {
                                                addToFavourites(userId, annonceId)
                                                addedFavouriteToUser.observe(viewLifecycleOwner) {
                                                    if (!it) {
                                                        doOnFail(ERROR_TEXT)
                                                    }
                                                }
                                            }
                                        } else if (isFavChecked) {
                                            setOnClickListener {
                                                //add logic to delete favourite
                                                deleteFavourite(userId, annonceId)
                                                deletedWithSuccess.observe(viewLifecycleOwner) { deleted ->
                                                    if (!deleted) {
                                                        requireContext().toast(
                                                            ERROR_TEXT,
                                                            Toast.LENGTH_SHORT
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                orderNow.setOnClickListener {

                                    val quantityView =
                                        QuantityLayoutBinding.inflate(layoutInflater)
                                    quantityView.apply {
                                        minus.setOnClickListener {
                                            quantity.text = (quantity.text.toString()
                                                .toInt() - 1).toString()
                                        }
                                        plus.setOnClickListener {
                                            quantity.text = (quantity.text.toString()
                                                .toInt() + 1).toString()
                                        }
                                    }

                                    makeDialog(
                                        requireContext(),
                                        object : OnDialogClicked {
                                            override fun onPositiveButtonClicked() {
                                                Log.i(
                                                    TAG, "onPositiveButtonClicked quantity is: ${
                                                        quantityView.quantity.text.toString()
                                                            .toInt()
                                                    }"
                                                )
                                                addOrder(
                                                    userId,
                                                    annonceId,
                                                    quantityView.quantity.text.toString()
                                                        .toInt()
                                                )
                                                orderAdded.observe(viewLifecycleOwner) { added ->
                                                    if (added) {
                                                        doOnSuccess(ORDER_SUCCESS)
                                                    } else {
                                                        doOnFail(ERROR_TEXT)
                                                    }
                                                }
                                            }

                                            override fun onNegativeButtonClicked() {
                                                //return to the annonce screen
                                            }
                                        },
                                        getString(R.string.order_title),
                                        getString(R.string.order_message),
                                        quantityView.root,
                                        getString(R.string.order_negative),
                                        getString(R.string.order_positive)
                                    ).show()
                                }

                            } else {
                                addToFav.apply {
                                    isChecked = false
                                    isEnabled = false
                                    setOnClickListener {
                                        requireContext().toast(NO_USER, Toast.LENGTH_SHORT)
//                                        doOnFail(NO_USER)
                                    }
                                }
                                orderNow.setOnClickListener {
                                    requireContext().toast(
                                        NO_USER,
                                        Toast.LENGTH_SHORT
                                    )
                                }
                            }
                        }
                    }

                    productSeller.setOnClickListener {
                        goToSellerPage(annonce.seller!!.userId)
                    }
                    sellerInfo.setOnClickListener {
                        goToSellerPage(annonce.seller!!.userId)
                    }

                }

                isProgressBarTurning.observe(viewLifecycleOwner) {
                    annonceProgressBar.isVisible = it
                }

            }
        }

        return binding?.root
    }

    private fun goToSellerPage(sellerId: String) {
        val action = AnnonceFragmentDirections.actionAnnonceFragmentToSellerInfoFragment(sellerId)
        findNavController().navigate(action)
    }

    private fun goToLoginActivity() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

    private fun doOnFail(message: String) {
        requireContext().toast(message, Toast.LENGTH_SHORT)
        goToMainActivity()
    }

    private fun doOnSuccess(message: String) {
        requireContext().toast(message, Toast.LENGTH_SHORT)
    }

    private fun goToMainActivity() {
        val activity = requireActivity() as AnnonceActivity
        activity.finish()
    }
}