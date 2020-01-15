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

package daniel.pythontutor.model

data class OrderedMap<K, V> (val data: Map<K, V>, val orderedKeys: List<K>) {
    val size = data.size
    fun get(position: Int) = data[orderedKeys[position]]
    fun reverse(): OrderedMap<K, V> {
        val order = ArrayList<K>(orderedKeys)
        order.reverse()
        return OrderedMap(data, order)
    }
}
