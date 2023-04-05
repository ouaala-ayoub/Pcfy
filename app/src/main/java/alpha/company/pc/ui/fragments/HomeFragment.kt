package alpha.company.pc.ui.fragments

import alpha.company.pc.data.models.network.Annonce
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import alpha.company.pc.data.models.network.CategoryEnum
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.HomeRepository
import alpha.company.pc.databinding.FragmentHomeBinding
import alpha.company.pc.ui.activities.AnnonceActivity
import alpha.company.pc.ui.activities.MainActivity
import alpha.company.pc.ui.adapters.AnnoncesAdapter
import alpha.company.pc.ui.adapters.CategoryAdapter
import alpha.company.pc.ui.adapters.PopularsAdapter
import alpha.company.pc.ui.viewmodels.HomeModel
import alpha.company.pc.utils.ERROR_MSG

private const val NUM_ROWS = 2
private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private lateinit var annoncesAdapter: AnnoncesAdapter
    private lateinit var popularsAdapter: PopularsAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var viewModel: HomeModel
    private lateinit var onClickListener: AnnoncesAdapter.OnAnnonceClickListener
    private lateinit var binding: FragmentHomeBinding
    private var annoncesList = mutableListOf<Annonce>()

    //    private val adBuilder = AdRequest.Builder()
    override fun onCreate(savedInstanceState: Bundle?) {
        val retrofitService = RetrofitService.getInstance(requireContext())
        super.onCreate(savedInstanceState)

        viewModel = HomeModel(HomeRepository(retrofitService)).also {
            it.apply {
                getCategories()
                getPopularAnnonces()
                getAnnoncesListAll()
            }
        }

        onClickListener = object : AnnoncesAdapter.OnAnnonceClickListener {
            override fun onAnnonceClick(annonceId: String) {
                goToAnnonceActivity(annonceId)
            }

            override fun onAnnonceLoadFail() {
//                findNavController().popBackStack()
                Log.e(TAG, "onAnnonceLoadFail : something went wrong with loading the annonce")
            }
        }
        annoncesAdapter = AnnoncesAdapter(onClickListener, annoncesList)
        popularsAdapter = PopularsAdapter(onClickListener)
        categoryAdapter = CategoryAdapter(
            object : CategoryAdapter.OnCategoryClickedListener {
                override fun onCategoryClicked(title: String) {
                    annoncesAdapter.setAnnoncesListFromAdapter(listOf())
                    if (title == CategoryEnum.ALL.title) {
                        viewModel.apply {
                            getAnnoncesListAll()
                        }
                    } else {
                        viewModel.apply {
                            getAnnoncesByCategory(title)
                        }
                    }
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.categoryShimmerRv.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = categoryAdapter
            Log.d(TAG, "showing categories shimmer")
            showShimmerAdapter()
        }
        binding.apply {

//            val adRequest = adBuilder.build()
//            Log.d(TAG, "adRequest: $adRequest")
//            adView.loadAd(adRequest)

            vAppBar.addOnOffsetChangedListener { _, verticalOffset ->
                val isScreenOnTop = verticalOffset == 0
                Log.d(TAG, "addOnOffsetChangedListener isScreenOnTop : $isScreenOnTop")
                swiperefresh.isEnabled = isScreenOnTop
            }
            popularsShimmerRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val isRvDragging = newState == RecyclerView.SCROLL_STATE_DRAGGING
                    swiperefresh.isEnabled = !isRvDragging
                    Log.d(TAG, "onScrollStateChanged isRvDragging : $isRvDragging")
                }
            })

            //setting the categories list


            //setting the annonces list
            annonceRv.apply {
                layoutManager = GridLayoutManager(requireContext(), NUM_ROWS)
                adapter = annoncesAdapter
                Log.d(TAG, "showing annonce shimmer")
                showShimmerAdapter()

                scrollTop.setOnClickListener {
                    smoothScrollToPosition(0)
                }

            }

            //setting the popular annonces list
            popularsShimmerRv.apply {
                layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                adapter = popularsAdapter
                Log.d(TAG, "showing populars shimmer")
                showShimmerAdapter()
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.show()

        viewModel.apply {

            newAnnoncesAdded.observe(viewLifecycleOwner) { newAnnoncesAdded ->

                Log.d(TAG, "newAnnoncesAdded: $newAnnoncesAdded ")
                binding.annonceRv.clearOnScrollListeners()
                val scrollListener = object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (!recyclerView.canScrollVertically(1) &&
                            newState == RecyclerView.SCROLL_STATE_IDLE &&
                            !annoncesAdapter.isListEmpty() &&
                            newAnnoncesAdded
                        ) {
                            Log.d(TAG, "onScrollStateChanged sending the requests")
                            val current = categoryAdapter.getCurrentCategory()
                            if (current == CategoryEnum.ALL.title) {
                                addAnnoncesListAll()
                            } else {
                                addAnnoncesByCategory(current)
                            }
                        }
                    }
                }
                binding.annonceRv.addOnScrollListener(scrollListener)
            }

            categoriesList.observe(viewLifecycleOwner) { categories ->
                categoryAdapter.setCategoriesList(categories)
                Log.d(TAG, "hiding categories shimmer")
                binding.categoryShimmerRv.hideShimmerAdapter()
            }

            annoncesList.observe(viewLifecycleOwner) { annonces ->
                if (annonces != null) {
                    binding.swiperefresh.isRefreshing = false
                    val annonceRv = binding.annonceRv.layoutManager
                    val recyclerViewState =
                        annonceRv?.onSaveInstanceState()
                    annoncesAdapter.setAnnoncesListFromAdapter(annonces)
                    annonceRv?.onRestoreInstanceState(recyclerViewState)

                    Log.d(TAG, "hiding annonce shimmer")
                    binding.annonceRv.hideShimmerAdapter()

                } else {
                    Log.e(TAG, "annoncesList is $annonces")
                }

                updateIsEmpty()
                emptyMsg.observe(viewLifecycleOwner) { msg ->
                    Log.i(TAG, "updateIsEmpty: $msg")
                    if (msg.isEmpty()) {
                        binding.noAnnonce.visibility = View.GONE
                    } else {
                        if (msg == ERROR_MSG) {
                            binding.apply {
                                popularTv.visibility = View.GONE
                                foruTv.visibility = View.GONE
                            }
                        }
                        binding.noAnnonce.apply {
                            visibility = View.VISIBLE
                            text = msg
                        }
                    }
                }

            }

            popularsList.observe(viewLifecycleOwner) { populars ->
                if (populars != null) {
                    popularsAdapter.setPopularsList(populars)
                    binding.popularsShimmerRv.hideShimmerAdapter()
                }
            }

            binding.apply {
                swiperefresh.setOnRefreshListener {
//                    val adRequest = adBuilder.build()
                    val currentCategory = categoryAdapter.getCurrentCategory()

//                    adView.loadAd(adRequest)
                    if (categoryAdapter.isEmptyList()) {
                        viewModel.getCategories()
                    }
                    if (currentCategory == CategoryEnum.ALL.title) {
                        getAnnoncesListAll()
                    } else {
                        getAnnoncesByCategory(currentCategory)
                    }
                    getPopularAnnonces()

                }
                annoncesList.observe(viewLifecycleOwner) { annonces ->

                }
            }

            isProgressBarTurning.observe(viewLifecycleOwner) {
                binding.homeProgressBar.isVisible = it
            }

        }
    }

    private fun goToAnnonceActivity(annonceId: String) {
        val intent = Intent(activity, AnnonceActivity::class.java)
        intent.putExtra("id", annonceId)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}