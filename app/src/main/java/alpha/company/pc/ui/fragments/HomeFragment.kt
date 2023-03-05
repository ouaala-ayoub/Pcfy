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
import com.google.android.gms.ads.AdRequest

private const val NUM_ROWS = 2
private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private lateinit var annoncesAdapter: AnnoncesAdapter
    private lateinit var popularsAdapter: PopularsAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var viewModel: HomeModel
    private lateinit var onClickListener: AnnoncesAdapter.OnAnnonceClickListener
    private var binding: FragmentHomeBinding? = null
    private var annoncesList = mutableListOf<Annonce>()
//    private val adBuilder = AdRequest.Builder()
    override fun onCreate(savedInstanceState: Bundle?) {
        val retrofitService = RetrofitService.getInstance(requireContext())
        super.onCreate(savedInstanceState)

        viewModel = HomeModel(HomeRepository(retrofitService))

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
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel.apply {
            getCategories()
            getPopularAnnonces()
            getAnnoncesListAll()
        }

        binding!!.categoryShimmerRv.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = categoryAdapter
            Log.d(TAG, "showing categories shimmer")
            showShimmerAdapter()
        }
        binding!!.apply {

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
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.show()
//        Thread.sleep(5000)


        viewModel.apply {

            categoriesList.observe(viewLifecycleOwner) { categories ->
                categoryAdapter.setCategoriesList(categories)
                Log.d(TAG, "hiding categories shimmer")
                binding!!.categoryShimmerRv.hideShimmerAdapter()
            }

            annoncesList.observe(viewLifecycleOwner) { annonces ->

                Log.d(TAG, "annonces: $annonces")
                if (annonces != null) {

                    val annonceRv = binding!!.annonceRv.layoutManager
                    val recyclerViewState =
                        annonceRv?.onSaveInstanceState()
                    annoncesAdapter.addElements(annonces)
                    annonceRv?.onRestoreInstanceState(recyclerViewState)

                    Log.d(TAG, "hiding annonce shimmer")
                    binding!!.annonceRv.hideShimmerAdapter()

                } else {
                    Log.e(TAG, "annoncesList is $annonces")
                }

                updateIsEmpty()
                emptyMsg.observe(viewLifecycleOwner) { msg ->
                    Log.i(TAG, "updateIsEmpty: $msg")
                    if (msg.isEmpty()) {
                        binding!!.noAnnonce.visibility = View.GONE
                    } else {
                        if (msg == ERROR_MSG) {
                            binding!!.apply {
                                popularTv.visibility = View.GONE
                                foruTv.visibility = View.GONE
                            }
                        }
                        binding!!.noAnnonce.visibility = View.VISIBLE
                        binding!!.noAnnonce.text = msg
                    }
                }

            }

            popularsList.observe(viewLifecycleOwner) { populars ->
                if (populars != null) {
                    val popularsList = populars.map { popular -> popular.title }
                    Log.d(TAG, "popularsList: $popularsList")
                    popularsAdapter.setPopularsList(populars)
                    Log.d(TAG, "hiding populars shimmer")
                    binding!!.popularsShimmerRv.hideShimmerAdapter()
                } else {
                    Log.e(TAG, "popularsList is : $populars")
                }
            }

            binding!!.apply {
                swiperefresh.setOnRefreshListener {
//                    val adRequest = adBuilder.build()
                    val current = categoryAdapter.getCurrentCategory()

//                    adView.loadAd(adRequest)
                    if (categoryAdapter.isEmptyList()) {
                        viewModel.getCategories()
                    }
                    annoncesAdapter.freeList()
                    if (current == CategoryEnum.ALL.title) {
                        getAnnoncesListAll()
                    } else {
                        getAnnoncesByCategory(current)
                    }
                    getPopularAnnonces()
                    swiperefresh.isRefreshing = false
                }
                annonceRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (!recyclerView.canScrollVertically(1) &&
                            newState == RecyclerView.SCROLL_STATE_IDLE &&
                            !annoncesAdapter.isListEmpty()
                        ) {
                            Log.i(TAG, "end")
                            val current = categoryAdapter.getCurrentCategory()
                            if (current == CategoryEnum.ALL.title) {
                                getAnnoncesListAll()
                            } else {
                                getAnnoncesByCategory(current)
                            }
                        }
                    }
                })
            }

            isProgressBarTurning.observe(viewLifecycleOwner) {
                binding!!.homeProgressBar.isVisible = it
            }

        }
    }

    override fun onResume() {
        super.onResume()
//        (requireActivity() as MainActivity).supportActionBar?.show()
//        binding!!.annonceRv.hideShimmerAdapter()
    }

    private fun goToAnnonceActivity(annonceId: String) {
        val intent = Intent(activity, AnnonceActivity::class.java)
        intent.putExtra("id", annonceId)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}