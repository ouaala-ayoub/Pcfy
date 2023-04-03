package alpha.company.pc.ui.fragments

import alpha.company.pc.R
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.DemandRepository
import alpha.company.pc.databinding.FragmentDemandsBinding
import alpha.company.pc.ui.adapters.DemandsAdapter
import alpha.company.pc.ui.viewmodels.DemandsModel
import alpha.company.pc.ui.viewmodels.MessageText
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

private const val TAG = "DemandsFragment"

class DemandsFragment : Fragment() {

    private lateinit var binding: FragmentDemandsBinding
    private lateinit var demandsModel: DemandsModel
    private lateinit var demandsAdapter: DemandsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        demandsModel = DemandsModel(
            DemandRepository(RetrofitService.getInstance(requireContext())), MessageText(
                "",
                getString(R.string.error),
                getString(R.string.list_empty)
            )
        ).also {
            it.getDemands()
        }
        demandsAdapter = DemandsAdapter(object : DemandsAdapter.OnDemandClicked {
            override fun onDemandClicked(demandId: String) {
                demandsModel.rvState = binding.demandsRv.layoutManager?.onSaveInstanceState()
                goToDemandFragment(demandId)
            }
        }, Picasso.get())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDemandsBinding.inflate(inflater, container, false)
        binding.apply {
            demandsModel.isTurning.observe(viewLifecycleOwner) { isTurning ->
                demandsPrpgressBar.isVisible = isTurning
            }
            //maybe it's the reason the recycler view doesn't keep its state after resume
            demandSearchView.onActionViewExpanded()

            swipeRefresh.apply {
                setOnRefreshListener {
                    demandsModel.getDemands()
                }
            }

            demandSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
//                    demandsModel.getDemands(query)
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
//                    demandsAdapter.freeList()
                    if (!newText.isNullOrBlank()) {
                        demandsModel.getDemands(newText)
                    } else {
                        demandsModel.getDemands()
                    }
                    return false
                }
            })

            demandsRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (!recyclerView.canScrollVertically(1) &&
                        newState == RecyclerView.SCROLL_STATE_IDLE &&
                        !demandsAdapter.isListEmpty()
                    ) {
                        //add more data when bottom is reached
                        demandsModel.addDemands()
                    }
                }
            })

            demandsRv.apply {

                layoutManager = LinearLayoutManager(requireContext())
                adapter = demandsAdapter

                scrollTopDemands.setOnClickListener {
                    smoothScrollToPosition(0)
                }

                showShimmerAdapter()

                demandsModel.apply {
                    messageTv.observe(viewLifecycleOwner) { message ->
                        //bind the message value to the textView
                        msg.text = message
                    }
                    demandsList.observe(viewLifecycleOwner) { demands ->
                        //show the data fetched
                        if (demands != null) {
                            Log.d(TAG, "demandsList.observe demands")
                            //to save the state of the recycler view (to fix later)
                            swipeRefresh.isRefreshing = false

                            //save state after getting new demands
                            val recyclerViewState =
                                demandsRv.layoutManager?.onSaveInstanceState()
                            demandsAdapter.setList(demands)
                            demandsRv.layoutManager?.onRestoreInstanceState(recyclerViewState)

                            hideShimmerAdapter()
                        } else {
                            Log.e(TAG, "demands is: $demands")
                        }
                    }
                }
//                }
            }
        }

        return binding.root
    }


    override fun onResume() {
        super.onResume()
        if (demandsModel.rvState != null) {
            //resume recycler view state onResume fragment
            binding.demandsRv.layoutManager?.onRestoreInstanceState(demandsModel.rvState)
        }
    }

    private fun goToDemandFragment(demandId: String) {
        Log.d(TAG, "goToDemandFragment: $demandId")
        val action = DemandsFragmentDirections.actionDemandsFragmentToDemandFragment(demandId)
        findNavController().navigate(action)
    }
}