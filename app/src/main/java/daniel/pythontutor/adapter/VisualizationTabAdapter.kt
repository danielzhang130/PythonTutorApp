package daniel.pythontutor.adapter

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import daniel.pythontutor.ui.FragmentHeap
import daniel.pythontutor.ui.FragmentStack
import daniel.pythontutor.ui.FragmentStdout

class VisualizationTabAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val mStdoutFragment = FragmentStdout()
    private val mStackFragment = FragmentStack()
    private val mHeapFragment = FragmentHeap()

    override fun getItem(position: Int) =
        when (position) {
            0 -> mStdoutFragment
            1 -> mStackFragment
            else -> mHeapFragment
        }

    override fun getCount() = 3

    override fun getPageTitle(position: Int) =
        when (position) {
            0 -> "StdOut"
            1 -> "Stack"
            else -> "Heap"
        }
}
