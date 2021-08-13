package com.congxiaoyao.widget

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.window.*
import com.congxiaoyao.JavaFileMethodBoxLoader
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.awt.event.InputModifiersHelper
import java.io.File


@Composable
fun MethodBoxWindow(
  size: WindowSize,
  state: MethodBoxState,
  onCloseRequest: () -> Unit,
  onSearchRequest: () -> Unit,
  onLoadNewState: (MethodBoxState) -> Unit,
  content: @Composable BoxScope.() -> Unit
) {
  Window(
    onCloseRequest = onCloseRequest,
    state = rememberWindowState(
      position = WindowPosition.PlatformDefault,
      size = size
    ),
    onPreviewKeyEvent = handleKeyEvent(
      onSpace = onSearchRequest,
      onClose = onCloseRequest
    )
  ) {
    window.setLocationRelativeTo(null)

    DesktopMaterialTheme {
      Box {
        MethodBox(state)
        content()
      }
    }
    SideEffect {
      FocusRequester.Default.requestFocus()
    }

    listenFileDrop {
      println(it)
      onLoadNewState(JavaFileMethodBoxLoader(it).loadBoxState())
    }
  }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun handleKeyEvent(
  onSpace: () -> Unit,
  onClose: () -> Unit
): (KeyEvent) -> Boolean = lambda@{
  return@lambda if (it.isKeyUp(Key.Spacebar)) {
    onSpace()
    true
  } else if (it.isKeyDown(Key.W) && it.isMetaPressed) {
    onClose()
    true
  } else false
}

@Composable
private fun FrameWindowScope.listenFileDrop(action: (File) -> Unit) {
  LaunchedEffect(Unit) {
    println("listen file drop")
    DropTarget(window, DnDConstants.ACTION_COPY_OR_MOVE, object : DropTargetAdapter() {
      override fun drop(dtde: DropTargetDropEvent) {
        if (!dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) return
        // 接收拖拽目标数据
        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE)
        // 以文件集合的形式获取数据
        val files = dtde.transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
        files.firstOrNull()?.takeIf { it.isFile }?.also(action)
      }
    }, true)
  }
}
