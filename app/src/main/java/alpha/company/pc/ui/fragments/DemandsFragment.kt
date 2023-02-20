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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager

private const val NUM_ROWS = 2
private const val TAG = "DemandsFragment"

class DemandsFragment : Fragment() {

    private lateinit var binding: FragmentDemandsBinding
    private lateinit var demandsModel: DemandsModel
    private lateinit var demandsAdapter: DemandsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        demandsModel = DemandsModel(
            DemandRepository(RetrofitService.getInstance()), MessageText(
                "",
                getString(R.string.error),
                getString(R.string.list_empty)
            )
        )
        demandsModel.getDemands()

        demandsAdapter = DemandsAdapter(object : DemandsAdapter.OnDemandClicked {
            override fun onDemandClicked(demandId: String) {
                goToDemandFragment(demandId)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDemandsBinding.inflate(inflater, container, false)

        binding.apply {
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
                            demandsAdapter.setDemandsList(demands)
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