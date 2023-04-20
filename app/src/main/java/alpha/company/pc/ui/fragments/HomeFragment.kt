package alpha.company.pc.ui.fragments

import alpha.company.pc.data.models.network.Annonce
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
import com.google.android.material.appbar.AppBarLayout


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
                getNumPages()
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
                    viewModel.apply {
                        pagesList.observe(viewLifecycleOwner) { pagesList ->
                            Log.d(TAG, "onCategoryClicked: observer")
                            val firstPage = pagesList[0].toString()
                            if (title == CategoryEnum.ALL.title) {
                                viewModel.apply {
                                    getAnnoncesListAll(firstPage)
                                }
                            } else {
                                viewModel.apply {
                                    getAnnoncesByCategory(title)
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    fun RecyclerView.handleRefreshWithScrolling() {
        this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val isRvDragging = newState == RecyclerView.SCROLL_STATE_DRAGGING
                binding.swiperefresh.isEnabled = !isRvDragging
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

//        viewModel.pagesList.observe(viewLifecycleOwner) { pagesList ->
//            viewModel.getAnnoncesListAll(pagesList[0].toString())
//        }

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
                swiperefresh.isEnabled = isScreenOnTop
            }
            popularsShimmerRv.handleRefreshWithScrolling()
            categoryShimmerRv.handleRefreshWithScrolling()

            //setting the categories list


            //setting the annonces list
            annonceRv.apply {
                layoutManager = GridLayoutManager(requireContext(), NUM_ROWS)
                adapter = annoncesAdapter
                Log.d(TAG, "showing annonce shimmer")
                showShimmerAdapter()

                scrollTop.setOnClickListener {
                    scrollToPosition(0)
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
                            val current = categoryAdapter.getCurrentCategory()
                            pagesList.observe(viewLifecycleOwner) { pagesList ->
                                Log.d(TAG, "onScrollStateChanged: observer")
                                if (current == CategoryEnum.ALL.title) {
                                    if (pagesList.lastIndex >= currentIndex) {
                                        val currentPage = pagesList[currentIndex].toString()
                                        addAnnoncesListAll(currentPage)
                                    }
                                } else {
                                    addAnnoncesByCategory(current, currentIndex.toString())
                                }
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
                //handle collapsing bar behaviour
                if (annonces.isNullOrEmpty()) {
                    disableScroll()
                } else {
                    enableScroll()
                }

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
                    viewModel.apply {
                        pagesList.observe(viewLifecycleOwner) { pagesList ->
                            Log.d(TAG, "swiperefresh: observer")
                            val firstPage = pagesList[0].toString()
                            if (currentCategory == CategoryEnum.ALL.title) {
                                viewModel.apply {
                                    getAnnoncesListAll(firstPage)
                                }
                            } else {
                                viewModel.apply {
                                    getAnnoncesByCategory(currentCategory)
                                }
                            }
                        }
                    }
                    //enhance authentication
                    val activity = requireActivity() as MainActivity
                    if (activity.isAuthRequestTimeout()) {
                        activity.apply {
//                            requireActivity().invalidateOptionsMenu()
                            auth()
                        }
                    }

                    //popular annonces
                    getPopularAnnonces()

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

    private fun enableScroll() {
        val params = binding.collapsingBar.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = (
                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                        or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
                )
        binding.collapsingBar.layoutParams = params
    }

    private fun disableScroll() {
        val params = binding.collapsingBar.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
        binding.collapsingBar.layoutParams = params
    }
}