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

package daniel.pythontutor.lib

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import daniel.pythontutor.model.PythonVisualization.EncodedObject
import java.lang.reflect.Type
import java.text.NumberFormat

class EncodedObjectDeserializer : JsonDeserializer<EncodedObject> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?)
            : EncodedObject {
        require(json?.isJsonArray!!)
        val array: JsonArray = json.asJsonArray
        require(array[0].isJsonPrimitive && array[0].asJsonPrimitive.isString)
        return when (array[0].asString) {
            "SPECIAL_FLOAT" ->
                EncodedObject.SpecialFloat(array[1].asString)
            "HEAP_PRIMITIVE" ->
                EncodedObject.HeapPrimitive(
                    array[1].asString,
                    if (array[2].isJsonPrimitive) getPrimitiveValue(array[2].asJsonPrimitive)
                    else deserialize(array[2], typeOfT, context)
                )
            "IMPORTED_FAUX_PRIMITIVE" ->
                EncodedObject.ImportedFauxPrimitive(array[1].asString)
            "LIST" ->
                EncodedObject.PyList(
                    array.drop(1).map {
                        when {
                            it.isJsonNull -> EncodedObject.None
                            it.isJsonPrimitive -> getPrimitiveValue(it.asJsonPrimitive)
                            else -> deserialize(it, EncodedObject::class.java, context)
                        }
                    }
                )
            "TUPLE" ->
                EncodedObject.PyTuple(
                    array.drop(1).map {
                        when {
                            it.isJsonNull -> EncodedObject.None
                            it.isJsonPrimitive -> getPrimitiveValue(it.asJsonPrimitive)
                            else -> deserialize(it, EncodedObject::class.java, context)
                        }
                    }
                )
            "SET" ->
                EncodedObject.PySet(
                    array.drop(1).map {
                        when {
                            it.isJsonNull -> EncodedObject.None
                            it.isJsonPrimitive -> getPrimitiveValue(it.asJsonPrimitive)
                            else -> deserialize(it, EncodedObject::class.java, context)
                        }
                    }.toSet()
                )
            "DICT" ->
                EncodedObject.PyDict(
                    array.drop(1).map {
                        if (it.asJsonArray[0].isJsonNull && !it.asJsonArray[1].isJsonNull)
                            if (it.asJsonArray[1].isJsonPrimitive)
                                EncodedObject.None to getPrimitiveValue(it.asJsonArray[1].asJsonPrimitive)
                            else
                                EncodedObject.None to deserialize(
                                    it.asJsonArray[1],
                                    EncodedObject::class.java,
                                    context
                                )
                        else if (!it.asJsonArray[0].isJsonNull && it.asJsonArray[1].isJsonNull)
                            if (it.asJsonArray[0].isJsonPrimitive)
                                getPrimitiveValue(it.asJsonArray[0].asJsonPrimitive) to EncodedObject.None
                            else
                                deserialize(
                                    it.asJsonArray[0],
                                    EncodedObject::class.java,
                                    context
                                ) to EncodedObject.None
                        else if (it.asJsonArray[0].isJsonNull && it.asJsonArray[1].isJsonNull)
                            EncodedObject.None to EncodedObject.None
                        else if (it.asJsonArray[0].isJsonPrimitive && it.asJsonArray[1].isJsonPrimitive)
                            getPrimitiveValue(it.asJsonArray[0].asJsonPrimitive) to getPrimitiveValue(
                                it.asJsonArray[1].asJsonPrimitive
                            )
                        else if (it.asJsonArray[0].isJsonPrimitive && !it.asJsonArray[1].isJsonPrimitive)
                            getPrimitiveValue(it.asJsonArray[0].asJsonPrimitive) to deserialize(
                                it.asJsonArray[1],
                                EncodedObject::class.java,
                                context
                            )
                        else if (!it.asJsonArray[0].isJsonPrimitive && it.asJsonArray[1].isJsonPrimitive)
                            deserialize(
                                it.asJsonArray[0],
                                EncodedObject::class.java,
                                context
                            ) to getPrimitiveValue(it.asJsonArray[1].asJsonPrimitive)
                        else
                            deserialize(
                                it.asJsonArray[0],
                                EncodedObject::class.java,
                                context
                            ) to deserialize(it.asJsonArray[1], EncodedObject::class.java, context)
                    }
                )
            "INSTANCE" ->
                EncodedObject.Instance(
                    array[1].asString,
                    array.drop(2).map {
                        getAttrValuePair(it, typeOfT, context)
                    }
                )
            "INSTANCE_PPRINT" ->
                EncodedObject.InstancePPrint(
                    array[1].asString,
                    array[2].asString,
                    array.drop(3).map {
                        getAttrValuePair(it, typeOfT, context)
                    }
                )
            "CLASS" ->
                EncodedObject.PyClass(
                    array[1].asString,
                    context?.deserialize(array[2], object : TypeToken<List<String>>(){}.type) ?: emptyList(),
                    array.drop(3)
                        .map{
                            getAttrValuePair(it, typeOfT, context)
                        }
                )
            "FUNCTION" -> {
                EncodedObject.PyFunction(
                    array[1].asString,
                    if (array[2].isJsonNull) {
                        null
                    } else {
                        array[2].asLong
                    },
                    if (array.size() == 4) {
                        val jsonElement = array[3]
                        require(jsonElement.isJsonArray)
                        jsonElement.asJsonArray.map {
                            getAttrValuePair(it, typeOfT, context)
                        }
                    } else {
                        emptyList()
                    }
                )
            }
            "module" -> {
                EncodedObject.PyModule(array[1].asString)
            }
            "REF" ->
                EncodedObject.Ref(array[1].asInt)
            else ->
                EncodedObject.Other(array[0].asString, array[1].asString)
        }
    }

    private fun getPrimitiveValue(primitive: JsonPrimitive) = if (primitive.isNumber) {
        NumberFormat.getInstance().parse(primitive.asString)!!
    } else {
        val f = JsonPrimitive::class.java.getDeclaredField("value")
        f.isAccessible = true
        f.get(primitive)!!
    }

    private fun getAttrValuePair(it: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?) : Pair<String, Any> {
        require(it.isJsonArray && it.asJsonArray.size() == 2)
        val pair = it.asJsonArray
        return when {
            pair[1].isJsonNull -> pair[0].asString to EncodedObject.None
            pair[1].isJsonPrimitive -> pair[0].asString to getPrimitiveValue(pair[1].asJsonPrimitive)
            else -> pair[0].asString to deserialize(pair[1], typeOfT, context)
        }
    }
}