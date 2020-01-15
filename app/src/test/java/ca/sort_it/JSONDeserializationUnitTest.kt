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

package ca.sort_it

import ca.sort_it.pythontutor.lib.EncodedObjectDeserializer
import ca.sort_it.pythontutor.lib.EventDeserializer
import ca.sort_it.pythontutor.model.PythonVisualization
import ca.sort_it.pythontutor.model.PythonVisualization.EncodedObject
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@Suppress("TestFunctionName")
class JSONDeserializationUnitTest {
    private lateinit var gson: Gson

    @Before
    fun init() {
        val builder = GsonBuilder()
        builder.registerTypeAdapter(PythonVisualization.Event::class.java, EventDeserializer())
            .registerTypeHierarchyAdapter(EncodedObject::class.java, EncodedObjectDeserializer())
        gson = builder.create()
    }

    @Test
    fun PyList() {
        // l = [[1, False], "string", None, True, datetime.datetime.now(), 1, 0.1]
        val json = "[\"LIST\",[\"REF\",3],\"string\",null,true,[\"REF\",4], 1, 0.1]"
        val encodedObject = gson.fromJson<EncodedObject>(json, EncodedObject::class.java)
        assertTrue(encodedObject is EncodedObject.PyList)
        assertEquals(listOf(
            EncodedObject.Ref(3),
            "string",
            EncodedObject.None,
            true,
            EncodedObject.Ref(4),
            1L,
            0.1
        ), (encodedObject as EncodedObject.PyList).elements)
    }

    @Test
    fun PyTuple() {
        // t = ((1, False), "string", None, True, datetime.datetime.now())
        val json = "[\"TUPLE\", [\"REF\", 3], \"string\", null, true, [\"REF\", 4]]"
        val encodedObject = gson.fromJson<EncodedObject>(json, EncodedObject::class.java)
        assertTrue(encodedObject is EncodedObject.PyTuple)
        assertEquals(listOf(
            EncodedObject.Ref(3),
            "string",
            EncodedObject.None,
            true,
            EncodedObject.Ref(4)
        ), (encodedObject as EncodedObject.PyTuple).elements)
    }

    @Test
    fun PySet() {
        // s = set(((1, False), "string", None, True, datetime.datetime.now()))
        val json = "[\"SET\", true, null, \"string\", [\"REF\", 3], [\"REF\", 4]]"
        val encodedObject = gson.fromJson<EncodedObject>(json, EncodedObject::class.java)
        assertTrue(encodedObject is EncodedObject.PySet)
        assertEquals(setOf(
            EncodedObject.Ref(3),
            "string",
            EncodedObject.None,
            true,
            EncodedObject.Ref(4)
        ), (encodedObject as EncodedObject.PySet).elements)
    }

    @Test
    fun PyDict() {
        // m = {
        //        None: "stuff",
        //        "Stuff": None,
        //        1: False,
        //        False: [],
        //        (): 0,
        //        tuple("t"): {},
        //        datetime.date(2019, 12, 4): math
        //    }
        val json = "[\"DICT\",[null,\"stuff\"],[\"Stuff\",null],[1,false],[false,[\"REF\",4]],[[\"REF\",5],0],[[\"REF\",6],[\"REF\",7]],[[\"REF\",8],[\"REF\",2]]]"
        val encodedObject = gson.fromJson<EncodedObject>(json, EncodedObject::class.java)
        assertTrue(encodedObject is EncodedObject.PyDict)
        assertEquals(listOf(
            EncodedObject.None to "stuff",
            "Stuff" to EncodedObject.None,
            1L to false,
            false to EncodedObject.Ref(4),
            EncodedObject.Ref(5) to 0L,
            EncodedObject.Ref(6) to EncodedObject.Ref(7),
            EncodedObject.Ref(8) to EncodedObject.Ref(2)
        ), (encodedObject as EncodedObject.PyDict).elements)
    }

    @Test
    fun PyDictNone1() {
        // m = {None:None}
        val json = "[\"DICT\", [null, null]]"
        val encodedObject = gson.fromJson<EncodedObject>(json, EncodedObject::class.java)
        assertTrue(encodedObject is EncodedObject.PyDict)
        assertEquals(listOf(
            EncodedObject.None to EncodedObject.None
        ), (encodedObject as EncodedObject.PyDict).elements)
    }

    @Test
    fun PyDictNone2() {
        // m = {None:math, math:None}
        val json = "[\"DICT\",[null,[\"REF\",1]],[[\"REF\",1],null]]"
        val encodedObject = gson.fromJson<EncodedObject>(json, EncodedObject::class.java)
        assertTrue(encodedObject is EncodedObject.PyDict)
        assertEquals(listOf(
            EncodedObject.None to EncodedObject.Ref(1),
            EncodedObject.Ref(1) to EncodedObject.None
        ), (encodedObject as EncodedObject.PyDict).elements)
    }

