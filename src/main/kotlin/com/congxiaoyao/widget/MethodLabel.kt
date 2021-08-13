package com.congxiaoyao.widget

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalDesktopApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.*
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
fun MethodLabel(
  state: MethodLabelState,
  alpha: Float,
  isSelect: Boolean,
  modifier: Modifier = Modifier,
  onDrag: (offset: Offset) -> Unit,
  onSelect: () -> Unit,
  onRequestPopup: (position: Offset) -> Unit,
  onClosePopup: () -> Unit
) {
  val labelShape = RoundedCornerShape(50)
  val requester = remember { FocusRequester() }

  val deflate = remember { mutableStateOf(false) }
  val scale = animateFloatAsState(
    targetValue = if (deflate.value) .9f else 1f,
    animationSpec = tween(60, easing = LinearEasing),
    finishedListener = { deflate.value = false }
  )
  state.scale = scale.value

  Box(
    Modifier.wrapContentSize()
      .scale(scale.value)
      .focusRequester(requester)
      .focusable()
      .border(1.dp, if (isSelect) Color.Gray else Color.LightGray, labelShape)
      .shadow(if (isSelect) 10.dp else 0.dp, labelShape)
      .background(colors.background, labelShape)
      .alpha(alpha)
      .handleKeyEvent(state, deflate)
      .handleMouseEvent(state, requester, onSelect, onDrag, onRequestPopup, onClosePopup)
      .then(modifier)
  ) {
    Text(
      state.name,
      style = typography.body2,
      color = Color.DarkGray,
      modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)
    )
  }
}

@OptIn(ExperimentalDesktopApi::class)
private fun Modifier.handleMouseEvent(
  state: MethodLabelState,
  requester: FocusRequester,
  onSelect: () -> Unit,
  onDrag: (offset: Offset) -> Unit,
  onRequestPopup: (position: Offset) -> Unit,
  onClosePopup: () -> Unit
) = pointerInput(state) {
  detectDragGestures(onDrag = { _, dragAmount ->
    onDrag(Offset(dragAmount.x, dragAmount.y))
  }, onDragStart = { onClosePopup() })
}.pointerInput(state) {
  forEachGesture {
    awaitPointerEventScope {
      val inputChange = awaitFirstDown(false)
      inputChange.consumed.downChange = true
      onSelect()
      requester.requestFocus()
      if (currentEvent.buttons.isSecondaryPressed) {
        onRequestPopup(inputChange.position)
      } else if (currentEvent.buttons.isPrimaryPressed) {
        onClosePopup()
      }
    }
  }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.handleKeyEvent(
  state: MethodLabelState,
  deflate: MutableState<Boolean>
) = onPreviewKeyEvent callback@{
  if (it.isKeyDown(Key.C) && it.isMetaPressed) {
    deflate.value = true
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(state.name), null)
  }
  false
}

@Stable
class MethodLabelState(name: String) {
  var name by mutableStateOf(name)
  var offset by mutableStateOf(IntOffset(0, 0))
  var scale: Float = 1f

  private var size = IntSize(0, 0)

  val boundsWithScale: Rect
    get() {
      val scaledSize = size.toSize() * scale
      val leftTop = offset.toOffset() +
          Offset(size.width * 1f, size.height * 1f) * (1 - scale) / 2f
      return Rect(leftTop, scaledSize)
    }

  fun updatePosition(offset: IntOffset) {
    this.offset = offset
  }

  fun offsetPosition(offset: Offset) {
    this.offset += offset.round()
  }

  fun updateSize(width: Int, height: Int) {
    size = IntSize(width, height)
  }
}