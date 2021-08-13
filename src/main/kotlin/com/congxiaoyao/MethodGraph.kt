package com.congxiaoyao

import com.google.gson.annotations.JsonAdapter
import java.util.*

class MethodGraph(
  private var names: List<String>,
  @JsonAdapter(MethodStore.MatrixJsonAdapter::class)
  var graph: Array<BooleanArray> = Array(names.size) { BooleanArray(names.size) }
) {
  fun getNames() = names
  fun addConnection(from: String, to: String) = setConnection(from, to, true)
  fun clearConnection(from: String, to: String) = setConnection(from, to, false)
  fun isConnect(from: Int, to: Int) = graph[from][to]
  fun isConnect(from: String, to: String) = graph[indexOf(from)][indexOf(to)]
  fun remove(name: String) = remove(indexOf(name))
  fun remove(index: Int) {
    names = names.filterIndexed { i, _ -> i != index }
    val temp = graph.filterIndexed { i, _ -> i != index }
    graph = Array(names.size) {
      temp[it].filterIndexed { i, _ -> i != index }.toBooleanArray()
    }
  }

  fun createSubGraph(subNodes: List<Int>): MethodGraph {
    val subGraph = Array(subNodes.size) { row ->
      subNodes.map { graph[subNodes[row]][it] }.toBooleanArray()
    }
    val subNames = subNodes.map { names[it] }
    return MethodGraph(subNames, subGraph)
  }

  fun getSubGraphFrom(name: String, maxLevel: Int = Int.MAX_VALUE) = getSubGraphFrom(indexOf(name), maxLevel)

  fun getSubGraphFrom(fromIndex: Int, maxLevel: Int = Int.MAX_VALUE): MethodGraph {
    val visit = Array(names.size) { false }
    visitSubNamesFrom(fromIndex, visit, maxLevel)
    val visitedIndexes = visit.mapIndexedNotNull { index, b -> if (b) index else null }
    return createSubGraph(visitedIndexes)
  }

  fun getSubGraphTo(name: String, maxLevel: Int = Int.MAX_VALUE): MethodGraph {
    val visit = Array(graph.size) { false }
    visitSubNamesTo(indexOf(name), visit, maxLevel)
    val visitedIndexes = visit.mapIndexedNotNull { index, b -> if (b) index else null }
    return createSubGraph(visitedIndexes)
  }

  fun visitFrom(index: Int, level: Int, action: (from: Int, to: Int) -> Unit) {
    val visit = Array(graph.size) { false }
    visitSubNamesFrom(index, visit, level, action)
  }

  fun visitTo(index: Int, level: Int, action: (from: Int, to: Int) -> Unit) {
    val visit = Array(graph.size) { false }
    visitSubNamesTo(index, visit, level, action)
  }

  private fun visitSubNamesFrom(
    rootIndex: Int,
    visit: Array<Boolean>,
    remainLevel: Int,
    action: (from: Int, to: Int) -> Unit = { _, _ -> }
  ) {
    var curLevel = remainLevel
    val queue = LinkedList<Int>()
    queue.offer(rootIndex)
    while (queue.isNotEmpty() && curLevel > 0) {
      val size = queue.size
      for (i in 0 until size) {
        val fromIndex = queue.pop()
        visit[fromIndex] = true
        graph[fromIndex].forEachIndexed { toIndex, b ->
          if (b) {
            action(fromIndex,toIndex)
            if (!visit[toIndex]) queue.offer(toIndex)
          }
        }
      }
      curLevel--
    }
  }

  private fun visitSubNamesTo(
    rootIndex: Int,
    visit: Array<Boolean>,
    remainLevel: Int,
    action: (from: Int, to: Int) -> Unit = { _, _ -> }
  ) {
    var curLevel = remainLevel
    val queue = LinkedList<Int>()
    queue.offer(rootIndex)
    while (queue.isNotEmpty() && curLevel > 0){
      val size = queue.size
      for (i in 0 until size) {
        val toIndex = queue.pop()
        visit[toIndex] = true
        graph.forEachIndexed { fromIndex, b ->
          if (b[toIndex]){
            action(fromIndex,toIndex)
            if (!visit[fromIndex]) queue.offer(fromIndex)
          }
        }
      }
      curLevel--
    }
  }

  private fun visitSubNames(index: Int, visit: Array<Int>) {
    visit[index] = 1
    graph[index].forEachIndexed { i, b -> if (visit[i] == 0 && b) visitSubNames(i, visit) }
  }

  private fun setConnection(from: String, to: String, b: Boolean) {
    val fromIndex = indexOf(from)
    val toIndex = indexOf(to)
    graph[fromIndex][toIndex] = b
  }

  private fun indexOf(name: String) = names.indexOf(name)
}