package alpha.company.pc.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import alpha.company.pc.R
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.DemandRepository
import alpha.company.pc.databinding.FragmentDemandsBinding
import alpha.company.pc.ui.adapters.DemandsAdapter
import alpha.company.pc.ui.viewmodels.DemandsModel
import alpha.company.pc.ui.viewmodels.MessageText
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager

private const val TAG = "DemandsFragment"

class DemandsFragment : Fragment() {

    private lateinit var binding: FragmentDemandsBinding
    private lateinit var demandsModel: DemandsModel
    private lateinit var demandsAdapter: DemandsAdapter
//    private var demandsToShow = mutableListOf<Demand>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        demandsModel = DemandsModel(
            DemandRepository(RetrofitService.getInstance()), MessageText(
                "",
                getString(R.string.error),
                getString(R.string.list_empty)
            )
        )
        demandsAdapter = DemandsAdapter(object : DemandsAdapter.OnDemandClicked {
            override fun onDemandClicked(demandId: String) {
                goToDemandFragment(demandId)
            }
        })
        demandsModel.getDemands()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDemandsBinding.inflate(inflater, container, false)

        binding.apply {
            demandsRv.showShimmerAdapter()
            swipeRefresh.apply {
                setOnRefreshListener {
                    demandsAdapter.freeList()
                    demandsModel.getDemands()
                    isRefreshing = false
                }
            }

            demandsRv.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = demandsAdapter

                demandsModel.apply {
                    messageTv.observe(viewLifecycleOwner) { message ->
                        //bind the message value to the textView
                        msg.text = message
                    }
                    demandsList.observe(viewLifecycleOwner) { demands ->
                        //show the data fetched
                        if (demands != null) {
                            if (demandsAdapter.isListEmpty()) {
                                Log.d(TAG, "setting demands list $demands")
                                demandsAdapter.setList(demands)
                                hideShimmerAdapter()
                            } else {
                                Log.d(TAG, "adding demands : ")
                                demandsAdapter.addDemands(demands)
                            }

                        } else {
                            Log.e(TAG, "demands is: $demands")
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private fun goToDemandFragment(demandId: String) {
        Log.i(TAG, "goToDemandFragment: $demandId")
    }
}