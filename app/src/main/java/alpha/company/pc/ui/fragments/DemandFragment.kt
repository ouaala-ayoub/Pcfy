package alpha.company.pc.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import alpha.company.pc.R
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.DemandRepository
import alpha.company.pc.data.repositories.UserInfoRepository
import alpha.company.pc.databinding.FragmentDemandBinding
import alpha.company.pc.ui.viewmodels.DemandModel
import alpha.company.pc.utils.*
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso

class DemandFragment : Fragment() {
    private lateinit var binding: FragmentDemandBinding
    private lateinit var demandId: String
    private lateinit var demandModel: DemandModel
    private lateinit var picasso: Picasso

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navArgs: DemandFragmentArgs by navArgs()
        val retrofitService = RetrofitService.getInstance(requireContext())
        picasso = Picasso.get()
        demandId = navArgs.demandId
        demandModel =
            DemandModel(DemandRepository(retrofitService), UserInfoRepository((retrofitService)))
                .also {
                    it.getDemandById(demandId)
                }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDemandBinding.inflate(inflater, container, false)

        binding.apply {
            demandModel.apply {

                isTurning.observe(viewLifecycleOwner) { isTurning ->
                    demandProgressBar.isVisible = isTurning
                }

                demand.observe(viewLifecycleOwner) { demand ->
                    if (demand != null) {
                        getUserById(demand.creatorId)
                        creator.observe(viewLifecycleOwner) { creator ->
                            if (creator != null) {

                                creatorInfo.setOnClickListener {
                                    goToSellerPage(creator.userId!!)
                                }
                                demandCreator.setOnClickListener {
                                    goToSellerPage(creator.userId!!)
                                }
                                creatorName.text = creator.name
                                picasso
                                    .load("$USERS_AWS_S3_LINK${creator.imageUrl}")
                                    .placeholder(circularProgressBar(requireContext()))
                                    .fit()
                                    .error(R.drawable.ic_baseline_no_photography_24)
                                    .into(creatorImage)
                            } else {
                                creatorName.text = getString(R.string.error)
                                creatorImage.setImageResource(R.drawable.ic_baseline_no_photography_24)
                            }
                        }
                        picasso
                            .load("$DEMANDS_AWS_S3_LINK${demand.picture}")
                            .placeholder(circularProgressBar(requireContext()))
                            .fit()
                            .error(R.drawable.ic_baseline_no_photography_24)
                            .into(demandPageImage)

                        demandTitle.text = demand.title

                        if (!demand.description.isNullOrBlank()) {
                            demandDescription.text = demand.description
                        } else {
                            demandDescription.text = getString(R.string.no_description)
                        }
                        if (!demand.price.isNullOrBlank()) {
                            demandPrice.text =
                                binding.root.context.getString(R.string.price, demand.price)
                        } else {
                            demandPrice.text = getString(R.string.no_price)
                        }
                    } else {
                        requireContext().toast(getString(R.string.error), Toast.LENGTH_SHORT)
                        findNavController().popBackStack()
                    }
                }
            }
        }

        return binding.root
    }

    private fun goToSellerPage(userId: String) {
        val action = DemandFragmentDirections.actionDemandFragmentToSellerInfoFragment(userId)
        findNavController().navigate(action)
    }
}