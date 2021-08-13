package com.congxiaoyao.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.congxiaoyao.AppState
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppSnackBar(state: SnackBarState, modifier: Modifier) {
  val remembered = remember { mutableStateOf(state.showSnack) }
  val timestamp = remember { mutableStateOf(state.snackTimestamp) }
  if (remembered.value && state.showSnack && state.snackTimestamp != timestamp.value) {
    LaunchedEffect(state.snackTimestamp) {
      delay(2000)
      state.hideMessage()
      remembered.value = false
      timestamp.value = state.snackTimestamp
    }
  }

  AnimatedVisibility(state.showSnack, modifier) {
    remembered.value = true
    Snackbar(backgroundColor = Color.DarkGray){
      Text(state.messageText)
    }
  }
}

@Stable
class SnackBarState {
  var showSnack by mutableStateOf(false)
    private set
  var messageText = ""
    private set
  var snackTimestamp by mutableStateOf(0L)
    private set

  fun showMessage(message: String) {
    snackTimestamp = System.currentTimeMillis()
    this.messageText = message
    showSnack = true
  }

  fun hideMessage() {
    showSnack = false
    messageText = ""
  }
}

val LocalSnackBar = compositionLocalOf { SnackBarState() }

@Composable
fun showSnackBar(message: String) = LocalSnackBar.current.showMessage(message)