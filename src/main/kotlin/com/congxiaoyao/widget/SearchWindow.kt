package com.congxiaoyao.widget

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Build
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowSize
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.regex.Pattern

@Composable
fun SearchWindow(
  state: SearchWindowState,
  onCloseRequest: () -> Unit,
  onSelect: (String) -> Unit,
  onOptionClick: (Int) -> Unit
) = Window(
  visible = state.isOpen,
  undecorated = true,
  resizable = false,
  onCloseRequest = onCloseRequest,
  state = rememberWindowState(size = WindowSize(480.dp, 600.dp)),
  onPreviewKeyEvent = @OptIn(ExperimentalComposeUiApi::class) {
    if (it.isKeyUp(Key.Escape)) onCloseRequest()
    false
  },
) {
  window.setLocationRelativeTo(null)
  window.background = java.awt.Color(0, true)
  window.addWindowFocusListener(object : WindowAdapter() {
    override fun windowLostFocus(e: WindowEvent) = onCloseRequest()
  })

  val focusRequester = remember { FocusRequester() }

  DesktopMaterialTheme {
    val shape = remember { RoundedCornerShape(CornerSize(8.dp)) }
    Column(
      Modifier.fillMaxSize()
        .clip(shape)
        .background(colors.background),
    ) {
      HeaderLayout(focusRequester, state, onSelect, onOptionClick)

      Box(Modifier.weight(1f)) {
        SearchList(state.highLightIndex, state.searchResults, state.listState, onSelect)
        VerticalScrollbar(rememberScrollbarAdapter(state.listState), Modifier.align(Alignment.TopEnd))
      }

      FooterLayout(state)
    }
  }

  SideEffect {
    focusRequester.requestFocus()
    val keyword = state.keyword
    if (keyword.text.isNotEmpty()) {
      state.keyword = keyword.copy(selection = TextRange(0, keyword.text.length))
    }
  }
}

