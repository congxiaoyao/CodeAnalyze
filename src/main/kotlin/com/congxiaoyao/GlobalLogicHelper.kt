package com.congxiaoyao

import com.congxiaoyao.widget.SnackBarState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun removeIsolatedVertex(state: AppState, snackBar: SnackBarState) {
  val graph = state.methodBoxState.graph?.graph ?: return
  state.searchWindowState.isOpen = false
  MainScope().launch {
    val removingIndexes = withContext(Dispatchers.Default) {
      val removingIndexes = mutableListOf<Int>()
      for (i in graph.indices) {
        if (graph[i].all { !it } && graph.all { !it[i] }) {
          removingIndexes += i
        }
      }
      removingIndexes
    }
    with(state.methodBoxState) {
      val oldSelectedName = labels.getOrNull(selectIndex)?.name
      removingIndexes.sortedDescending().forEach {
        removeLabel(it)
      }
      select(labels.indexOfFirst { it.name == oldSelectedName })
    }
    snackBar.showMessage("共清理 ${removingIndexes.size} 个节点")
  }
}