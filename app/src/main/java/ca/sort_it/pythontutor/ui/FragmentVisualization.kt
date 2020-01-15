/*
 *     Copyright (c) 2020 danielzhang130
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.sort_it.pythontutor.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import ca.sort_it.pythontutor.R
import ca.sort_it.pythontutor.adapter.CodeAdapter
import ca.sort_it.pythontutor.adapter.VisualizationTabAdapter
import ca.sort_it.pythontutor.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.visualization_fragment.*
import javax.inject.Inject

class FragmentVisualization @Inject constructor(): Fragment(), Toolbar.OnMenuItemClickListener {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mViewModel: MainViewModel
    private lateinit var mAdapter: CodeAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context as ActivityMain).lockDrawer()
        mViewModel = ViewModelProviders.of(context as FragmentActivity, mViewModelFactory).get(MainViewModel::class.java)
    }

    override fun onDetach() {
        (requireActivity() as ActivityMain).unlockDrawer()
        super.onDetach()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.visualization_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.inflateMenu(R.menu.visualize)
        toolbar.setOnMenuItemClickListener(this)

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

        mViewModel.getStdOut().observe(this, Observer {
            if (visualization_pager.currentItem != 0) {
                visualization_tabs.getTabAt(0)?.orCreateBadge?.isVisible = true
            }
        })

        mViewModel.getGlobals().observe(this, Observer {
            if (visualization_pager.currentItem != 1) {
                visualization_tabs.getTabAt(1)?.orCreateBadge?.isVisible = true
            }
        })

        mViewModel.getStack().observe(this, Observer {
            if (visualization_pager.currentItem != 1) {
                visualization_tabs.getTabAt(1)?.orCreateBadge?.isVisible = true
            }
        })

        mViewModel.getHeap().observe(this, Observer {
            if (visualization_pager.currentItem != 2) {
                visualization_tabs.getTabAt(2)?.orCreateBadge?.isVisible = true
            }
        })
        mViewModel.getGoToHeapState().observe(this, Observer {
            if (it != null) {
                visualization_pager.setCurrentItem(2, true)
            }
        })

        visualization_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                visualization_tabs.getTabAt(position)?.orCreateBadge?.isVisible = false
            }
        })
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.first -> mViewModel.goToStart()
            R.id.prev -> mViewModel.prev()
            R.id.next -> mViewModel.next()
            R.id.last -> mViewModel.goToEnd()
        }
        return true
    }
}
