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

package ca.sort_it.pythontutor.model

import java.io.Serializable
import java.util.*

data class PythonVisualization(val code: String,
                               val trace: List<Trace>) {

    data class Trace(val event: Event,
                     val fun_name: String,
                     val globals: Map<String, Any>,
                     val heap: Map<Int, EncodedObject>,
                     val line: Int,
                     val ordered_globals: List<String>,
                     val stack_to_render: List<Stack>,
                     val stdout: String,
                     val offset: Int,
                     val exception_msg: String)

    sealed class Event {
        object Call : Event()
        object Step_Line : Event()
        object Return : Event()
        object Exception : Event()
        object Uncaught_Exception : Event()

        override fun toString(): String {
            return "Event: " + this.javaClass.simpleName.toLowerCase(Locale.getDefault())
        }
    }

    data class Stack(val func_name: String,
                     val is_parent: Boolean,
                     val frame_id: Int,
                     val parent_frame_id_list: List<Any>,
                     val encoded_locals: Map<String, Any>,
                     val ordered_varnames: List<String>,
                     val is_zombie: Boolean,
                     val is_highlighted: Boolean,
                     val unique_hash: String)

    /**
     * https://github.com/pgbovine/OnlinePythonTutor/blob/4865ec6714556f510ace77aa5c1c488fad37dca9/v5-unity/pg_encoder.py
     */
    sealed class EncodedObject: Serializable {
        object None : EncodedObject()
        data class SpecialFloat(val value: String) : EncodedObject()
        data class HeapPrimitive(val name: String, val value: Any) : EncodedObject()
        data class ImportedFauxPrimitive(val label: String) : EncodedObject()
        data class PyList(val elements: List<Any>) : EncodedObject()
        data class PyTuple(val elements: List<Any>) : EncodedObject()
        data class PySet(val elements: Set<Any>) : EncodedObject()
        data class PyDict(val elements: List<Pair<Any, Any>>) : EncodedObject()
        data class Instance(val name: String, val attrs: List<Pair<String, Any>>) : EncodedObject()
        data class InstancePPrint(val name: String, val string: String, val attrs: List<Pair<String, Any>>) : EncodedObject()
        data class PyClass(val name: String, val supers: List<String>, val attrs: List<Pair<String, Any>>) : EncodedObject()
        data class PyFunction(val name: String, val parent_frame_id: Long?, val defaults: List<Pair<String, Any>>) : EncodedObject()
        data class PyModule(val name: String) : EncodedObject()
        data class Other(val name: String, val value: String) : EncodedObject()
        data class Ref(val id: Int) : EncodedObject()
    }
}