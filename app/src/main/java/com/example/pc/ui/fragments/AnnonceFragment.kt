package com.example.pc.ui.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.example.pc.R
import com.example.pc.data.remote.RetrofitService
import com.example.pc.data.repositories.AnnonceRepository
import com.example.pc.data.repositories.LoginRepository
import com.example.pc.databinding.FragmentAnnonceBinding
import com.example.pc.ui.activities.AnnonceActivity
import com.example.pc.ui.activities.LoginActivity
import com.example.pc.ui.viewmodels.AnnonceModel
import com.example.pc.utils.toast
import com.squareup.picasso.Picasso

private const val TAG = "AnnonceActivity"
private const val ERROR_TEXT = "test erreur"
private const val SUCCESS_TEXT = "annonce ajoutée au favories avec succes"

class AnnonceFragment : Fragment() {

    private var binding: FragmentAnnonceBinding? = null
    private val retrofitService = RetrofitService.getInstance()
    private val viewModel = AnnonceModel(
        AnnonceRepository(
            retrofitService
        )
    )
    private lateinit var annonceId: String
    private lateinit var userId: String
    private val picasso = Picasso.get()

    private lateinit var loginRepository: LoginRepository

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

                        //get the sellers name
                        getSellerById(annonce.seller!!.userId)

                        //images first
                        if (annonce.pictures.isNotEmpty()) {
                            picasso
                                .load(annonce.pictures[0])
                                .fit()
                                .into(productImages)
                        }

                        productTitle.text = annonce.title
                        productPrice.text = getString(R.string.price, annonce.price.toInt())
                        productStatus.text = getString(R.string.status, annonce.status)

                        //the seller name
                        seller.observe(viewLifecycleOwner){
                            sellerName.text = it.name
                        }
//                        set the seller image
//                        selleerImage

                        productDescription.text = annonce.description

                        Log.i(TAG, "annonce = ${annonceToShow.value}")
                    } else {
                        requireContext().toast(ERROR_TEXT, Toast.LENGTH_SHORT)
                        Log.e(TAG, "error: something went wrong")
                        goToMainActivity()
                    }

                    addToFav.setOnClickListener {
                        if (annonce != null){

                            loginRepository = LoginRepository(
                                retrofitService,
                                requireContext()
                            )

                            val isLoggedIn = loginRepository.isLoggedIn
                            isLoggedIn.observe(viewLifecycleOwner){ isLogged ->

                                Log.i(TAG, "isLogged: $isLogged")

                                if (isLogged) {
                                    userId = loginRepository.user!!.userId
                                }

                                else if (!isLogged){
                                    goToLoginActivity()
                                }
                            }


                            addToFavourites(userId, annonce)
                            addedFavouriteToUser.observe(viewLifecycleOwner){
                                if (it)
                                    doOnSuccess()
                                else
                                    doOnFail()
                            }
                        }
                        else {
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

    private fun goToMainActivity(){
        val activity = requireActivity() as AnnonceActivity
        activity.finish()
    }
}