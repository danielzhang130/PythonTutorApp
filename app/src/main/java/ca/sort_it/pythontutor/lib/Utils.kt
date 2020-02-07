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

package ca.sort_it.pythontutor.lib

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.Toolbar
import ca.sort_it.pythontutor.model.PythonVisualization
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import java.util.*
import kotlin.math.roundToInt


class Utils {
    companion object {
        private val showcaseQueue: Queue<TapTarget> = LinkedList()
        private val showcaseCallback: Queue<(() -> Unit)> = LinkedList()

        fun toString(any: Any) = when (any) {
            is Double ->
                toString(any)
            is String ->
                toString(any)
            is List<*> ->
                if (any.size == 2 && any[0] == "SPECIAL_FLOAT") {
                    any[1].toString()
                } else {
                    any.toString()
                }
            true ->
                "True"
            false ->
                "False"
            is PythonVisualization.EncodedObject.None ->
                "None"
            is PythonVisualization.EncodedObject.SpecialFloat ->
                toString(any)
            else ->
                any.toString()
        }

        fun isPrimitive(any: Any?) = (
                any is PythonVisualization.EncodedObject.None ||
                any is PythonVisualization.EncodedObject.SpecialFloat ||
                (any !is PythonVisualization.EncodedObject && any !is List<*>) ||
                (any is List<*> && any.size == 2 && any[0] == "SPECIAL_FLOAT")
                )

        fun hideKeyboard(activity: Activity) {
            val imm: InputMethodManager =
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view: View? = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun addToShowcase(activity: Activity, toolbar: Toolbar, target: ShowcaseTarget, callback: () -> Unit) {
                internalAddToShowcase(
                    activity,
                    TapTarget.forToolbarNavigationIcon(toolbar, SHOWCASE_TEXT[target]?.first, SHOWCASE_TEXT[target]?.second),
                    target,
                    callback
                )
        }

        fun addToShowcase(activity: Activity, view: View, target: ShowcaseTarget, callback: () -> Unit) {
            val dp: Int
            try {
                dp = convertPixelsToDp(view.width/2, activity)
            } catch (e: ArithmeticException) {
                return
            }
            internalAddToShowcase(
                activity,
                TapTarget.forView(view, SHOWCASE_TEXT[target]?.first, SHOWCASE_TEXT[target]?.second)
                    .targetRadius((dp * 0.75).roundToInt()),
                target,
                callback
            )
        }

        fun addToShowcase(activity: Activity, toolbar: Toolbar, id: Int, target: ShowcaseTarget, callback: () -> Unit) {
            internalAddToShowcase(activity,
                TapTarget.forToolbarMenuItem(toolbar, id, SHOWCASE_TEXT[target]?.first, SHOWCASE_TEXT[target]?.second),
                target,
                callback)
        }

//        fun convertDpToPixel(dp: Int, context: Context) =
//            dp * (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)

        fun convertPixelsToDp(px: Int, context: Context) =
            px / (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)

        private fun internalAddToShowcase(activity: Activity, tapTarget: TapTarget, target: ShowcaseTarget, callback: () -> Unit) {
            if (readSharedSetting(activity, target.name, "false")?.toBoolean() == false) {
                tapTarget.drawShadow(true)
                    .tintTarget(true)

                showcaseCallback.add(callback)

                val listener = object : TapTargetView.Listener() {
                    private fun next() {
                        if (showcaseQueue.isEmpty() || showcaseCallback.isEmpty()) {
                            return
                        }

                        showcaseCallback.remove()
                        showcaseQueue.remove()
                        showcaseQueue.peek()?.let {
                            TapTargetView.showFor(activity, it, this)
                        }
                    }

                    override fun onTargetClick(view: TapTargetView?) {
                        super.onTargetClick(view)
                        saveSharedSetting(activity, target.name, "true")
                        showcaseCallback.peek()?.invoke()
                        next()
                    }

                    override fun onTargetCancel(view: TapTargetView?) {
                        super.onTargetCancel(view)
                        saveSharedSetting(activity, target.name, "true")
                        next()
                    }
                }

                if (showcaseQueue.isEmpty()) {
                    showcaseQueue.add(tapTarget)
                    TapTargetView.showFor(activity, tapTarget, listener)
                } else {
                    showcaseQueue.add(tapTarget)
                }
            }
        }


        fun readSharedSetting(ctx: Context, settingName: String) = readSharedSetting(ctx, settingName, null)

        private fun readSharedSetting(ctx: Context, settingName: String, defaultValue: String?): String? {
            val sharedPref =
                ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)
            return sharedPref.getString(settingName, defaultValue)
        }

        fun saveSharedSetting(ctx: Context, settingName: String, settingValue: String) {
            val sharedPref =
                ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString(settingName, settingValue)
            editor.apply()
        }

        private fun toString(double: Double) = if (double.compareTo(double.toInt()) == 0) {
            double.toInt().toString()
        } else {
            double.toString()
        }

        private fun toString(string: String) = "\"%s\"".format(string)

        private fun toString(specialFloat: PythonVisualization.EncodedObject.SpecialFloat) =
            specialFloat.value

        enum class ShowcaseTarget {
            DRAWER_TOGGLE,
            DRAWER_EXAMPLE,
            RUN_CODE
        }

        private val SHOWCASE_TEXT = mapOf(
            ShowcaseTarget.DRAWER_TOGGLE to ("Tap to show navigation menu" to "See code samples and more options"),
            ShowcaseTarget.DRAWER_EXAMPLE to ("Tap on a code sample" to "Load it to the editor"),
            ShowcaseTarget.RUN_CODE to ("Tap run to visualize your code" to null)
        )
    }
}