package alpha.company.pc.ui.fragments

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
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.OrdersRepository
import alpha.company.pc.databinding.FragmentOrdersListBinding
import alpha.company.pc.ui.activities.FullOrdersActivity
import alpha.company.pc.ui.activities.LoginActivity
import alpha.company.pc.ui.adapters.OrdersFullAdapter
import alpha.company.pc.ui.adapters.OrdersShortAdapter
import alpha.company.pc.ui.viewmodels.AuthModel
import alpha.company.pc.ui.viewmodels.FullOrdersModel
import alpha.company.pc.utils.ERROR_MSG
import alpha.company.pc.utils.toast
import android.content.Intent

private const val TAG = "OrdersListFragment"

class OrdersListFragment : Fragment() {

    private lateinit var binding: FragmentOrdersListBinding
    private lateinit var userId: String
    private var orderId: String? = null
    private lateinit var fullOrdersModel: FullOrdersModel
    private lateinit var authModel: AuthModel

    override fun onCreate(savedInstanceState: Bundle?) {
        val activity = requireActivity() as FullOrdersActivity

        authModel =
            AuthModel(RetrofitService.getInstance(requireContext())).also { it.auth(requireContext()) }

        userId = activity.userId
        orderId = activity.orderId

        Log.d(TAG, "userId : $userId , orderId: $orderId")

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOrdersListBinding.inflate(inflater, container, false)

        authModel.apply {
            user.observe(viewLifecycleOwner) { user ->
                Log.d(TAG, "authModel user: $user")
                if (user != null) {
                    fullOrdersModel =
                        FullOrdersModel(OrdersRepository(RetrofitService.getInstance(requireContext()))).also {
                            it.initialiseAdd(requireContext())
                        }
                    orderId?.let {
                        Log.d(TAG, "orderId : going to order page")
                        goToOrderPage(it)
                    }

                    fullOrdersModel.apply {
                        Log.d(TAG, "onCreateView userId: $userId")
                        getSellerOrders(userId)
                        sellerOrders.observe(requireActivity()) { orders ->
                            Log.i(TAG, "orders are $orders")
                            if (orders == null) {
                                Log.i(TAG, "orders are $orders")
                                doOnFail()
                            } else {
                                binding.ordersRv.apply {
                                    adapter =
                                        OrdersFullAdapter(
                                            orders,
                                            object : OrdersShortAdapter.OnOrderClicked {
                                                override fun onOrderClicked(orderId: String) {
                                                    showAdd(requireContext())
                                                    goToOrderPage(orderId)
                                                }
                                            })
                                    layoutManager = LinearLayoutManager(requireContext())
                                }
                            }
                        }

                        isEmpty.observe(requireActivity()) {
                            binding.isOrdersEmpty.isVisible = it
                        }

                        isTurning.observe(viewLifecycleOwner) { isTurning ->
                            binding.ordersProgressBar.isVisible = isTurning
                        }
                        binding.apply {
                            swiperefresh.setOnRefreshListener {
                                getSellerOrders(userId)
                                swiperefresh.isRefreshing = false
                            }
                        }
                    }

                } else {
                    goToLoginFragment()
                }
            }
        }



        return binding.root
    }

    private fun doOnFail() {
        requireContext().toast(ERROR_MSG, Toast.LENGTH_SHORT)
        val activity = requireActivity() as FullOrdersActivity
        activity.finish()
    }

    private fun goToOrderPage(orderId: String) {
        val action =
            OrdersListFragmentDirections.actionOrdersListFragmentToOrderPageFragment(orderId)
        findNavController().navigate(action)
    }

    private fun goToLoginFragment() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

}