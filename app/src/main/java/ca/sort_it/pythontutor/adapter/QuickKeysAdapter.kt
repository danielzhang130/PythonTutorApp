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

package ca.sort_it.pythontutor.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ca.sort_it.pythontutor.R
import ca.sort_it.pythontutor.ui.ActivityMain
import com.github.danielzhang130.aceeditor.AceEditor
import com.google.android.material.button.MaterialButton

class QuickKeysAdapter(private val mActivity: ActivityMain, private val mAceEditor: AceEditor) :
    RecyclerView.Adapter<QuickKeysAdapter.QuickKeysVH>() {

    private var mChars = ArrayList<Char>()
    private val mInflater = mActivity.layoutInflater

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuickKeysVH {
        val view : MaterialButton = mInflater.inflate(R.layout.quick_key, parent, false) as MaterialButton
        view.setOnClickListener {
            mAceEditor.insertTextAtCursor(view.text.toString())
            mActivity.setKeyTyped(view.text[0])
        }
        return QuickKeysVH(view)
    }

    override fun onBindViewHolder(holder: QuickKeysVH, position: Int) {
        (holder.itemView as MaterialButton).text = mChars[position].toString()
    }

    override fun getItemCount() = mChars.size

    fun setKeys(list: List<Char>) {
        val diffResult = DiffUtil.calculateDiff(CharDiffCallBack(mChars, list), true)
        mChars.clear()
        mChars.addAll(list)
        diffResult.dispatchUpdatesTo(this)
    }

    class CharDiffCallBack(private val mOldList: List<Char>, private val mNewList: List<Char>) :
        DiffUtil.Callback() {
        override fun getOldListSize() = mOldList.size
        override fun getNewListSize() = mNewList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = mOldList[oldItemPosition] == mNewList[newItemPosition]
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = true
    }

    class QuickKeysVH(itemView: View) : RecyclerView.ViewHolder(itemView)
}
