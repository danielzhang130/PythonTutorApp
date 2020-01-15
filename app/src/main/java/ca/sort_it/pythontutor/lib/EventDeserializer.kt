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

import ca.sort_it.pythontutor.model.PythonVisualization
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.util.*

class EventDeserializer : JsonDeserializer<PythonVisualization.Event> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?)
            : PythonVisualization.Event =
        PythonVisualization.Event::class.nestedClasses.first {
            it.simpleName?.toLowerCase(Locale.getDefault()) == json?.asString
        }.objectInstance as PythonVisualization.Event

}