package com.congxiaoyao.widget

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.*
import com.congxiaoyao.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.math.*

@Composable
fun MethodBox(boxState: MethodBoxState) {
  val popUpState by remember { mutableStateOf(PopUpState(listOf("生成子图(出)", "删除"))) }
  val indicatorState by remember { mutableStateOf(LevelIndicatorState(2)) }
  val scrollState = rememberScrollState(0)

  var requestPopPosition by remember { mutableStateOf(IntOffset.Zero) }
  popUpState.position = requestPopPosition.let { p -> p.copy(x = p.x - scrollState.value) }

  PopupContainer(popUpState, onSelect = { handlePopupSelect(it, boxState) }) {
    MethodBoxLayout(
      boxState,
      Modifier.fillMaxSize()
        .horizontalScroll(scrollState)
        .handleKeyEvent(boxState, popUpState,indicatorState)
        .drawMethodCall(boxState)
        .clearSelectionWhenTapOutside(boxState, popUpState)
    ) {
      boxState.labels.forEachIndexed { index, state ->
        MethodLabel(
          state,
          labelAlpha(boxState, index),
          boxState.selectIndex == index,
          onDrag = { state.offsetPosition(it) },
          onSelect = { boxState.select(index) },
          onRequestPopup = {
            requestPopPosition = (state.offset + it.round() + IntOffset(0, 1))
            popUpState.isVisible = true
            boxState.select(index)
          },
          onClosePopup = { popUpState.isVisible = false }
        )
      }
    }

    boxState.selectedLabel?.apply {
      val bound = boundsWithScale
      val offset = bound.centerRight.round() - IntOffset(scrollState.value, 0)
      LevelIndicator(indicatorState, Modifier.offset { offset.let { it.copy(it.x + 8.dp.roundToPx()) } })
    }
  }
}

