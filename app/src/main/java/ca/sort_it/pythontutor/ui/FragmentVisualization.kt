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
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.VISIBLE
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import ca.sort_it.pythontutor.R
import ca.sort_it.pythontutor.adapter.VisualizationTabAdapter
import ca.sort_it.pythontutor.lib.Utils
import ca.sort_it.pythontutor.viewmodel.MainViewModel
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.visualization_fragment.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min


class FragmentVisualization @Inject constructor(): BaseFragment(), Toolbar.OnMenuItemClickListener {

    companion object {
        const val CODE_INDEX = 0
        const val STDOUT_INDEX = 1
        const val STACK_INDEX = 2
        const val HEAP_INDEX = 3
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mViewModel by activityViewModels<MainViewModel> { mViewModelFactory }

    private val mFragmentParentTabAndPosition :Array<Pair<ViewPager, Int>?> = arrayOfNulls(4)

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

        assignViewTabs()

        setupViewPager()

        mViewModel.stdout.observe(viewLifecycleOwner, Observer {
            showBadge(mFragmentParentTabAndPosition[STDOUT_INDEX])
        })

        mViewModel.globals.observe(viewLifecycleOwner, Observer {
            showBadge(mFragmentParentTabAndPosition[STACK_INDEX])
        })

        mViewModel.stack.observe(viewLifecycleOwner, Observer {
            showBadge(mFragmentParentTabAndPosition[STACK_INDEX])
        })

        mViewModel.heap.observe(viewLifecycleOwner, Observer {
            showBadge(mFragmentParentTabAndPosition[HEAP_INDEX])
        })

        mViewModel.goToHeapState.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                mFragmentParentTabAndPosition[HEAP_INDEX]?.let { pair ->
                    pair.first.setCurrentItem(pair.second, true)
                }
            }
        })

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
            showBadge(mFragmentParentTabAndPosition[CODE_INDEX])
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

