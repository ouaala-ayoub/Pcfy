package alpha.company.pc.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import alpha.company.pc.R
import alpha.company.pc.data.remote.RetrofitService
import alpha.company.pc.databinding.ActivityFullOrdersBinding
import alpha.company.pc.ui.viewmodels.AuthModel
import androidx.activity.addCallback

private const val TAG = "FullOrdersActivity"

class FullOrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullOrdersBinding
    lateinit var userId: String
    var orderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

//        binding = ActivityFullOrdersBinding.inflate(layoutInflater)

        userId = intent.getStringExtra("id") as String
        orderId = intent.getStringExtra("orderId")

        Log.d(TAG, "userId : $userId , orderId: $orderId")

//        if (orderId != null) {
//            findNavController(R.id.ordersFragmentContainerView).navigate(R.id.action_ordersListFragment_to_orderPageFragment)
//
//        }

        //handle back button click
        onBackPressedDispatcher.addCallback(this) {
            if (orderId != null) {
                finish()
            }
        }

        supportActionBar?.hide()

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_full_orders)
    }
}