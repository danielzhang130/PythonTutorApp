package daniel.pythontutor.model

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