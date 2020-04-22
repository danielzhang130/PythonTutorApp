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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify.WEB_URLS
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.text.util.LinkifyCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ca.sort_it.pythontutor.BuildConfig
import ca.sort_it.pythontutor.R
import ca.sort_it.pythontutor.lib.Utils
import ca.sort_it.pythontutor.model.PythonVisualization.EncodedObject
import ca.sort_it.pythontutor.viewmodel.MainViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.heap.*
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.text.StringEscapeUtils
import java.io.InputStream
import javax.inject.Inject


class ActivityMain : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        private const val SELECT_CONTENT = "select_content"
        private const val EDIT_FRAGMENT = "EDIT"
        private const val VISUALIZATION_FRAGMENT = "VISUALIZATION"
        private const val HEAP_ZOOM_FRAGMENT = "HEAP%s"

        private val EXAMPLES = mapOf(
            R.id.hello_world to "hello_world.txt",
            R.id.intro to "intro.txt",
            R.id.insertion_sort to "insertion_sort.txt",
            R.id.for_else to "for_else.txt",
            R.id.factorial to "factorial.txt",
            R.id.square_root to "square_root.txt",
            R.id.gcd to "gcd.txt",
            R.id.hanoi to "tower_of_hanoi.txt",
            R.id.oop1 to "oop_1.txt",
            R.id.oop2 to "oop_2.txt",
            R.id.oop3 to "oop_3.txt"
        )

        private const val REQUEST_SAVE_FILE = 2
        private const val REQUEST_LOAD_FILE = 3
    }

    @Inject
    lateinit var mEditFragment: FragmentEdit
    @Inject
    lateinit var mVisualizationFragment: FragmentVisualization
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mViewModel: MainViewModel

    private var mCurrentAnimator: AnimatorSet? = null

    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUiMode()
        setContentView(R.layout.main_activity)

        setSupportActionBar(toolbar)

        toggle = object :
            ActionBarDrawerToggle(this, drawer_layout, R.string.open_drawer, R.string.close_drawer) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                Utils.hideKeyboard(this@ActivityMain)
                drawer.post{
                    val out = ArrayList<View>()
                    drawer.findViewsWithText(out, getString(R.string.intro_to_python), View.FIND_VIEWS_WITH_TEXT)
                    if (out.isNotEmpty()) {
                        Utils.addToShowcase(this@ActivityMain, out[0], Utils.Companion.ShowcaseTarget.DRAWER_EXAMPLE) {
                            (out[0].parent as View).performClick()
                        }
                    }
                }
            }
        }

        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        drawer.setNavigationItemSelectedListener(this)

        mViewModel = ViewModelProvider(this, mViewModelFactory).get(MainViewModel::class.java)
        mViewModel.newResult.observe(this, Observer {
            if (it != null && it) {
                if (supportFragmentManager.findFragmentByTag(VISUALIZATION_FRAGMENT) == null) {
                    supportFragmentManager.beginTransaction()
                        .setTransition(TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(VISUALIZATION_FRAGMENT)
                        .replace(R.id.drawer_layout, mVisualizationFragment, VISUALIZATION_FRAGMENT)
                        .commit()
                }
                mViewModel.newResultReceived()
            }
        })
        mViewModel.uncaughtException.observe(this, Observer {
            if (it != null) {
                mViewModel.uncaughtExceptionHandled()

                AlertDialog.Builder(this)
                    .setTitle("Uncaught Exception")
                    .setMessage("line %s: %s".format(it.line, it.exceptionMsg))
                    .setNeutralButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        })
        mViewModel.loadingState.observe(this, Observer {
            if (it == true) {
                showProgress()
            } else {
                hideProgress()
            }
        })
        mViewModel.errorState.observe(this, Observer {
            if (it == true) {
                mViewModel.errorHandled()

                AlertDialog.Builder(this)
                    .setTitle("An error occurred")
                    .setMessage("Please try again later")
                    .setNeutralButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        })

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, mEditFragment, EDIT_FRAGMENT)
                .commitNow()
        } else {
            mEditFragment = supportFragmentManager.findFragmentByTag(EDIT_FRAGMENT) as FragmentEdit?
                ?: mEditFragment
            mVisualizationFragment = supportFragmentManager.findFragmentByTag(VISUALIZATION_FRAGMENT) as FragmentVisualization? ?:
                    mVisualizationFragment
        }

        toolbar.post {
            Utils.addToShowcase(this, toolbar, Utils.Companion.ShowcaseTarget.DRAWER_TOGGLE) {
                drawer_layout.openDrawer(GravityCompat.START)
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent) : Boolean{
        setKeyTyped(event.unicodeChar.toChar())
        return super.dispatchKeyEvent(event)
    }

    fun setKeyTyped(char: Char) {
        mViewModel.setKeyTyped(char)
    }

    private fun showProgress() {
        progress_bar.visibility = VISIBLE
    }

    private fun hideProgress() {
        progress_bar.visibility = GONE
    }

    private fun setUiMode() {
        when (Utils.readSharedSetting(this, getString(R.string.dark_mode)) ?: "0") {
            "0" ->
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            "1" ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

            "2" ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.ok) {
            FirebaseAnalytics.getInstance(this).logEvent("run_code", Bundle.EMPTY)

            runCode()
            return true
        } else if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun runCode() {
        Utils.hideKeyboard(this)
        mViewModel.submit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawer_layout.closeDrawer(GravityCompat.START)

        when (item.itemId) {
            R.id.save_as_file -> {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/x-python"
                }
                startActivityForResult(intent, REQUEST_SAVE_FILE)
            }
            R.id.load_from_file -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/x-python"
                }

                startActivityForResult(intent, REQUEST_LOAD_FILE)
            }
            R.id.ui_mode -> {
                val b = Bundle()
                b.putString(FirebaseAnalytics.Param.ITEM_NAME, "dark mode dialog")
                FirebaseAnalytics.getInstance(this).logEvent(SELECT_CONTENT, b)

                AlertDialog.Builder(this)
                    .setTitle(R.string.dark_mode)
                    .setSingleChoiceItems(arrayOf(getString(R.string.auto), getString(R.string.on), getString(R.string.off)),
                        Integer.valueOf(
                            Utils.readSharedSetting(this, getString(R.string.dark_mode)) ?: "0"
                        )) { dialog, which ->
                        dialog.dismiss()
                        Utils.saveSharedSetting(this, getString(R.string.dark_mode), which.toString())

                        val b1 = Bundle()
                        b1.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "dark mode")
                        b1.putString(FirebaseAnalytics.Param.ITEM_NAME, which.toString())
                        FirebaseAnalytics.getInstance(this).logEvent(SELECT_CONTENT, b1)

                        setUiMode()
                    }
                    .show()
            }
            R.id.about -> {
                val b = Bundle()
                b.putString(FirebaseAnalytics.Param.ITEM_NAME, "about")
                FirebaseAnalytics.getInstance(this).logEvent(SELECT_CONTENT, b)

                AlertDialog.Builder(this)
                    .setTitle(R.string.about)
                    .setMessage(SpannableString(getString(R.string.about_text, BuildConfig.VERSION_NAME))
                        .apply { LinkifyCompat.addLinks(this, WEB_URLS) })
                    .setNeutralButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
                    .show()
                    .apply {
                        findViewById<TextView>(android.R.id.message)?.movementMethod = LinkMovementMethod.getInstance()
                    }
            }
            else -> {
                EXAMPLES[item.itemId]?.let {
                    val b = Bundle()
                    b.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "sample")
                    b.putString(FirebaseAnalytics.Param.ITEM_NAME, it)
                    FirebaseAnalytics.getInstance(this).logEvent(SELECT_CONTENT, b)

                    CoroutineScope(Dispatchers.Main).launch {
                        withContext(Dispatchers.IO) {
                            setText(resources.assets.open("examples/$it"))
                        }
                    }
                }

                Utils.addToShowcase(this@ActivityMain, toolbar, R.id.ok, Utils.Companion.ShowcaseTarget.RUN_CODE) {
                    runCode()
                }
            }
        }

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SAVE_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                val b = Bundle()
                b.putString(FirebaseAnalytics.Param.ITEM_NAME, "file saved")
                FirebaseAnalytics.getInstance(this).logEvent(SELECT_CONTENT, b)

                data?.let {
                    it.data?.let {
                        CoroutineScope(Dispatchers.Main).launch {
                            withContext(Dispatchers.IO) {
                                contentResolver.openOutputStream(it)?.use { stream ->
                                    stream.write(
                                        StringEscapeUtils.unescapeJava(
                                            mViewModel.getText().value ?: ""
                                        )
                                            .toByteArray()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else if (requestCode == REQUEST_LOAD_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                val b = Bundle()
                b.putString(FirebaseAnalytics.Param.ITEM_NAME, "file opened")
                FirebaseAnalytics.getInstance(this).logEvent(SELECT_CONTENT, b)

                data?.let {
                    it.data?.let {
                        CoroutineScope(Dispatchers.Main).launch {
                            var text: InputStream? = null
                            withContext(Dispatchers.IO) {
                                text = contentResolver.openInputStream(it)
                            }
                            setText(text)
                        }
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private suspend fun setText(inputStream: InputStream?) {
        withContext(Dispatchers.Main) {
            mEditFragment.setText(
                withContext(Dispatchers.IO) {
                    inputStream.use { stream ->
                        stream?.bufferedReader()
                            .use { bufferedReader ->
                                StringEscapeUtils.escapeJava(bufferedReader?.readText())
                            }
                    }
                }
            )
        }
    }

    fun fragmentZoomInTransition(fragment: Fragment, encodedObject: EncodedObject, view: View) {
        val fragmentManager =
            if (fragment is FragmentHeap) fragment.childFragmentManager
            else fragment.parentFragmentManager

        mViewModel.heapRoot.value?.find { ref -> ref == encodedObject }?.let {
            fragmentManager.apply {
                beginTransaction().apply {
                    fragments.forEach { f -> remove(f) }
                    commitNow()
                }
            }
            mViewModel.goToHeapAt(it.id)
            return
        }

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
            if (encodedObject is EncodedObject.Ref) {
                val fragmentHeapZoom = FragmentHeapZoom.newInstance(
                    mViewModel.heap.value?.get(encodedObject.id)
                        ?: error("Encoded Object at index ${encodedObject.id} not found")
                )

                fragmentManager.beginTransaction()
                    .setTransition(TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.heap_root, fragmentHeapZoom)
                    .addToBackStack(HEAP_ZOOM_FRAGMENT.format(encodedObject.id))
                    .commit()
            }
        }, 200)
    }


    fun goToHeapAt(ref: Int) {
        mViewModel.goToHeapAt(ref)
    }

    fun lockDrawer() {
        drawer_layout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    fun unlockDrawer() {
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }
}
