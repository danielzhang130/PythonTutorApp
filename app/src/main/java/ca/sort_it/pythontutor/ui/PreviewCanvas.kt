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

package ca.sort_it.pythontutor.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class PreviewCanvas @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    private var mTarget: View = View(context)
    private var mX = 0F
    private var mY = 0F

    fun translate(dx: Float, dy: Float) {
        mX += dx
        mY += dy
        invalidate()
    }

    fun setPreviewTarget(target: View) {
        mTarget = target
        val p = Point()
        val rect = Rect()
        mTarget.getGlobalVisibleRect(rect, p)
        mX = p.x.toFloat()
        mY = p.y.toFloat()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.translate(mX, mY)
        mTarget.draw(canvas)
        canvas.restore()
    }
}