package alpha.company.pc.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import alpha.company.pc.R
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.DemandRepository
import alpha.company.pc.databinding.FragmentUserDemandsBinding
import alpha.company.pc.ui.adapters.DemandsAdapter
import alpha.company.pc.ui.viewmodels.UserDemandsModel
import alpha.company.pc.utils.OnDialogClicked
import alpha.company.pc.utils.makeDialog
import alpha.company.pc.utils.toast
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager

private const val TAG = "UserDemandsFragment"

class UserDemandsFragment : Fragment() {

    private lateinit var binding: FragmentUserDemandsBinding
    private lateinit var demandsModel: UserDemandsModel
    private lateinit var demandsAdapter: DemandsAdapter
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = requireActivity().intent.getStringExtra("id")
        Log.d(TAG, "userId: $userId")
        if (userId == null) {
            requireContext().toast(getString(R.string.error_msg), Toast.LENGTH_SHORT)
            requireActivity().finish()
        }
        demandsModel =
            UserDemandsModel(DemandRepository(RetrofitService.getInstance(requireContext())))
                .also {
                    it.getUserDemands(userId!!)
                }
        demandsAdapter = DemandsAdapter(object : DemandsAdapter.OnDemandClicked {
            override fun onDemandClicked(demandId: String) {
                goToDemandModify(demandId)
            }

            override fun onDeleteClicked(demandId: String): Nothing? {
                makeDialog(
                    requireContext(),
                    object : OnDialogClicked {
                        override fun onPositiveButtonClicked() {
                            demandsModel.deleteDemand(demandId)
                        }

                        override fun onNegativeButtonClicked() {
                            Log.d(TAG, "onNegativeButtonClicked demand delete canceled")
                        }
                    },
                    getString(R.string.annonce_delete_dialog_title),
                    getString(R.string.demand_delete_dialog_message)
                ).show()

                return super.onDeleteClicked(demandId)
            }
        }, true)

    }

    private fun goToDemandModify(demandId: String) {
        Log.d(TAG, "goToDemandModify $demandId")
        val action =
            UserDemandsFragmentDirections.actionUserDemandsFragmentToDemandModifyFragment(demandId)
        findNavController().navigate(action)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentUserDemandsBinding.inflate(inflater, container, false)

        binding.userDemandsRv.apply {
            adapter = demandsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.swiperefresh.setOnRefreshListener {
            demandsModel.getUserDemands(userId!!)
        }

        demandsModel.apply {
            deleted.observe(viewLifecycleOwner) { deleted ->
                if (deleted) {
                    getUserDemands(userId!!)
                    requireContext().toast(
                        getString(R.string.demand_deleted_success),
                        Toast.LENGTH_SHORT
                    )
                } else {
                    requireContext().toast(
                        getString(R.string.error_msg),
                        Toast.LENGTH_SHORT
                    )
                }
            }
            isTurning.observe(viewLifecycleOwner) { loading ->
                binding.userDemandsProgressbar.isVisible = loading
            }
            demands.observe(viewLifecycleOwner) { demands ->
                binding.apply {
                    swiperefresh.isRefreshing = false
                    if (demands == null) {
                        isEmpty.text = getString(R.string.error_msg)
                    } else {
                        if (demands.isEmpty()) {
                            isEmpty.text = getString(R.string.list_empty)
                        } else {
                            isEmpty.text = ""
                        }
                        demandsAdapter.setList(demands)
                    }
                }
            }
        }

        return binding.root
    }


}