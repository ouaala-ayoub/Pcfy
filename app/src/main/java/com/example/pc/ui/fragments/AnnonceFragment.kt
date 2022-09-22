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
import com.example.pc.R
import com.example.pc.data.models.local.Detail
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.AnnonceRepository
import com.example.pc.databinding.FragmentAnnonceBinding
import com.example.pc.ui.activities.AnnonceActivity
import com.example.pc.ui.activities.LoginActivity
import com.example.pc.ui.adapters.DetailsAdapter
import com.example.pc.ui.viewmodels.AnnonceModel
import com.example.pc.ui.viewmodels.AuthModel
import com.example.pc.utils.toast
import com.squareup.picasso.Picasso

private const val TAG = "AnnonceActivity"
private const val ERROR_TEXT = "test erreur"
private const val SUCCESS_TEXT = "annonce ajoutée au favories avec succes"

class AnnonceFragment : Fragment() {

    private var binding: FragmentAnnonceBinding? = null
    private lateinit var annonceId: String
    private lateinit var userId: String
    private lateinit var detailsAdapter: DetailsAdapter
    private val retrofitService = RetrofitService.getInstance()
    private val picasso = Picasso.get()
    private val viewModel = AnnonceModel(
        AnnonceRepository(
            retrofitService
        )
    )

    private lateinit var authModel: AuthModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = requireActivity() as AnnonceActivity
        annonceId = activity.intent.getStringExtra("id") as String

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

                            //images first
                            if (annonce.pictures.isNotEmpty()) {
                                picasso
                                    .load(annonce.pictures[0])
                                    .fit()
                                    .into(productImages)
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
                            }

                            //the seller field
                            seller.observe(viewLifecycleOwner) { seller ->
                                sellerName.text = seller.name
//                                picasso
//                                    .load(seller.imageUrl)
//                                    .fit()
//                                    .centerCrop()
//                                    .into(selleerImage)
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

                    addToFav.setOnClickListener {
                        if (annonce != null) {

                            authModel = AuthModel(
                                retrofitService,
                                null
                            )

                            authModel.auth(requireContext())

                            authModel.apply {
                                auth.observe(viewLifecycleOwner) {

                                    if (isAuth()) {
                                        userId = getPayload()!!.id
                                        addToFavourites(userId, annonce)
                                        addedFavouriteToUser.observe(viewLifecycleOwner) {
                                            if (it)
                                                doOnSuccess()
                                            else
                                                doOnFail()
                                        }
                                    } else {
                                        goToLoginActivity()
                                    }
                                }
                            }
                        } else {
                            //consider Fail
                            doOnFail()
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

    private fun doOnFail() {
        requireContext().toast(ERROR_TEXT, Toast.LENGTH_SHORT)
        goToMainActivity()
    }

    private fun doOnSuccess() {
        requireContext().toast(SUCCESS_TEXT, Toast.LENGTH_SHORT)
    }

    private fun goToMainActivity() {
        val activity = requireActivity() as AnnonceActivity
        activity.finish()
    }
}