private fun handlePopupSelect(item: Int, state: MethodBoxState) {
  when (item) {
    0 -> {
      val subGraphRootName = state.run { labels[selectIndex].name }
      thread {
        launchApplication(GraphMethodBoxLoader.create(state.graph!!, subGraphRootName))
      }
    }
    1 -> {
      val selectIndex = state.selectIndex.takeIf { it >= 0 } ?: return
      state.unSelect()
      state.removeLabel(selectIndex)
    }
  }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.handleKeyEvent(
  state: MethodBoxState,
  popUpState: PopUpState,
  indicatorState: LevelIndicatorState
) =
  focusRequester(FocusRequester.Default)
    .focusable()
    .onPreviewKeyEvent callback@{
      if (it.isKeyUp(Key.Backspace)) {
        val selectIndex = state.selectIndex.takeIf { it >= 0 } ?: return@callback false
        state.unSelect()
        state.removeLabel(selectIndex)
        true
      } else if (it.isKeyUp(Key.Escape)) {
        state.unSelect()
        popUpState.isVisible = false
        true
      } else if (it.isKeyUp(Key.DirectionUp)) {
        state.upgradeSelectLevel()
        indicatorState.level = state.selectLevel
        true
      } else if (it.isKeyUp(Key.DirectionDown)) {
        state.degradeSelectLevel()
        indicatorState.level = state.selectLevel
        true
      } else false
    }

internal fun KeyEvent.isKeyDown(key: Key) = this.key == key && this.type == KeyEventType.KeyDown
internal fun KeyEvent.isKeyUp(key: Key) = this.key == key && this.type == KeyEventType.KeyUp

private fun labelAlpha(boxState: MethodBoxState, labelIndex: Int): Float {
  if (boxState.selectIndex < 0) return 1f
  return if (labelIndex in boxState.highlightIndexes) 1f else .25f
}

val CALL_OUT_COLOR = Color(58, 125, 74)

private fun Modifier.drawMethodCall(boxState: MethodBoxState) = drawWithContent scope@{
  val graph = boxState.graph ?: run { drawContent();return@scope }
  val bounds = boxState.labels.map { it.boundsWithScale }
  //无选中label
  if (boxState.selectIndex < 0) {
    boxState.labels.forEachIndexed outer@{ i, from ->
      boxState.labels.forEachIndexed inner@{ j, to ->
        if (!graph.isConnect(i, j)) return@inner
        val p0 = bounds[i].getIntersection(bounds[j].center) ?: return@inner
        val p1 = bounds[j].getIntersection(bounds[i].center) ?: return@inner
        drawLine(Color.Gray, p0, p1, 1f)
        this.drawArrow(p0, p1, Color.Gray, ARROW_PATH_LITE)
      }
    }
    drawContent()
    return@scope
  }
  //选中状态
  drawContent()
  val selectIndex = boxState.selectIndex
  val selectLevel = boxState.selectLevel

  if (boxState.selectLevel == 0) {
    graph.visitFrom(selectIndex, 1) { from, to ->
      drawConnection(bounds[from], bounds[to], CALL_OUT_COLOR)
    }
    graph.visitTo(selectIndex, 1) { from, to ->
      drawConnection(bounds[from], bounds[to], Color.Red)
    }
  } else if (selectLevel < 0) {
    graph.visitTo(selectIndex, -selectLevel) { from, to ->
      drawConnection(bounds[from], bounds[to], Color.Red)
    }
  } else {
    graph.visitFrom(selectIndex, selectLevel) { from, to ->
      drawConnection(bounds[from], bounds[to], CALL_OUT_COLOR)
    }
  }
}

private val ARROW_PATH = Path().apply {
  val r = 15
  val theta = Math.toRadians(20.0)
  moveTo(0f, 0f)
  lineTo((r * cos(theta)).toFloat(), (r * sin(theta)).toFloat())
  lineTo((r * cos(theta)).toFloat(), (r * -sin(theta)).toFloat())
  close()
}

private val ARROW_PATH_LITE = Path().apply {
  val r = 10
  val theta = Math.toRadians(20.0)
  moveTo(0f, 0f)
  lineTo((r * cos(theta)).toFloat(), (r * sin(theta)).toFloat())
  lineTo((r * cos(theta)).toFloat(), (r * -sin(theta)).toFloat())
  close()
}

private fun DrawScope.drawArrow(p0: Offset, p1: Offset, color: Color, path: Path = ARROW_PATH) {
  val delta = p1 - p0
  val angle = atan2(delta.y, delta.x) - Math.PI
  rotate(Math.toDegrees(angle).toFloat(), p1) {
    translate(p1.x, p1.y) {
      drawPath(path, color, style = Fill)
      drawPath(path, color, style = Stroke(1f))
    }
  }
}

private fun DrawScope.drawConnection(from: Rect, to: Rect, color: Color, arrowPath: Path = ARROW_PATH) {
  val p0 = from.getIntersection(to.center) ?: return
  val p1 = to.getIntersection(from.center) ?: return
  drawLine(color, p0, p1, 2f)
  drawArrow(p0, p1, color, arrowPath)
}

private fun Rect.getIntersection(p0: Offset): Offset? {
  val r = height / 2
  val rsq = r * r
  val horOffsetR = Offset(r, 0f)
  val p1 = center
  val p4 = centerLeft
  val p2 = p4 + horOffsetR
  val p5 = centerRight
  val p3 = p5 - horOffsetR
  val p6 = topLeft + horOffsetR
  val p7 = topRight - horOffsetR
  val p8 = bottomLeft + horOffsetR
  val p9 = bottomRight - horOffsetR
  if (p0.x >= p6.x && p0.x <= p7.x && p0.y >= p6.y && p0.y <= p8.y) return null
  if ((p0 - p2).getDistanceSquared() <= rsq) return null
  if ((p0 - p3).getDistanceSquared() <= rsq) return null

  if (p1.x == p0.x) {
    return if (p0.y < p6.y) Offset(p0.x, p6.y) else Offset(p0.x, p8.y)
  }
  val k = (p1.y - p0.y) / (p1.x - p0.x)
  val ksq = k * k
  var b = p0.y - p0.x * k

  if (p0.y < p6.y) {
    ((p6.y - b) / k).takeIf { it in p6.x..p7.x }?.also { return Offset(it, p6.y) }
  } else {
    ((p8.y - b) / k).takeIf { it in p8.x..p9.x }?.also { return Offset(it, p8.y) }
  }

  if (p0.x < p6.x) {
    b += (p2.x * k - p2.y)
    val bsq = b * b
    val delta = sqrt(4 * ksq * bsq - 4 * (ksq + 1) * (bsq - rsq))
    val ix0 = (-2 * k * b + delta) / (2 * (ksq + 1))
    if (ix0 < 0) {
      return Offset(ix0 + p2.x, k * ix0 + b + p2.y)
    }
    val ix1 = (-2 * k * b - delta) / (2 * (ksq + 1))
    if (ix1 < 0) {
      return Offset(ix1 + p2.x, k * ix1 + b + p2.y)
    }
  } else if (p0.x > p7.x) {
    b += (p3.x * k - p3.y)
    val bsq = b * b
    val delta = sqrt(4 * ksq * bsq - 4 * (ksq + 1) * (bsq - rsq))
    val ix0 = (-2 * k * b + delta) / (2 * (ksq + 1))
    if (ix0 > 0) {
      return Offset(ix0 + p3.x, k * ix0 + b + p3.y)
    }
    val ix1 = (-2 * k * b - delta) / (2 * (ksq + 1))
    if (ix1 > 0) {
      return Offset(ix1 + p3.x, k * ix1 + b + p3.y)
    }
  }
  return null
}

private fun Modifier.clearSelectionWhenTapOutside(boxState: MethodBoxState, popUpState: PopUpState) =
  pointerInput(boxState) {
    forEachGesture {
      awaitPointerEventScope {
        awaitFirstDown(true)
        popUpState.isVisible = false
        boxState.unSelect()
        FocusRequester.Default.requestFocus()
      }
    }
  }

@Composable
private fun MethodBoxLayout(
  boxState: MethodBoxState,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Layout(
    content = { content() },
    modifier = modifier,
    measurePolicy = remember(boxState) { measurePolicy(boxState) }
  )
}

private fun measurePolicy(boxState: MethodBoxState) = object : MeasurePolicy {
  var isFirstLayout = true
  var totalWidth = 0

  override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
    val contentConstraints = constraints.copy(minWidth = 0, minHeight = 0)
    val placeables = measurables.map { it.measure(contentConstraints) }

    if (isFirstLayout) {
      var x = 0
      var y = 0
      val marginRight = 10
      val marginBottom = 10
      var rowMaxWidth = 0

      placeables.forEachIndexed { index, placeable ->
        if (y == 0) {
          rowMaxWidth = 0
        } else if (y + placeable.height > constraints.maxHeight) {
          y = 0
          x += (rowMaxWidth + marginRight)
          rowMaxWidth = 0
        }

        val state = boxState.labels[index]
        state.updatePosition(IntOffset(x, y))
        state.updateSize(placeable.width, placeable.height)

        y += (placeable.height + marginBottom)
        rowMaxWidth = max(placeable.width, rowMaxWidth)
      }
      if (placeables.isNotEmpty()) {
        isFirstLayout = false
      }
      totalWidth = x + rowMaxWidth
    }

    val width = if (totalWidth == 0) {
      constraints.maxWidth
    } else if (constraints.maxWidth == Int.MAX_VALUE) {
      totalWidth.coerceAtLeast(constraints.minWidth)
    } else {
      constraints.maxWidth
    }

    return layout(width, constraints.maxHeight) {
      if (placeables.isEmpty()) return@layout
      boxState.drawingOrders.forEach {
        val state = boxState.labels[it]
        val placeable = placeables[it]
        placeable.place(state.offset)
      }
    }
  }
}

