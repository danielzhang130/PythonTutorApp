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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ca.sort_it.pythontutor.R

class CodeAdapter(
            private val mInflater: LayoutInflater,
            private val mHighlight: Int,
            private val mSecondary: Int,
            private val mNormal: Int
        ) : RecyclerView.Adapter<CodeAdapter.CodeLineViewHolder>() {

    private var mLines: List<String> = ArrayList()
    private var mCurrentLine = -1
    private var mPrevLine = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CodeLineViewHolder(mInflater.inflate(R.layout.code_line, parent, false))

    override fun getItemCount() = mLines.size

    override fun onBindViewHolder(holder: CodeLineViewHolder, position: Int) {
        holder.code.text = mLines[position]
        if (position == mCurrentLine) {
            holder.code.setBackgroundColor(mHighlight)
        }
        else if (position == mPrevLine) {
            holder.code.setBackgroundColor(mSecondary)
        }
        else {
            holder.code.setBackgroundColor(mNormal)
        }
    }

    fun setCode(code: List<String>) {
        mLines = code
        notifyDataSetChanged()
    }

    fun setCurrentLine(position: Int) {
        val prev = mCurrentLine
        mCurrentLine = position
        if (prev >= 0) {
            notifyItemChanged(prev)
        }
        if (mCurrentLine >= 0) {
            notifyItemChanged(mCurrentLine)
        }
    }

    fun setPrevLine(position: Int) {
        val prev = mPrevLine
        mPrevLine = position
        if (prev >= 0) {
            notifyItemChanged(prev)
        }
        if (mPrevLine >= 0) {
            notifyItemChanged(mPrevLine)
        }
    }

    class CodeLineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var code: TextView = itemView.findViewById(R.id.code)
    }
}
