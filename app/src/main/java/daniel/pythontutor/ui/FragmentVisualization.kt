package daniel.pythontutor.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import daniel.pythontutor.R
import daniel.pythontutor.adapter.CodeAdapter
import daniel.pythontutor.adapter.VisualizationTabAdapter
import daniel.pythontutor.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.visualization_fragment.*
import javax.inject.Inject

class FragmentVisualization @Inject constructor(): Fragment() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mViewModel: MainViewModel
    private lateinit var mAdapter: CodeAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mViewModel = ViewModelProviders.of(context as FragmentActivity, mViewModelFactory).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.visualization_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mAdapter = CodeAdapter(this@FragmentVisualization.layoutInflater,
            ContextCompat.getColor(activity!!, R.color.code_blue),
            ContextCompat.getColor(activity!!, R.color.code_orange),
            Color.TRANSPARENT)

        code_list.apply {
            layoutManager = LinearLayoutManager(this@FragmentVisualization.context)
            adapter = mAdapter
                .apply { setCode(mViewModel.getLines()) }
        }

        visualization_pager.apply {
            visualization_tabs.setupWithViewPager(this)
            adapter = VisualizationTabAdapter(this@FragmentVisualization.childFragmentManager)
            setCurrentItem(1, true)
        }

        mViewModel.getCurrentLine().observe(this, Observer {
            mAdapter.setCurrentLine(it)
            code_list.scrollToPosition(it)
        })
        mViewModel.getPrevLine().observe(this, Observer {
            mAdapter.setPrevLine(it)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.visualize, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.first -> mViewModel.goToStart()
            R.id.prev -> mViewModel.prev()
            R.id.next -> mViewModel.next()
            R.id.last -> mViewModel.goToEnd()
        }
        return true
    }
}