//        stack_text?.setOnTouchListener(object : View.OnTouchListener {
//            private var mLastX = 0f
//            private var mLastY = 0f
//            private var mDownX = 0f
//            private var mDownY = 0f
//            private val mSlop = ViewConfiguration.get(requireContext()).scaledTouchSlop
//            private var mIsScrolling = false
//            private val mGestureDetector = GestureDetector(requireContext(), object : SimpleOnGestureListener() {
//                    override fun onLongPress(e: MotionEvent) {
//                        stack_text?.isPressed = false
//                        canvas?.apply {
//                            setPreviewTarget(fragment_stack)
//                            visibility = VISIBLE
//                        }
//                    }
//                })
//
//            override fun onTouch(v: View, event: MotionEvent): Boolean {
//                mGestureDetector.onTouchEvent(event)
//
//                return canvas?.run {
//                    when (event.actionMasked) {
//                        MotionEvent.ACTION_DOWN -> {
//                            mLastX = event.rawX
//                            mLastY = event.rawY
//                            mDownX = event.rawX
//                            mDownY = event.rawY
//                            stack_text?.drawableHotspotChanged(event.x, event.y)
//                            stack_text?.isPressed = true
//                            stack_text?.performClick()
//                            return true
//                        }
//                        MotionEvent.ACTION_MOVE -> {
//                            if (mIsScrolling || abs(hypot(mDownX - event.rawX, mDownY - event.rawY)) > mSlop) {
//                                mIsScrolling = true
//                                translate(event.rawX - mLastX, event.rawY - mLastY)
//                                mLastX = event.rawX
//                                mLastY = event.rawY
//                            }
//                            return true
//                        }
//                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                            stack_text?.isPressed = false
//                            mIsScrolling = false
//                            visibility = View.GONE
//                            return true
//                        }
//                    }
//                    false
//                } ?: false
//            }
//        })

        view.post {
            if (divider_horizontal.visibility == VISIBLE) {
                Utils.addToShowcase(requireActivity(), divider_horizontal_image, Utils.Companion.ShowcaseTarget.DIVIDER, true)
            } else if (divider_vertical.visibility == VISIBLE) {
                Utils.addToShowcase(requireActivity(), divider_vertical_image, Utils.Companion.ShowcaseTarget.DIVIDER, true)
            }
        }
    }

    private fun showBadge(pair: Pair<ViewPager, Int>?) {
        pair?.let {
            if (it.first.currentItem != pair.second) {
                (it.first.getChildAt(0) as? TabLayout)?.getTabAt(pair.second)?.orCreateBadge?.isVisible = true
            }
        }
    }

    private fun assignViewTabs() {
        when {
            visualization_pager_br.visibility == VISIBLE -> {
                mFragmentParentTabAndPosition[CODE_INDEX] = visualization_pager_tl to 0
                mFragmentParentTabAndPosition[STDOUT_INDEX] = visualization_pager_bl to 0
                mFragmentParentTabAndPosition[STACK_INDEX] = visualization_pager_tr to 0
                mFragmentParentTabAndPosition[HEAP_INDEX] = visualization_pager_br to 0
            }
            visualization_pager_tr.visibility == VISIBLE -> {
                mFragmentParentTabAndPosition[CODE_INDEX] = visualization_pager_tl to 0
                mFragmentParentTabAndPosition[STDOUT_INDEX] = visualization_pager_tr to 0
                mFragmentParentTabAndPosition[STACK_INDEX] = visualization_pager_tr to 1
                mFragmentParentTabAndPosition[HEAP_INDEX] = visualization_pager_tr to 2
            }
            else -> {
                mFragmentParentTabAndPosition[CODE_INDEX] = visualization_pager_tl to 0
                mFragmentParentTabAndPosition[STDOUT_INDEX] = visualization_pager_bl to 0
                mFragmentParentTabAndPosition[STACK_INDEX] = visualization_pager_bl to 1
                mFragmentParentTabAndPosition[HEAP_INDEX] = visualization_pager_bl to 2
            }
        }
    }

    private fun setupViewPager() {
        visualization_tabs_tl.setupWithViewPager(visualization_pager_tl)
        visualization_tabs_tr.setupWithViewPager(visualization_pager_tr)
        visualization_tabs_bl.setupWithViewPager(visualization_pager_bl)
        visualization_tabs_br.setupWithViewPager(visualization_pager_br)

        mFragmentParentTabAndPosition.withIndex().forEach { (index, pair) ->
            if (pair != null) {
                var adapter = pair.first.adapter
                if (adapter == null) {
                    adapter = VisualizationTabAdapter(this.childFragmentManager, requireContext())
                    pair.first.adapter = adapter
                }
                if (adapter is VisualizationTabAdapter) {
                    adapter.addTab(
                        when (index) {
                            CODE_INDEX -> VisualizationTabAdapter.Companion.TabType.CODE
                            STDOUT_INDEX -> VisualizationTabAdapter.Companion.TabType.STDOUT
                            HEAP_INDEX -> VisualizationTabAdapter.Companion.TabType.HEAP
                            else -> VisualizationTabAdapter.Companion.TabType.STACK
                        }
                    )
                }
            }
        }

        visualization_pager_tl.addOnPageChangeListener(OnPageChangeListener(visualization_tabs_tl))
        visualization_pager_bl.addOnPageChangeListener(OnPageChangeListener(visualization_tabs_bl))
        visualization_pager_tr.addOnPageChangeListener(OnPageChangeListener(visualization_tabs_tr))
        visualization_pager_br.addOnPageChangeListener(OnPageChangeListener(visualization_tabs_br))

        mFragmentParentTabAndPosition.forEach {
            (it?.first?.getChildAt(0) as? TabLayout)?.getTabAt(it.second)?.view?.let { tabView ->
                tabView.setOnTouchListener(
                    object : View.OnTouchListener {
                        private var mLastX = 0f
                        private var mLastY = 0f
                        private var mDownX = 0f
                        private var mDownY = 0f
                        private val mSlop = ViewConfiguration.get(requireContext()).scaledTouchSlop
                        private var mIsScrolling = false
                        private val mGestureDetector = GestureDetector(requireContext(),
                            object : GestureDetector.SimpleOnGestureListener() {
                                override fun onLongPress(e: MotionEvent) {
                                    tabView.isPressed = false
                                    canvas?.apply {
                                        setPreviewTarget(it.first.getChildAt(1))
                                        visibility = VISIBLE
                                    }
                                }
                            })

                        override fun onTouch(v: View, event: MotionEvent): Boolean {
                            Log.d("ontouch", event.toString())
                            mGestureDetector.onTouchEvent(event)

                            return canvas?.run {
                                when (event.actionMasked) {
                                    MotionEvent.ACTION_DOWN -> {
                                        mLastX = event.rawX
                                        mLastY = event.rawY
                                        mDownX = event.rawX
                                        mDownY = event.rawY
                                        tabView.drawableHotspotChanged(event.x, event.y)
                                        tabView.isPressed = true
//                                        tabView.performClick()
                                        return false
                                    }
                                    MotionEvent.ACTION_MOVE -> {
                                        if (mIsScrolling ||
                                            abs(hypot(mDownX - event.rawX, mDownY - event.rawY)) > mSlop) {
                                            mIsScrolling = true
                                            translate(event.rawX - mLastX, event.rawY - mLastY)
                                            mLastX = event.rawX
                                            mLastY = event.rawY
                                        }
                                        return false
                                    }
                                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                        tabView.isPressed = false
                                        mIsScrolling = false
                                        visibility = View.GONE
                                        return false
                                    }
                                }
                                false
                            } ?: false
                        }
                    }
                )
            }
        }

        for (i in 0 until visualization_tabs_tl.tabCount) {
            visualization_tabs_tl.getTabAt(i)?.let {
                val tab = it.view

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

    class OnPageChangeListener(private val tab: TabLayout) : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {}

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

        override fun onPageSelected(position: Int) {
            tab.getTabAt(position)?.orCreateBadge?.isVisible = false
        }
    }
}
