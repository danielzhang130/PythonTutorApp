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
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import ca.sort_it.pythontutor.R
import ca.sort_it.pythontutor.adapter.CodeAdapter
import ca.sort_it.pythontutor.adapter.VisualizationTabAdapter
import ca.sort_it.pythontutor.lib.Utils
import ca.sort_it.pythontutor.viewmodel.MainViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.visualization_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class FragmentVisualization @Inject constructor(): BaseFragment(), Toolbar.OnMenuItemClickListener {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mViewModel by activityViewModels<MainViewModel> { mViewModelFactory }
    private lateinit var mAdapter: CodeAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context as ActivityMain).lockDrawer()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (context as ActivityMain).lockDrawer()
    }

    override fun onDetach() {
        (requireActivity() as ActivityMain).unlockDrawer()
        FirebaseAnalytics.getInstance(requireContext()).logEvent("close_vis", Bundle.EMPTY)
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

        val menuFirst = toolbar.menu.findItem(R.id.first)
        val menuPrev = toolbar.menu.findItem(R.id.prev)
        val menuLast = toolbar.menu.findItem(R.id.last)
        val menuNext = toolbar.menu.findItem(R.id.next)

        mAdapter = CodeAdapter(this@FragmentVisualization.layoutInflater,
            ContextCompat.getColor(requireActivity(), R.color.code_blue),
            ContextCompat.getColor(requireActivity(), R.color.code_orange),
            Color.TRANSPARENT)

        code_list.apply {
            layoutManager = LinearLayoutManager(this@FragmentVisualization.context)
            adapter = mAdapter
                .apply { setCode(mViewModel.getLines()) }
        }

        visualization_pager?.apply {
            visualization_tabs?.setupWithViewPager(this)
            adapter = VisualizationTabAdapter(this@FragmentVisualization.childFragmentManager, requireContext())
            setCurrentItem(1, true)
        }

        var currentLine: Int
        mViewModel.currentLine.observe(viewLifecycleOwner, Observer {
            mAdapter.setCurrentLine(it)
            currentLine = it
            CoroutineScope(Dispatchers.Main).launch {
                delay(500)
                if (currentLine != it) {
                    return@launch
                }
                code_list.scrollToPosition(it)
            }
        })
        mViewModel.prevLine.observe(viewLifecycleOwner, Observer {
            mAdapter.setPrevLine(it)
        })

        visualization_pager?.let {
            mViewModel.stdout.observe(viewLifecycleOwner, Observer { _ ->
                if (it.currentItem != 0) {
                    visualization_tabs?.getTabAt(0)?.orCreateBadge?.isVisible = true
                }
            })

            mViewModel.globals.observe(viewLifecycleOwner, Observer {_ ->
                if (it.currentItem != 1) {
                    visualization_tabs?.getTabAt(1)?.orCreateBadge?.isVisible = true
                }
            })

            mViewModel.stack.observe(viewLifecycleOwner, Observer { _ ->
                if (it.currentItem != 1) {
                    visualization_tabs?.getTabAt(1)?.orCreateBadge?.isVisible = true
                }
            })

            mViewModel.heap.observe(viewLifecycleOwner, Observer { _ ->
                if (it.currentItem != 2) {
                    visualization_tabs?.getTabAt(2)?.orCreateBadge?.isVisible = true
                }
            })
            mViewModel.goToHeapState.observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    visualization_pager?.setCurrentItem(2, true)
                }
            })
        }

        mViewModel.totalSteps.observe(viewLifecycleOwner, Observer {
            toolbar.title = "${mViewModel.currentStep.value?.plus(1)}/$it"
        })

        mViewModel.currentStep.observe(viewLifecycleOwner, Observer {
            toolbar.title = "${it?.plus(1)}/${mViewModel.totalSteps.value}"
            when (it) {
                0 -> {
                    menuFirst.isEnabled = false
                    menuPrev.isEnabled = false
                    menuLast.isEnabled = true
                    menuNext.isEnabled = true
                }
                mViewModel.totalSteps.value?.minus(1) -> {
                    menuFirst.isEnabled = true
                    menuPrev.isEnabled = true
                    menuLast.isEnabled = false
                    menuNext.isEnabled = false
                }
                else -> {
                    menuFirst.isEnabled = true
                    menuPrev.isEnabled = true
                    menuLast.isEnabled = true
                    menuNext.isEnabled = true
                }
            }
        })

        visualization_pager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                visualization_tabs?.getTabAt(position)?.orCreateBadge?.isVisible = false
            }
        })

        divider_horizontal.setOnTouchListener(object : View.OnTouchListener {
            private var mLastY = 0f
            private var mDownY = 0f
            private val mSlop = ViewConfiguration.get(requireContext()).scaledTouchSlop
            private var mIsScrolling = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when(event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        mLastY = event.rawY
                        mDownY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (mIsScrolling || abs(mDownY - event.rawY) > mSlop) {
                            mIsScrolling = true
                            val distance = event.rawY - mLastY
                            val layoutParams =
                                guideline_horizontal?.layoutParams as ConstraintLayout.LayoutParams
                            this@FragmentVisualization.view?.height?.let {
                                layoutParams.guidePercent += distance/it
                                layoutParams.guidePercent = max(0.18F, min(0.85F, layoutParams.guidePercent))
                                guideline_horizontal?.layoutParams = layoutParams
                            }
                            mLastY = event.rawY
                        }
                        return true
                    }
                    MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> mIsScrolling = false
                }
                return false
            }
        })

        divider_vertical.setOnTouchListener(object : View.OnTouchListener {
            private var mLastX = 0f
            private var mDownX = 0f
            private val mSlop = ViewConfiguration.get(requireContext()).scaledTouchSlop
            private var mIsScrolling = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when(event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        mLastX = event.rawX
                        mDownX = event.rawX
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (mIsScrolling || abs(mDownX - event.rawX) > mSlop) {
                            mIsScrolling = true
                            val distance = event.rawX - mLastX
                            val layoutParams =
                                guideline_vertical?.layoutParams as ConstraintLayout.LayoutParams
                            this@FragmentVisualization.view?.width?.let {
                                layoutParams.guidePercent += distance/it
                                layoutParams.guidePercent = max(0.18F, min(0.85F, layoutParams.guidePercent))
                                guideline_vertical?.layoutParams = layoutParams
                            }
                            mLastX = event.rawX
                        }
                        return true
                    }
                    MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> mIsScrolling = false
                }
                return false
            }
        })

        view.post {
            if (divider_horizontal_image != null) {
                Utils.addToShowcase(requireActivity(), divider_horizontal_image, Utils.Companion.ShowcaseTarget.DIVIDER, true)
            } else if (divider_vertical_image != null) {
                Utils.addToShowcase(requireActivity(), divider_vertical_image, Utils.Companion.ShowcaseTarget.DIVIDER, true)
            }
        }
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
