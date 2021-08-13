package com.congxiaoyao

import com.congxiaoyao.lang.Lexer
import com.congxiaoyao.lang.Parser
import com.congxiaoyao.widget.MethodBoxState
import com.congxiaoyao.widget.MethodLabelState
import java.io.*
import java.lang.Exception

interface MethodBoxLoader {
  fun loadBoxState(): MethodBoxState
}

class JavaFileMethodBoxLoader(
  private val file: File,
) : MethodBoxLoader {
  override fun loadBoxState(): MethodBoxState {
    val state = MethodBoxState()
    val content = file.readText()
    val parser = Parser(Lexer(content).toTokenList())
    val jClass = parser.readJClass()!!
    jClass.parseBody()
    val localMethods = jClass.methods.map { it.name }.toSet()
    state.setLabels(localMethods.map { MethodLabelState(it) })
    val graph = MethodGraph(localMethods.toList())
    jClass.methods.forEach { from ->
      val callingLocalMethod = from.callingMethod().filter { it in localMethods }
      callingLocalMethod.forEach { to ->
        graph.addConnection(from.name, to)
      }
    }
    state.graph = graph
    return state
  }
}

object TestMethodBoxLoader : MethodBoxLoader {
  override fun loadBoxState(): MethodBoxState {
    return try {
      JavaFileMethodBoxLoader(File("src/main/resources/BananaConfirmLoanFragmentV2.java")).loadBoxState()
    } catch (e: Exception) {
      MethodBoxState()
    }
  }
}

abstract class GraphMethodBoxLoader : MethodBoxLoader {

  companion object {

    fun create(graph: MethodGraph, subGraphRootName: String) = object : GraphMethodBoxLoader() {
      override fun loadBoxState(): MethodBoxState {
        val subGraph = graph.getSubGraphFrom(subGraphRootName)
        return createBoxState(subGraph).apply {
          select(labels.indexOfFirst { it.name == subGraphRootName })
        }
      }
    }

    fun create(graph: MethodGraph, subNodes: List<Int>) = object : GraphMethodBoxLoader() {
      override fun loadBoxState(): MethodBoxState {
        return createBoxState(graph.createSubGraph(subNodes))
      }
    }
  }

  protected fun createBoxState(graph: MethodGraph): MethodBoxState {
    val state = MethodBoxState()
    state.setLabels(graph.getNames().map { MethodLabelState(it) })
    state.graph = graph
    return state
  }
}
