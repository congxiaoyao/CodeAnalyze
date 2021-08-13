package com.congxiaoyao

import com.google.gson.Gson
import com.google.gson.JsonDeserializer
import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.awt.Rectangle
import java.io.File
import java.util.*

class MethodStore(
  val methodGraph: MethodGraph,
  @JsonAdapter(BoundListJsonAdapter::class)
  val boundList: List<Rectangle>
) {
  companion object {
    fun from(file: File): MethodStore {
      return Gson().fromJson(file.bufferedReader(), MethodStore::class.java)
    }

//    fun save(frame: MainFrame, file: File) {
//      val graph = frame.graph
//      val locations = frame.panel.getAdapter().renderers.map { it.bounds }
//      val store = MethodStore(graph, locations)
//      file.writeText(Gson().toJson(store))
//    }
  }

  class BoundListJsonAdapter : TypeAdapter<List<Rectangle>>() {

    override fun write(out: JsonWriter, value: List<Rectangle>): Unit = with(out) {
      beginArray()
      value.forEach {
        value(it.x)
        value(it.y)
        value(it.width)
        value(it.height)
      }
      endArray()
    }

    override fun read(reader: JsonReader): List<Rectangle> {
      val result = mutableListOf<Rectangle>()
      reader.beginArray()
      while (reader.peek() != JsonToken.END_ARRAY) {
        result += Rectangle(reader.nextInt(), reader.nextInt(), reader.nextInt(), reader.nextInt())
      }
      reader.endArray()
      return result
    }
  }

  class MatrixJsonAdapter : TypeAdapter<Array<BooleanArray>>() {
    override fun write(out: JsonWriter, value: Array<BooleanArray>?) {
      out.beginObject()
      out.name("size")
      out.value(value?.size ?: 0)
      out.name("matrix")
      out.beginArray()
      (value ?: emptyArray()).map { array ->
        BitSet().apply {
          repeat(array.size) {
            this[it] = array[it]
          }
        }.toLongArray()
      }.forEach {
        out.beginArray()
        it.forEach { out.value(it) }
        out.endArray()
      }
      out.endArray()
      out.endObject()
    }

    override fun read(reader: JsonReader): Array<BooleanArray> {
      reader.beginObject()
      check(reader.nextName() == "size")
      val size = reader.nextInt()
      check(reader.nextName() == "matrix")
      reader.beginArray()
      val matrix = Array(size) {
        reader.beginArray()
        val raw = mutableListOf<Long>()
        while (reader.peek() != JsonToken.END_ARRAY) {
          raw += reader.nextLong()
        }
        reader.endArray()
        val bitSet = BitSet.valueOf(raw.toLongArray())
        BooleanArray(size) { bitSet[it] }
      }
      reader.endArray()
      reader.endObject()
      return matrix
    }
  }
}