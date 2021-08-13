package com.congxiaoyao.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue


@Composable
fun LevelIndicator(state: LevelIndicatorState, modifier: Modifier) {
  val shape = RoundedCornerShape(50)
  val lineWidth = 1.5.dp
  Column(
    modifier
      .shadow(8.dp, shape)
      .width(16.dp)
      .layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val blockHeight = placeable.height / state.blockCount
        state.jointY = state.jointIndex * blockHeight + blockHeight / 2
        layout(placeable.width, placeable.height) {
          placeable.place(IntOffset.Zero)
        }
      }
      .background(colors.background)
      .drawWithContent {
        val contentSize = size.toRect().deflate(lineWidth.toPx()).size
        val contentDrawScope = this
        scale(contentSize.width / size.width, contentSize.height / size.height) {
          contentDrawScope.drawContent()
        }
      }
      .clip(shape)
  ) {
    val count = state.blockCount
    val jointIndex = state.jointIndex
    repeat(count) { index ->
      if (index > 0) {
        Spacer(Modifier.fillMaxWidth().height(lineWidth))
      }
      val curLevel = jointIndex - index
      if (curLevel == 0) {
        Block(Modifier, STYLE_MIX)
      } else if (state.level > 0 && curLevel in 1..state.level) {
        Block(Modifier, STYLE_GREEN)
      } else if (state.level < 0 && curLevel in -1 downTo state.level) {
        Block(Modifier, STYLE_RED)
      } else {
        Block(Modifier, STYLE_GRAY)
      }
    }
  }
}

private val trianglePath = Path().apply {
  moveTo(0f, 0f)
  lineTo(1f, 0f)
  lineTo(0f, 1f)
  close()
}

private const val STYLE_RED = 0
private const val STYLE_GREEN = 1
private const val STYLE_GRAY = 2
private const val STYLE_MIX = 3

@Composable
private fun Block(modifier: Modifier, style: Int) {
  val green = CALL_OUT_COLOR.copy(alpha = .65f)
  val red = Color.Red.copy(alpha = .65f)
  val gray = Color.LightGray

  val bg: Modifier = when (style) {
    STYLE_RED -> Modifier.background(red)
    STYLE_GREEN -> Modifier.background(green)
    STYLE_GRAY -> Modifier.background(gray)
    STYLE_MIX -> Modifier.drawBehind {
      scale(size.width, size.height,Offset.Zero) {
        drawPath(trianglePath, green)
        rotate(180f, Offset(.5f, .5f)) {
          drawPath(trianglePath, red)
        }
      }
    }
    else -> Modifier
  }
  Spacer(modifier.fillMaxWidth().layout { measurable, constraints ->
    val placeable = measurable.measure(
      constraints.let { it.copy(minHeight = (it.minWidth/1.2).toInt(), maxHeight = (it.maxWidth/1.2).toInt()) }
    )
    layout(placeable.width, placeable.height) {
      placeable.place(0, 0)
    }

  }.then(bg))
}

@Stable
class LevelIndicatorState(val minGrid: Int) {
  var level by mutableStateOf(0)
  var jointY by mutableStateOf(0)

  val blockCount get() = (level.absoluteValue + 1).coerceAtLeast(minGrid * 2 + 1)

  val jointIndex
    get() = when {
      level > minGrid -> level
      level < -minGrid -> (minGrid * 2 + level).coerceAtLeast(0)
      else -> minGrid
    }
}