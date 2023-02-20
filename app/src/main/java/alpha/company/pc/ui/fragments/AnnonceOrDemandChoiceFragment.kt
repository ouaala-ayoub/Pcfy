package alpha.company.pc.ui.fragments

import alpha.company.pc.R
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import alpha.company.pc.databinding.FragmentAnnonceOrDemandChoiceBinding
import alpha.company.pc.ui.activities.MainActivity
import androidx.navigation.fragment.findNavController

class AnnonceOrDemandChoiceFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentAnnonceOrDemandChoiceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.hide()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAnnonceOrDemandChoiceBinding.inflate(inflater, container, false)

        binding.apply {
            annonceChoice.setOnClickListener(this@AnnonceOrDemandChoiceFragment)
            demandChoice.setOnClickListener(this@AnnonceOrDemandChoiceFragment)
        }

        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.annonce_choice -> {
                goToCreateAnnonceFragment()
            }
            R.id.demand_choice -> {
                goToCreateDemandFragment()
            }
        }
    }

    private fun goToCreateAnnonceFragment() {
        val action =
            AnnonceOrDemandChoiceFragmentDirections.actionAnnonceOrDemandChoiceFragmentToCreateAnnonceFragment()
        findNavController().navigate(action)
    }

    private fun goToCreateDemandFragment() {
        val action =
            AnnonceOrDemandChoiceFragmentDirections.actionAnnonceOrDemandChoiceFragmentToDemandCreateFragment()
        findNavController().navigate(action)
    }

}