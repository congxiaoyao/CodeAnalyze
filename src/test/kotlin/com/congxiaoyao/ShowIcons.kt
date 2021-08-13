package com.congxiaoyao

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import org.reflections.Reflections
import org.reflections.scanners.*
import java.util.*


fun main() = singleWindowApplication(title = "内置图标") {
  val reflections = Reflections(
    "androidx.compose.material.icons.filled",
    Arrays.asList(
      SubTypesScanner(false),
      MethodParameterNamesScanner(),
      MethodAnnotationsScanner(),
      MemberUsageScanner(),
      TypeAnnotationsScanner()
    )
  )

  val dataGroup = reflections.getSubTypesOf(Any::class.java)
    .map {
      val method = it.declaredMethods.first()
      method.name.replaceFirst("get", "") to (method.invoke(it, Icons.Filled) as ImageVector)
    }.withIndex().groupBy {
      it.index / 6
    }.mapValues { it.value.map { it.value } }
    .map { it.value }
  MaterialTheme {
    Box {
      val listState = rememberLazyListState()
      VerticalScrollbar(rememberScrollbarAdapter(listState),Modifier.align(Alignment.TopEnd))
      LazyColumn(state = listState) {
        items(dataGroup) {
          Spacer(Modifier.height(8.dp))
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            it.forEach {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(125.dp)
              ) {
                Spacer(Modifier.height(16.dp))
                Card(Modifier.wrapContentSize()) {
                  Image(it.second, null, Modifier.clickable { }.padding(12.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(it.first, style = typography.caption)
              }
            }
          }
          Spacer(Modifier.height(8.dp))
        }
      }
    }
  }
}
