package alpha.company.pc.ui.activities

import alpha.company.pc.R
import alpha.company.pc.databinding.ActivitySubscriptionsBinding
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class SubscriptionsActivity : AppCompatActivity() {

//    private lateinit var binding: ActivitySubscriptionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {

//        binding = ActivitySubscriptionsBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        setContentView(R.layout.activity_subscriptions)
    }
}