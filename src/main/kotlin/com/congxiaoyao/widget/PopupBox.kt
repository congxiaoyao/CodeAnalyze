package com.congxiaoyao.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PopupContainer(
  state: PopUpState,
  modifier: Modifier = Modifier,
  onSelect: (Int) -> Unit = {},
  content: @Composable BoxScope.() -> Unit
) {
  Box(modifier.fillMaxSize()) {
    content()
    AnimatedVisibility(state.isVisible, Modifier.offset { state.position }) {
      ItemGroup(state.items) {
        state.isVisible = false
        onSelect(it)
      }
    }
  }
}

@Composable
private fun ItemGroup(
  names: List<String>,
  onSelect: (Int) -> Unit
) {
  val shape = RoundedCornerShape(5.dp)
  Column(
    Modifier.width(IntrinsicSize.Max)
      .defaultMinSize(200.dp)
      .shadow(10.dp, shape)
      .background(Color.White)
      .clip(shape),
  ) {
    Spacer(Modifier.height(8.dp))
    names.forEachIndexed { index, name ->
      val (isActive,changeActive) = remember { mutableStateOf(false) }
      Text(
        text = name,
        style = typography.body2,
        modifier = Modifier
          .clickable { onSelect(index) }
          .observeMousePointer(changeActive)
          .background(if (isActive) Color.LightGray else Color.Transparent)
          .padding(8.dp)
          .fillMaxWidth(),
        softWrap = false
      )
    }
    Spacer(Modifier.height(8.dp))
  }
}

private fun Modifier.observeMousePointer(changeActive: (Boolean) -> Unit) = this.pointerMoveFilter(
  onEnter = {
    changeActive(true)
    false
  },
  onExit = {
    changeActive(false)
    false
  }
)

@Stable
class PopUpState(var items: List<String>) {
  var position: IntOffset by mutableStateOf(IntOffset.Zero)
  var isVisible by mutableStateOf(false)

}