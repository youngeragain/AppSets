package xcj.app.appsets.ui.nonecompose

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import xcj.app.appsets.databinding.ActivityCurveViewBinding

class CurveViewActivity : AppCompatActivity() {
    private lateinit var binding:ActivityCurveViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCurveViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnDoAction.setOnClickListener {
            binding.curveView.doAnimate()
        }
    }
}