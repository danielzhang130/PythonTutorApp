package daniel.pythontutor.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import daniel.pythontutor.R
import daniel.pythontutor.viewmodel.MainViewModel
import javax.inject.Inject

class ActivityMain : BaseActivity() {

    companion object {
        private const val VISUALIZATION_FRAGMENT = "VISUALIZATION"
    }

    @Inject
    lateinit var mEditFragment: FragmentEdit
    @Inject
    lateinit var mVisualizationFragment: FragmentVisualization
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(MainViewModel::class.java)
        mViewModel.getVisualization().observe(this, Observer {
            if (it != null) {
                supportFragmentManager.beginTransaction()
                    .setTransition(TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(VISUALIZATION_FRAGMENT)
                    .replace(R.id.container, mVisualizationFragment)
                    .commit()
            }
        })

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, mEditFragment)
                .commitNow()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.ok) {
            mViewModel.prepareSubmission()
            mEditFragment.getText()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    public fun fragmentZoomInTransition(fragment: Fragment) {}
}
