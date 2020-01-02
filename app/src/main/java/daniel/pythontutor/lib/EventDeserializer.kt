package daniel.pythontutor.lib

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import daniel.pythontutor.model.PythonVisualization
import java.lang.reflect.Type
import java.util.*

class EventDeserializer : JsonDeserializer<PythonVisualization.Event> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?)
            : PythonVisualization.Event =
        PythonVisualization.Event::class.nestedClasses.first {
            it.simpleName?.toLowerCase(Locale.getDefault()) == json?.asString
        }.objectInstance as PythonVisualization.Event

}