@Stable
class MethodBoxState {
  var graph: MethodGraph? = null
  var selectIndex by mutableStateOf(-1)
  val labels: List<MethodLabelState> = mutableStateListOf()
  var drawingOrders = mutableListOf<Int>()

  var selectLevel by mutableStateOf(0)
    private set
  var highlightIndexes by mutableStateOf(emptySet<Int>(), referentialEqualityPolicy())
    private set

  val selectedLabel get() = labels.getOrNull(selectIndex)

  fun setLabels(list: List<MethodLabelState>) {
    (labels as MutableList).clear()
    drawingOrders.apply {
      clear()
      addAll(list.mapIndexed { index, _ -> labels.size + index })
    }
    labels.addAll(list)
    highlightIndexes = emptySet()
  }

  fun removeLabel(index: Int) {
    graph?.remove(index)
    (labels as MutableList).removeAt(index)
    val orders = drawingOrders
    orders.remove(index)
    repeat(orders.size) {
      if (orders[it] > index) {
        orders[it]--
      }
    }
  }

  fun select(index: Int) {
    selectIndex = index.let { if (it in labels.indices) it else -1 }
    refreshHighlightIndexesAndDrawingOrder()
  }

  fun unSelect() {
    selectIndex = -1
    refreshHighlightIndexesAndDrawingOrder()
  }

  fun upgradeSelectLevel() = updateSelectLevel(selectLevel + 1)

  fun degradeSelectLevel() = updateSelectLevel(selectLevel - 1)

  fun updateSelectLevel(level: Int) {
    selectLevel = level
    refreshHighlightIndexesAndDrawingOrder()
  }

  private fun refreshHighlightIndexesAndDrawingOrder() {
    if (selectIndex !in labels.indices) {
      highlightIndexes = emptySet()
      return
    }

    val indexes = mutableSetOf<Int>()
    if (selectLevel == 0) {
      graph?.visitFrom(selectIndex, 1) { _, to -> indexes += to }
      graph?.visitTo(selectIndex, 1) { from, _ -> indexes += from }
      indexes += selectIndex
    } else if (selectLevel < 0) {
      graph?.visitTo(selectIndex, -selectLevel) { from, _ -> indexes += from }
      indexes += selectIndex
    } else {
      graph?.visitFrom(selectIndex, selectLevel) { from, to ->
        indexes += to
        indexes += from
      }
      indexes += selectIndex
    }
    highlightIndexes = indexes

    bringLabelsToTop(indexes)
  }

  fun bringLabelToTop(index: Int) {
    if (drawingOrders.remove(index)) {
      drawingOrders.add(index)
    }
  }

  private fun bringLabelsToTop(indexes: Set<Int>) {
    val newOrders = ArrayList<Int>(labels.size)
    drawingOrders.forEach {
      if (it !in indexes) newOrders += it
    }
    newOrders.addAll(indexes)
    drawingOrders = newOrders
  }

  fun getNamesFromGraph() = graph?.getNames() ?: emptyList()
}