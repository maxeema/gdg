package maxeem.america.gdg

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.activity_main.*
import maxeem.america.gdg.ui.BaseFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private val currentFrag get() = runCatching {
        (navHostFrag as NavHostFragment).childFragmentManager.fragments.first() as BaseFragment
    }.getOrNull()

    override fun onKeyDown(keyCode: Int, event: KeyEvent?) =
        (keyCode == KeyEvent.KEYCODE_BACK && currentFrag?.consumeBackPressed() ?: false)
                || super.onKeyDown(keyCode, event)

}
