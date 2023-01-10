package com.example.pc.ui.fragments

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.example.pc.R
import com.example.pc.data.models.network.*
import com.example.pc.databinding.FragmentOrderBinding
import com.example.pc.databinding.OrderDialogViewBinding
import com.example.pc.ui.activities.AnnonceActivity
import com.example.pc.ui.viewmodels.AnnonceModel
import com.example.pc.ui.viewmodels.AuthModel
import com.example.pc.ui.viewmodels.OrderModel
import com.example.pc.utils.*
import com.squareup.picasso.Picasso

private const val ERROR_AUTH = "Erreur d'authentification"
private const val ORDER_SUCCESS = "Commande passée avec succes"
private const val TAG = "OrderFragment"

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
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOrderBinding.inflate(inflater, container, false)

        authModel.apply {
            auth.observe(viewLifecycleOwner) {

                if (isAuth()) {
                    val userId = getUserId()!!

                    binding.apply {

                        viewModel.apply {


                            isTurning.observe(viewLifecycleOwner) { loading ->
                                isProgressBarTurning.observe(viewLifecycleOwner) { annonceLoading ->
                                    val isLoading = loading || annonceLoading
                                    progressBar3.isVisible = isLoading
                                    disableUi(isLoading)
                                }
                            }

                            getAnnonceById(annonceId)
                            annonceToShow.observe(viewLifecycleOwner) { annonce ->
                                if (annonce != null) {
                                    orderModel = OrderModel(annonce.price.toFloat())
                                    val binding = OrderDialogViewBinding.inflate(
                                        layoutInflater
                                    )
                                    val dialog = makeDialog(
                                        requireContext(),
                                        object : OnDialogClicked {
                                            override fun onPositiveButtonClicked() {
                                                orderModel.apply {
                                                    val orderToAdd = Order(
                                                        seller = IdResponse(
                                                            annonce.seller.id
                                                        ),
                                                        quantity = quantity.value!!,
                                                        customer = Customer(
                                                            userId,
                                                            name.value.toString(),
                                                            address.value.toString(),
                                                            phoneNumber.value.toString()
                                                        ),
                                                        annonce = Product(
                                                            annonceId,
                                                            annonce.title,
                                                            annonce.pictures[0],
                                                            annonce.price
                                                        )
                                                    )
                                                    Log.i(
                                                        TAG,
                                                        "onPositiveButtonClicked: order To Add $orderToAdd"
                                                    )
                                                    addOrder(
                                                        orderToAdd
                                                    )

                                                    orderAdded.observe(
                                                        viewLifecycleOwner
                                                    ) { addedId ->
                                                        Log.i(
                                                            TAG,
                                                            "onPositiveButtonClicked: order added = $addedId"
                                                        )

                                                        if (addedId != null) {
//
                                                            getSellerById(
                                                                annonce.seller.id,
                                                                "seller"
                                                            )
                                                            seller.observe(viewLifecycleOwner) {
                                                                if (it != null) {
                                                                    val sellerToken = it.token
                                                                    Log.i(
                                                                        TAG,
                                                                        "sellerToken: $sellerToken"
                                                                    )
                                                                    Log.d(
                                                                        TAG,
                                                                        "seller : ${it.userId}"
                                                                    )
                                                                    if (sellerToken != null) {
                                                                        val fireBaseKey =
                                                                            getFirebaseKey()

                                                                        Log.d(
                                                                            TAG,
                                                                            "fireBaseKey: $fireBaseKey"
                                                                        )
                                                                        val message = Message(
                                                                            Data(
                                                                                annonce.title,
                                                                                it.userId!!,
                                                                                addedId
                                                                            ),
                                                                            it.token
                                                                        )
                                                                        notifySeller(
                                                                            message,
                                                                            fireBaseKey
                                                                        )
                                                                        doOnSuccess(
                                                                            ORDER_SUCCESS
                                                                        )
                                                                    } else {
                                                                        doOnSuccess(
                                                                            ORDER_SUCCESS
                                                                        )
                                                                    }

                                                                } else {
                                                                    doOnSuccess(
                                                                        ORDER_SUCCESS
                                                                    )
                                                                }
                                                            }

                                                        } else {
                                                            doOnFail(ERROR_MSG)
                                                        }
                                                    }
                                                }
                                            }

                                            override fun onNegativeButtonClicked() {

                                            }

                                        },
                                        "Informations de livraison",
                                        "Confirmations des informations :",
                                        view = binding.root
                                    )

                                    Picasso.get()
                                        .load("${BASE_AWS_S3_LINK}${annonce.pictures[0]}")
                                        .fit()
                                        .into(annonceImage)

                                    annonceTitle.text = annonce.title
//                                    annonceSeller.text = annonce.seller?.userName ?: "Non Available"
                                    announcePrice.text = getString(R.string.price, annonce.price)

                                    //frais de livraison !!!!


                                    orderModel.apply {

                                        minus.setOnClickListener { quantitySub() }
                                        plus.setOnClickListener { quantityAdd() }

                                        price.observe(viewLifecycleOwner) { price ->
                                            //hardcoded the shipping price
                                            // to change
                                            val priceWithShipping = price + 50
                                            Log.d(TAG, "priceWithShipping: $priceWithShipping")
                                            orderPrice.text = priceWithShipping.toString()
                                        }
                                        quantity.observe(viewLifecycleOwner) { quantity ->
                                            quantityTv.text = quantity.toString()
                                        }
                                    }

                                    order.setOnClickListener {

                                        getSellerById(userId, "user")
                                        user.observe(viewLifecycleOwner) { user ->
                                            if (user != null) {
                                                binding.apply {

                                                    nameEditText.setText(user.name)
                                                    phoneEditText.setText(user.phoneNumber)
                                                    if (user.address != null) {
                                                        addressEditText.setText(user.address)
                                                    }

                                                    orderModel.apply {
                                                        val userAddress = user.address

                                                        name.value = user.name
                                                        phoneNumber.value = user.phoneNumber
                                                        if (userAddress != null) {
                                                            address.value = userAddress
                                                        } else {
                                                            address.value = ""
                                                        }

                                                        nameEditText.doOnTextChanged { text, _, _, _ ->
                                                            name.value = text.toString()
                                                            Log.i(TAG, "name.value: ${name.value}")
                                                        }
                                                        phoneEditText.doOnTextChanged { text, _, _, _ ->
                                                            phoneNumber.value = text.toString()
                                                            Log.i(
                                                                TAG,
                                                                "phoneNumber.value: ${phoneNumber.value}"
                                                            )
                                                        }
                                                        addressEditText.doOnTextChanged { text, _, _, _ ->
                                                            address.value = text.toString()
                                                            Log.i(
                                                                TAG,
                                                                "address.value: ${address.value}"
                                                            )
                                                        }

                                                        dialog.show()
                                                        isValidData.observe(viewLifecycleOwner) { isValid ->
                                                            dialog
                                                                .getButton(AlertDialog.BUTTON_POSITIVE)
                                                                .isEnabled = isValid
                                                        }
                                                    }
                                                }
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

    private fun getFirebaseKey(): String {
        return try {
            val ai = requireContext().packageManager.getApplicationInfo(
                requireContext().packageName,
                PackageManager.GET_META_DATA
            ).metaData.get("FIREBASE_KEY")

            ai.toString()
        } catch (e: Throwable) {
            Log.e(TAG, "getFirebaseKey: ${e.message}")
            "key_not_found"
        }
    }


    private fun disableUi(loading: Boolean) {
        binding.apply {
            order.isEnabled = !loading
            plus.isEnabled = !loading
            minus.isEnabled = !loading
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