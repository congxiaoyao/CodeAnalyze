package com.congxiaoyao

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.WindowSize
import androidx.compose.ui.window.application
import com.congxiaoyao.widget.*

fun main() {
  launchApplication(TestMethodBoxLoader)
}

private fun app(content: @Composable ApplicationScope.() -> Unit) {
  application {
    CompositionLocalProvider(
      LocalSnackBar.provides(SnackBarState()),
    ) {
      content()
    }
  }
}

fun launchApplication(loader: MethodBoxLoader) = application {
  val appState by remember { mutableStateOf(AppState()) }

  val searchWindowState = appState.searchWindowState
  val methodBoxState = appState.methodBoxState
  val snackBar = LocalSnackBar.current

  MethodBoxWindow(
    WindowSize(1280.dp, 720.dp),
    methodBoxState,
    onCloseRequest = this::exitApplication,
    onSearchRequest = { searchWindowState.isOpen = true },
    onLoadNewState = {
      appState.updateByMethodBoxState(it)
    }
  ) {
    AppSnackBar(
      LocalSnackBar.current,
      Modifier.width(600.dp).align(Alignment.BottomCenter)
    )
  }

  if (searchWindowState.isOpen) {
    SearchWindow(
      searchWindowState,
      onCloseRequest = {
        searchWindowState.isOpen = false
      },
      onSelect = { name ->
        searchWindowState.isOpen = false
        methodBoxState.select(methodBoxState.labels.indexOfFirst { it.name == name })
      },
      onOptionClick = {
        when (it) {
          0 -> removeIsolatedVertex(appState,snackBar)
          1 -> snackBar.showMessage("暂不支持此功能哦~")
        }
      })
  }

  LaunchedEffect(Unit) {
    appState.updateByMethodBoxState(loader.loadBoxState())
  }
}

@Stable
class AppState {
  var methodBoxState by mutableStateOf(MethodBoxState())
  val searchWindowState by mutableStateOf(SearchWindowState().apply {
    options = listOf("清理孤立点", "保存")
  })

  fun updateByMethodBoxState(state: MethodBoxState) {
    methodBoxState = state
    searchWindowState.fillRawData(methodBoxState.getNamesFromGraph())
  }
}
