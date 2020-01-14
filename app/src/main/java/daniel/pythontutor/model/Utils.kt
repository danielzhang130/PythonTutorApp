package daniel.pythontutor.model

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager


class Utils {
    companion object {
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
                toString(any)
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

        private fun toString(double: Double) = if (double.compareTo(double.toInt()) == 0) {
            double.toInt().toString()
        } else {
            double.toString()
        }

        private fun toString(string: String) = "\"%s\"".format(string)

        private fun toString(none: PythonVisualization.EncodedObject.None) = "None"

        private fun toString(specialFloat: PythonVisualization.EncodedObject.SpecialFloat) =
            specialFloat.value
    }
}