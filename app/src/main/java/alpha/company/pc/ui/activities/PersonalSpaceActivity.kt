package alpha.company.pc.ui.activities

import alpha.company.pc.R
import alpha.company.pc.databinding.ActivityPersonalSpaceBinding
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class PersonalSpaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalSpaceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalSpaceBinding.inflate(layoutInflater)

        setContentView(binding.root)

    }

}
