package com.congxiaoyao

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.singleWindowApplication

fun main() = singleWindowApplication {
  DesktopMaterialTheme {
    println("remember")
    var bean by remember { mutableStateOf(Bean(true, "hello")) }

    Column(
      Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      if (bean.visible) {
        Text(bean.text)
      }
      OutlinedButton(onClick = {
        println("in")
        bean = bean.copy(visible = bean.visible.not())
      }) {
        Text("Button")
      }
    }
  }
}

private data class Bean(var visible: Boolean, var text: String) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Bean

    if (visible != other.visible) return false
    if (text != other.text) return false

    return true
  }

  override fun hashCode(): Int {
    var result = visible.hashCode()
    result = 31 * result + text.hashCode()
    return result
  }
}