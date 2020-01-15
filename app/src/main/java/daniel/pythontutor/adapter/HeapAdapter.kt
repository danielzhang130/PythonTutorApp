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

package daniel.pythontutor.adapter

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import daniel.pythontutor.R
import daniel.pythontutor.model.PythonVisualization.EncodedObject

class HeapAdapter(private val fragment: Fragment) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_LIST = 0
        private const val TYPE_TEXT = 1
        private const val TYPE_INSTANCE_PPRINT = 2
        private const val TYPE_CLASS = 3
        private const val TYPE_FUNCTION = 4
    }

    private var mHeap = emptyMap<Int, EncodedObject>()
    private var mRoot = emptyList<EncodedObject.Ref>()

    private val activity = fragment.requireActivity()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            TYPE_LIST ->
                ListViewHolder(
                    activity.layoutInflater.inflate(
                        R.layout.heap_list,
                        parent,
                        false
                    ).also {
                        it.clipToOutline = false
                    })
            TYPE_TEXT ->
                TextViewHolder(activity.layoutInflater.inflate(R.layout.heap_text, parent, false))
            TYPE_INSTANCE_PPRINT ->
                InstancePPrintViewHolder(activity.layoutInflater.inflate(
                    R.layout.heap_instance_pprint,
                    parent,
                    false
                )
                    .apply {
                        clipToOutline = false
                    })
            TYPE_CLASS ->
                ClassViewHolder(activity.layoutInflater.inflate(R.layout.heap_class, parent, false)
                    .apply {
                        clipToOutline = false
                    })
                    .apply {
                        superClasses.layoutManager = LinearLayoutManager(activity)
                        attrs.layoutManager = LinearLayoutManager(activity)
                    }
            TYPE_FUNCTION ->
                FunctionViewHolder(activity.layoutInflater.inflate(
                    R.layout.heap_function,
                    parent,
                    false
                )
                    .apply {
                        clipToOutline = false
                    })
            else ->
                error("Invalid view type")
        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val encodedObject = getItem(position)
        when (holder) {
            is ListViewHolder -> {
                when (encodedObject) {
                    is EncodedObject.PyDict -> {
                        holder.label.text = activity.getString(R.string.map)
                        holder.recyclerView.adapter =
                            PyMapAdapter(fragment).apply { submitList(encodedObject.elements) }
                    }
                    is EncodedObject.Instance -> {
                        holder.label.text = activity.getString(R.string.instance_formatted).format(encodedObject.name)
                        holder.recyclerView.adapter =
                            PyMapAdapter(fragment).apply { submitList(encodedObject.attrs) }
                    }
                    else -> {
                        holder.recyclerView.adapter = PyListAdapter(fragment)
                    }
                }

                when (encodedObject) {
                    is EncodedObject.PyList -> {
                        holder.label.text = activity.getString(R.string.list)
                        (holder.recyclerView.adapter as PyListAdapter).submitList(encodedObject.elements)
                    }
                    is EncodedObject.PySet -> {
                        holder.label.text = activity.getString(R.string.set)
                        (holder.recyclerView.adapter as PyListAdapter).submitList(encodedObject.elements.toList())
                    }
                    is EncodedObject.PyTuple -> {
                        holder.label.text = activity.getString(R.string.tuple)
                        (holder.recyclerView.adapter as PyListAdapter).submitList(encodedObject.elements)
                    }
                }
            }
            is TextViewHolder -> {
                when (encodedObject) {
                    is EncodedObject.PyModule -> {
                        holder.name.text = activity.getString(R.string.module)
                        holder.value.text = encodedObject.name
                    }
                    is EncodedObject.Other -> {
                        holder.name.text = encodedObject.name
                        holder.value.text = encodedObject.value
                    }
                    is EncodedObject.HeapPrimitive -> {
                        holder.name.text = encodedObject.name
                        holder.value.text = encodedObject.value.toString()
                    }
                    is EncodedObject.ImportedFauxPrimitive -> {
                        holder.name.text = activity.getString(R.string.imported_faux_primitive)
                        holder.value.text = encodedObject.label
                    }
                }
            }
            is InstancePPrintViewHolder -> {
                if (encodedObject is EncodedObject.InstancePPrint) {
                    holder.type.text = activity.getString(R.string.instance_formatted).format(encodedObject.name)
                    holder.string.text = activity.getString(R.string.string_rep_formatted).format(encodedObject.string)
                    holder.recyclerView.adapter =
                        PyMapAdapter(fragment).apply { submitList(encodedObject.attrs) }
                }
            }
            is ClassViewHolder -> {
                if (encodedObject is EncodedObject.PyClass) {
                    holder.type.text =
                        activity.getString(R.string.class_formatted).format(encodedObject.name)

                    if (encodedObject.supers.isEmpty()) {
                        holder.superClassesGroup.visibility = GONE
                    } else {
                        holder.superClassesGroup.visibility = VISIBLE
                        holder.superClasses.adapter = ArrayAdapter(activity.layoutInflater).apply {
                            encodedObject.supers
                        }
                    }

                    if (encodedObject.attrs.isEmpty()) {
                        holder.attrsGroup.visibility = GONE
                    } else {
                        holder.attrsGroup.visibility = VISIBLE
                        holder.attrs.adapter =
                            PyMapAdapter(fragment).apply { submitList(encodedObject.attrs) }
                    }
                }
            }
            is FunctionViewHolder -> {
                if (encodedObject is EncodedObject.PyFunction) {
                    holder.header.text =
                        activity.getString(R.string.function).format(encodedObject.name)
//                    if (encodedObject.parent_frame_id == null) {
//                        holder.parentGroup.visibility = GONE
//                    } else {
//                        holder.parentGroup.visibility = VISIBLE
//                        holder.parent.text =
//                    }
                    if (encodedObject.defaults.isEmpty()) {
                        holder.defaultsGroup.visibility = GONE
                    } else {
                        holder.defaultsGroup.visibility = VISIBLE
                        holder.defaults.adapter =
                            PyMapAdapter(fragment).apply { submitList(encodedObject.defaults) }
                    }
                }
            }
        }
    }

    override fun getItemCount() = mRoot.size

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is EncodedObject.PyModule,
        is EncodedObject.Other,
        is EncodedObject.HeapPrimitive,
        is EncodedObject.ImportedFauxPrimitive
        -> TYPE_TEXT

        is EncodedObject.PyTuple,
        is EncodedObject.PyList,
        is EncodedObject.PySet,
        is EncodedObject.PyDict,
        is EncodedObject.Instance
        -> TYPE_LIST

        is EncodedObject.InstancePPrint
        -> TYPE_INSTANCE_PPRINT

        is EncodedObject.PyClass
        -> TYPE_CLASS

        is EncodedObject.PyFunction
        -> TYPE_FUNCTION

        is EncodedObject.Ref,
        is EncodedObject.None,
        is EncodedObject.SpecialFloat,
        null
        -> error("Unexpected Heap data type")
    }

    private fun getItem(position: Int) = mHeap[mRoot[position].id]

    fun setRoot(root: List<EncodedObject.Ref>) {
        mRoot = root
        notifyDataSetChanged()
    }

    fun setHeap(heap: Map<Int, EncodedObject>) {
        mHeap = heap
        notifyDataSetChanged()
    }

    fun findRef(ref: Int) = mRoot.indexOfFirst {
        it.id == ref
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val label: TextView = itemView.findViewById(R.id.type)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.elements)

        init {
            recyclerView.layoutManager = StaggeredGridLayoutManager(4, RecyclerView.VERTICAL)
        }
    }

    class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val value: TextView = itemView.findViewById(R.id.value)
    }

    class InstancePPrintViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val type: TextView = itemView.findViewById(R.id.type)
        val string: TextView = itemView.findViewById(R.id.string)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.elements)

        init {
            recyclerView.layoutManager = StaggeredGridLayoutManager(4, RecyclerView.VERTICAL)
        }
    }

    class ClassViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val type: TextView = itemView.findViewById(R.id.type)
        val superClassesGroup: LinearLayout = itemView.findViewById(R.id.super_classes_group)
        val superClasses: RecyclerView = itemView.findViewById(R.id.super_classes)
        val attrsGroup: LinearLayout = itemView.findViewById(R.id.attrs_group)
        val attrs: RecyclerView = itemView.findViewById(R.id.elements)
    }

    class FunctionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val header: TextView = itemView.findViewById(R.id.type)
        val parentGroup: LinearLayout = itemView.findViewById(R.id.parent_group)
        val parent: TextView = itemView.findViewById(R.id.parent)
        val defaultsGroup: LinearLayout = itemView.findViewById(R.id.defaults_group)
        val defaults: RecyclerView = itemView.findViewById(R.id.elements)

        init {
            defaults.layoutManager = StaggeredGridLayoutManager(4, RecyclerView.VERTICAL)
        }
    }
}
