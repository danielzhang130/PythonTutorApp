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
