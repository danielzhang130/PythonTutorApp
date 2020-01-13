package daniel.pythontutor.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import daniel.pythontutor.R
import daniel.pythontutor.model.PythonVisualization.EncodedObject
import daniel.pythontutor.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.heap.*
import javax.inject.Inject


class ActivityMain : BaseActivity() {

    companion object {
        private const val VISUALIZATION_FRAGMENT = "VISUALIZATION"
        private const val HEAP_ZOOM_FRAGMENT = "HEAP%s"
    }

    @Inject
    lateinit var mEditFragment: FragmentEdit
    @Inject
    lateinit var mVisualizationFragment: FragmentVisualization
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mViewModel: MainViewModel

    private var mCurrentAnimator: AnimatorSet? = null

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

    fun fragmentZoomInTransition(fragment: Fragment, encodedObject: EncodedObject, view: View) {
        mCurrentAnimator?.cancel()

        // start position of the view
        val startBoundsInt = Rect()
        // start position relative to the parent
        val startGlobalOffset = Point()
        // parent view box
        val finalBoundsInt = Rect()
        // parent view global offset
        val finalGlobalOffset = Point()

        view.getLocalVisibleRect(startBoundsInt)
        view.getGlobalVisibleRect(Rect(), startGlobalOffset)
        heap_root.getGlobalVisibleRect(finalBoundsInt, finalGlobalOffset)

        startGlobalOffset.offset(-finalGlobalOffset.x, -finalGlobalOffset.y)
        finalBoundsInt.offset(-finalGlobalOffset.x, -finalGlobalOffset.y)
        finalBoundsInt.offset(-startBoundsInt.left, -startBoundsInt.top)

        val startBounds = RectF(startBoundsInt)
        val finalBounds = RectF(finalBoundsInt)

        val finalScale: Float
        finalScale =
            if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
                finalBounds.height() / startBounds.height()
            } else {
                finalBounds.width() / startBounds.width()
            }

        mCurrentAnimator = AnimatorSet().apply {
            play(ObjectAnimator.ofFloat(view, View.ALPHA, view.alpha, 0.25f)).apply {
                with(ObjectAnimator.ofFloat(view, View.SCALE_X, view.scaleX, finalScale))
                with(ObjectAnimator.ofFloat(view, View.SCALE_Y, view.scaleY, finalScale))
                with(
                    ObjectAnimator.ofFloat(
                        view,
                        View.TRANSLATION_X,
                        0f,
//                        view.x,
                        (finalBounds.left + finalBounds.right - startBounds.right - startGlobalOffset.x.toFloat()) / 2
                    )
                )
                with(
                    ObjectAnimator.ofFloat(
                        view,
                        View.TRANSLATION_Y,
                        0f,
                        (finalBounds.top + finalBounds.bottom - startBounds.bottom - startGlobalOffset.y.toFloat()) / 2
                    )
                )
            }

            duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
            interpolator = AccelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                private fun reset() {
                    view.alpha = 1f
                    view.scaleX = 1f
                    view.scaleY = 1f
                    view.translationX = 0f
                    view.translationY = 0f
                    mCurrentAnimator = null
                }

                override fun onAnimationEnd(animation: Animator) {
                    reset()
                }

                override fun onAnimationCancel(animation: Animator) {
                    reset()
                }
            })
            start()
        }

        Handler().postDelayed({
            val fragmentManager =
                if (fragment is FragmentHeap) fragment.childFragmentManager
                else fragment.requireFragmentManager()

            if (encodedObject is EncodedObject.Ref) {
                val fragmentHeapZoom = FragmentHeapZoom.newInstance(
                    mViewModel.getHeap().value?.get(encodedObject.id)
                        ?: error("Encoded Object at index ${encodedObject.id} not found")
                )

                fragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.heap_root, fragmentHeapZoom)
                    .addToBackStack(HEAP_ZOOM_FRAGMENT.format(encodedObject.id))
                    .commit()
            }
        }, 200)
    }
}