@Composable
private fun FooterLayout(state: SearchWindowState) = Box(
  Modifier.height(24.dp)
    .fillMaxWidth()
    .background(colors.onSurface.copy(alpha = .05f))
    .padding(start = 8.dp)
) {
  Text(
    "共计方法数 ${state.rawData.size}",
    Modifier.align(Alignment.CenterStart),
    style = typography.caption,
    color = colors.onBackground.copy(.5f)
  )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun HeaderLayout(
  focusRequester: FocusRequester,
  state: SearchWindowState,
  onSelect: (String) -> Unit,
  onOptionClick: (Int) -> Unit
) {
  Column(Modifier.shadow(4.dp).background(Color.White).zIndex(1f)) {
    val scope = rememberCoroutineScope()
    TextField(modifier = Modifier.fillMaxWidth()
      .focusRequester(focusRequester)
      .onPreviewKeyEvent {
        val isDirectionKey = handleDirectionKeyEvent(it, state, scope)
        if (isDirectionKey) {
          return@onPreviewKeyEvent true
        } else if (it.type == KeyEventType.KeyUp && it.key == Key.Enter) {
          onSelect(state.searchResults[state.highLightIndex])
          return@onPreviewKeyEvent true
        }
        false
      },
      value = state.keyword,
      singleLine = true,
      shape = RectangleShape,
      textStyle = typography.h5,
      onValueChange = {
        with(state) {
          keyword = it
          searchResults.clear()
          searchResults.addAll(search(rawData, it.text))
          highLightIndex = 0
        }
      })

    Row(
      Modifier.padding(vertical = 8.dp)
        .horizontalScroll(rememberScrollState()),
      Arrangement.spacedBy(10.dp)
    ) {
      Spacer(Modifier)
      repeat(state.options.size) {
        Card {
          Row(
            Modifier.clickable { onOptionClick(it) }
              .background(Color.LightGray.copy(alpha = .3f))
              .padding(horizontal = 8.dp),
            Arrangement.spacedBy(2.dp),
            Alignment.CenterVertically
          ) {
            Image(
              Icons.Rounded.Build,
              "",
              Modifier.size(16.dp),
              colorFilter = ColorFilter.tint(Color.Gray)
            )
            Text(
              state.options[it],
              Modifier.padding(vertical = 4.dp),
              style = typography.body2,
              color = Color.DarkGray
            )
          }
        }
      }
      Spacer(Modifier)
    }
  }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun handleDirectionKeyEvent(
  event: KeyEvent,
  state: SearchWindowState,
  scope: CoroutineScope
): Boolean {
  val listState = state.listState
  val listSize = state.searchResults.size

  if (event.key == Key.DirectionDown && event.type == KeyEventType.KeyDown) {
    val targetIndex = (state.highLightIndex + 1) % listSize
    val info = listState.layoutInfo.visibleItemsInfo
    if (info.isEmpty()) return true
    val last2 = info.getOrNull(info.lastIndex - 2) ?: run {
      state.highLightIndex = targetIndex
      return true
    }
    scope.launch {
      if (targetIndex < state.highLightIndex) {
        listState.scrollToItem(0)
      } else if (targetIndex >= last2.index) {
        listState.scrollToItem(info.first().index + 1)
      } else if (targetIndex !in info.first().index..info.last().index) {
        listState.scrollToItem(targetIndex)
      }
      state.highLightIndex = targetIndex
    }
    return true
  } else if (event.key == Key.DirectionUp && event.type == KeyEventType.KeyDown) {
    val targetIndex = (state.highLightIndex - 1 + listSize) % listSize
    val info = listState.layoutInfo.visibleItemsInfo
    if (info.isEmpty()) return false
    val second = info.getOrNull(2) ?: run {
      state.highLightIndex = targetIndex
      return true
    }
    scope.launch {
      if (targetIndex > state.highLightIndex) {
        listState.scrollToItem(listSize - 1)
      } else if (targetIndex !in info.first().index..info.last().index) {
        listState.scrollToItem(targetIndex)
      } else if (targetIndex <= second.index) {
        listState.scrollToItem((info.first().index.minus(1)).coerceAtLeast(0))
      }
      state.highLightIndex = targetIndex
    }
    return true
  }
  return false
}

private fun search(source: List<String>, keyWord: String): List<String> {
  val reg = "(.*)(" + keyWord.lowercase().map { it }.joinToString(")(.*)(") + ")(.*)"
  val pattern = Pattern.compile(reg)
  return source.filter { pattern.matcher(it.lowercase()).lookingAt() }
}

@Composable
private fun SearchList(
  highLightIndex: Int,
  list: List<String>,
  state: LazyListState,
  onClickName: (String) -> Unit
) {
  LazyColumn(state = state) {
    itemsIndexed(list) { position, name ->
      Row(modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)
        .clickable { onClickName(name) }
        .background(if (highLightIndex == position) colors.primary.copy(alpha = .4f) else colors.background)
      ) {
        Box(Modifier.fillParentMaxWidth()) {
          Text(
            text = name,
            Modifier.align(Alignment.CenterStart)
              .padding(8.dp, 8.dp),
            style = typography.body1,
            maxLines = 1,
            color = if (highLightIndex == position) colors.background else colors.onBackground
          )
          if (highLightIndex == position) {
            Image(
              Icons.Default.KeyboardArrowRight, "",
              Modifier.align(Alignment.CenterEnd)
                .fillMaxHeight(),
              colorFilter = ColorFilter.tint(colors.background)
            )
          }
        }
      }
    }
  }
}

@Stable
class SearchWindowState {
  var isOpen by mutableStateOf(false)
  var keyword: TextFieldValue by mutableStateOf(TextFieldValue(""))
  var highLightIndex by mutableStateOf(0)
  var listState: LazyListState = LazyListState()
  val searchResults: MutableList<String> = mutableStateListOf()

  var options = emptyList<String>()

  var rawData = listOf<String>()

  fun fillRawData(list: List<String>) {
    rawData = list
    searchResults.clear()
    searchResults.addAll(rawData)
    highLightIndex = 0
    listState = LazyListState()
    keyword = TextFieldValue("")
  }
}