    @Test
    fun Instance() {
        // class person:
        //     def __init__(self, first, last):
        //	       self.first = first
        //	       self.last = last
        //	       self.initialized = True
        //
        // p = person("a", "b")
        val json = "[\"INSTANCE\",\"person\",[\"first\",\"a\"],[\"initialized\",true],[\"last\",\"b\"],[\"next\",[\"REF\",4]]]"
        val encodedObject = gson.fromJson<EncodedObject>(json, EncodedObject::class.java)
        assertTrue(encodedObject is EncodedObject.Instance)
        assertEquals("person", (encodedObject as EncodedObject.Instance).name)
        assertEquals(listOf(
            "first" to "a",
            "initialized" to true,
            "last" to "b",
            "next" to EncodedObject.Ref(4)
        ), encodedObject.attrs)
    }

    @Test
    fun InstancePPrint() {
        // class person:
        //     def __init__(self, first, last):
        //         self.first = first
        //         self.last = last
        //         self.initialized = True
        //
        //     def __str__(self):
        //         return self.first + self.last
        //
        // p = person("a", "b")
        val json = "[\"INSTANCE_PPRINT\",\"person\",\"ab\",[\"first\",\"a\"],[\"initialized\",true],[\"last\",\"b\"]]"
        val encodedObject = gson.fromJson<EncodedObject>(json, EncodedObject::class.java)
        assertTrue(encodedObject is EncodedObject.InstancePPrint)
        assertEquals("person", (encodedObject as EncodedObject.InstancePPrint).name)
        assertEquals("ab", encodedObject.string)
        assertEquals(listOf(
            "first" to "a",
            "initialized" to true,
            "last" to "b"
        ), encodedObject.attrs)
    }

    @Test
    fun PyClass() {
        // class person:
        //     def __init__(self, first, last):
        //         self.first = first
        //         self.last = last
        //         self.initialized = True
        //
        //     def __str__(self):
        //         return self.first + self.last
        //
        val json = "[\"CLASS\",\"person\",[],[\"__init__\",[\"REF\",2]],[\"__str__\",[\"REF\",3]]]"
        val encodedObject = gson.fromJson<EncodedObject>(json, EncodedObject::class.java)
        assertTrue(encodedObject is EncodedObject.PyClass)
        assertEquals("person", (encodedObject as EncodedObject.PyClass).name)
        assertEquals(emptyList<String>(), encodedObject.supers)
        assertEquals(listOf(
            "__init__" to EncodedObject.Ref(2),
            "__str__" to EncodedObject.Ref(3)
        ), encodedObject.attrs)
    }

    @Test
    fun childPyClass() {
        // class Object:
        //     def __init__(self):
        //         self.t = "object"
        //
        // class person:
        //     def __init__(self):
        //         self.t = "person"
        //
        // class kid(Object, person):
        //     def __init__(self, first, last):
        //         self.t = "kid"
        //         self.first = first
        //         self.last = last
        //         self.initialized = True
        val json = "[\"CLASS\",\"kid\",[\"Object\",\"person\"],[\"__init__\",[\"REF\",6]],[\"__str__\",[\"REF\",7]]]"
        val encodedObject = gson.fromJson<EncodedObject>(json, EncodedObject::class.java)
        assertTrue(encodedObject is EncodedObject.PyClass)
        assertEquals("kid", (encodedObject as EncodedObject.PyClass).name)
        assertEquals(listOf(
            "Object",
            "person"
        ), encodedObject.supers)
        assertEquals(listOf(
            "__init__" to EncodedObject.Ref(6),
            "__str__" to EncodedObject.Ref(7)
        ), encodedObject.attrs)
    }

    @Test
    fun emptyPyClass() {
        // class empty:
        //     pass
        val json = "[\"CLASS\",\"empty\",[]]"
        val encodedObject = gson.fromJson<EncodedObject>(json, EncodedObject::class.java)
        assertTrue(encodedObject is EncodedObject.PyClass)
        assertEquals("empty", (encodedObject as EncodedObject.PyClass).name)
        assertEquals(emptyList<String>(), encodedObject.supers)
        assertEquals(emptyList<Pair<String, Any>>(), encodedObject.attrs)
    }

    @Test
    fun PyFunction() {
        // def fun():
        //     pass
        val json = "[\"FUNCTION\",\"fun()\",null]"
        val encodedObject = gson.fromJson<EncodedObject>(json, EncodedObject::class.java)
        assertTrue(encodedObject is EncodedObject.PyFunction)
        assertEquals("fun()", (encodedObject as EncodedObject.PyFunction).name)
        assertEquals(null, encodedObject.parent_frame_id)
        assertEquals(emptyList<Pair<String, Any>>(), encodedObject.defaults)
    }

    @Test
    fun nestedPyFunction() {
        // def fun1():
        //     def fun2()
        val json = "[\"FUNCTION\",\"fun3(x, y)\",1,[[\"x\",1],[\"y\",2]]]"
        val encodedObject = gson.fromJson<EncodedObject>(json, EncodedObject::class.java)
        assertTrue(encodedObject is EncodedObject.PyFunction)
        assertEquals("fun3(x, y)", (encodedObject as EncodedObject.PyFunction).name)
        assertEquals(1L, encodedObject.parent_frame_id)
        assertEquals(listOf(
            "x" to 1L,
            "y" to 2L
        ), encodedObject.defaults)
    }
}
