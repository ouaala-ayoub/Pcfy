package com.example.pc.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.example.pc.R
import com.example.pc.databinding.FragmentOrderBinding
import com.example.pc.ui.activities.AnnonceActivity
import com.example.pc.ui.viewmodels.AnnonceModel
import com.example.pc.ui.viewmodels.AuthModel
import com.example.pc.ui.viewmodels.OrderModel
import com.example.pc.utils.BASE_AWS_S3_LINK
import com.example.pc.utils.ERROR_MSG
import com.example.pc.utils.USERS_AWS_S3_LINK
import com.example.pc.utils.toast
import com.squareup.picasso.Picasso

private const val ERROR_AUTH = "Erreur d'authentification"
private const val ORDER_SUCCESS = "Commande passée avec succes"

class OrderFragment : Fragment() {

    private lateinit var viewModel: AnnonceModel
    private lateinit var authModel: AuthModel
    private lateinit var orderModel: OrderModel
    private lateinit var annonceId: String
    private lateinit var binding: FragmentOrderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = requireActivity() as AnnonceActivity
        annonceId = activity.intent.getStringExtra("id") as String
        authModel = activity.authModel
        viewModel = activity.viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOrderBinding.inflate(inflater, container, false)

        authModel.apply {
            auth.observe(viewLifecycleOwner) {

                if (isAuth()) {
                    val userId = getPayload()!!.id

                    binding.apply {

                        viewModel.apply {

                            isTurning.observe(viewLifecycleOwner) { loading ->
                                progressBar3.isVisible = loading
                                disableUi()
                            }

                            getAnnonceById(annonceId)
                            annonceToShow.observe(viewLifecycleOwner) { annonce ->
                                if (annonce != null) {

                                    Picasso.get()
                                        .load("${BASE_AWS_S3_LINK}${annonce.pictures[0]}")
                                        .fit()
                                        .into(annonceImage)

                                    annonceTitle.text = annonce.title
                                    annonceSeller.text = annonce.seller?.userName ?: "Non Available"
                                    announcePrice.text = getString(R.string.price, annonce.price)

                                    //frais de livraison !!!!

                                    orderModel = OrderModel(annonce.price.toFloat())

                                    val orderModel = OrderModel(annonce.price.toFloat())
                                    orderModel.apply {

                                        minus.setOnClickListener { quantitySub() }
                                        plus.setOnClickListener { quantityAdd() }

                                        price.observe(viewLifecycleOwner) { price ->
                                            //hardcoded the shipping price
                                            // to change
                                            val priceWithShipping = price + 50
                                            orderPrice.text = priceWithShipping.toString()
                                        }
                                        quantity.observe(viewLifecycleOwner) { quantity ->
                                            quantityTv.text = quantity.toString()
                                        }
                                    }

                                    order.setOnClickListener {
                                        addOrder(
                                            userId,
                                            annonceId,
                                            quantityTv.text.toString().toInt()
                                        )
                                        orderAdded.observe(viewLifecycleOwner) { added ->
                                            if (added) {
                                                doOnSuccess(ORDER_SUCCESS)
                                            } else {
                                                doOnFail(ERROR_MSG)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    doOnFail(ERROR_AUTH)
                }
            }
        }

        return binding.root
    }

    private fun disableUi() {
        binding.apply {
            order.isEnabled = false
            plus.isEnabled = false
            minus.isEnabled = false
        }
    }

    private fun doOnFail(message: String) {
        requireContext().toast(message, Toast.LENGTH_SHORT)
        findNavController().popBackStack()
    }

    private fun doOnSuccess(message: String) {
        requireContext().toast(message, Toast.LENGTH_SHORT)
        goToOrderSuccessFragment()
    }

    private fun goToOrderSuccessFragment() {
        val action = OrderFragmentDirections.actionOrderFragmentToOrderSuccessFragment()
        findNavController().navigate(action)
    }
}