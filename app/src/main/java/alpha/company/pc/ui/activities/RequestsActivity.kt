package alpha.company.pc.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.data.repositories.OrdersRepository
import alpha.company.pc.databinding.ActivityRequestsBinding
import alpha.company.pc.ui.adapters.RequestsAdapter
import alpha.company.pc.ui.viewmodels.RequestsModel
import alpha.company.pc.utils.ERROR_MSG
import alpha.company.pc.utils.toast

private const val TAG = "RequestsActivity"

class RequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestsBinding
    private lateinit var requestsModel: RequestsModel
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityRequestsBinding.inflate(layoutInflater)
        requestsModel = RequestsModel(OrdersRepository(RetrofitService.getInstance()))
        userId = intent.getStringExtra("id") as String

        supportActionBar?.hide()

        super.onCreate(savedInstanceState)

        requestsModel.apply {

            isEmpty.observe(this@RequestsActivity) {
                binding.isRequestsEmpty.isVisible = it
            }

            getUserRequests(userId)
            userRequests.observe(this@RequestsActivity) { requests ->
                Log.i(TAG, "requests are $requests")
                if (requests == null) {
                    Log.i(TAG, "requests are $requests")
                    doOnFail()
                } else {
                    binding.requestsRv.apply {
                        adapter = RequestsAdapter(requests)
                        layoutManager = LinearLayoutManager(this@RequestsActivity)
                    }
                }
            }
            isTurning.observe(this@RequestsActivity) { isTurning ->
                binding.requestProgressBar.isVisible = isTurning
            }

            binding.apply {
                swiperefresh.setOnRefreshListener {
                    getUserRequests(userId)
                    swiperefresh.isRefreshing = false
                }
            }

        }

        setContentView(binding.root)
    }

    private fun doOnFail() {
        this.toast(ERROR_MSG, Toast.LENGTH_SHORT)
        this.finish()
    }
}