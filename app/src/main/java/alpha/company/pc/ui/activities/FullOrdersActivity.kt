package alpha.company.pc.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import alpha.company.pc.R
import alpha.company.pc.databinding.ActivityFullOrdersBinding

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

        supportActionBar?.hide()

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_full_orders)
    }
}