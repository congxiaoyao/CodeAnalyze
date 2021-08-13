package com.congxiaoyao.lang

import java.io.File

class Lexer(private val content: String) {
  private var pointer = 0
  private var hasQuot = false
  private var chars = mutableListOf<Char>()

  fun toTokenList(): List<Token> {
    val result = mutableListOf<Token>()
    while (pointer < content.length) {
      val keyword = readKeyWord()
      if (keyword != null) {
        result += KeyWord(keyword)
        continue
      }
      val symbol = readSymbol()
      if (symbol != null) {
        when (symbol) {
          is Space -> dropContinuousSpace()
          is NewLine -> dropContinuousNewLine()
          is Quot -> {
            if (hasQuot) {
              result += Name(String(chars.toCharArray()))
              chars.clear()
              hasQuot = false
            } else {
              hasQuot = true
            }
          }
        }
        if (chars.isNotEmpty()) {
          result += Name(String(chars.toCharArray()))
          chars.clear()
        }
        result += symbol
        continue
      }

      chars += content[pointer++]
    }
    return result
  }

  private fun dropContinuousSpace() {
    while (pointer < content.length && content[pointer] == ' ') {
      pointer++
    }
  }

  private fun dropContinuousNewLine() {
    while (pointer < content.length && content[pointer] == '\n') {
      pointer++
    }
  }

  private fun readKeyWord(): String? {
    val keyword = KeyWord.values.firstOrNull {
      val start = pointer
      val end = pointer + it.length
      if (end <= content.length) it == content.substring(start, end) else false
    } ?: return null
    val oldPointer = pointer
    skip(keyword.length)
    val singleCharToken = readSymbol()
    if (singleCharToken == null) {
      pointer = oldPointer
      return null
    }
    pointer -= 1
    return keyword
  }

  private fun readSymbol(): Token? {
    val ch = content[pointer]
    return singleCharTokens.firstOrNull { ch == it.value[0] }?.also { skip(1) }
  }

  private fun skip(n: Int) {
    pointer += n
  }

  private fun skip(token: Token) {
    pointer += token.value.length
  }
}

sealed class Token(val value: String) {
  override fun toString(): String {
    return "${this.javaClass.simpleName}($value)"
  }
}

object Dot : Token(".")
object Comma : Token(",")
object Quot : Token("\"")
object Semi : Token(";")
object Eq : Token("=")
object LeftPth : Token("(")
object RightPth : Token(")")
object LeftBracket : Token("{")
object RightBracket : Token("}")
object At : Token("@")
object Slash : Token("/")
object Star : Token("*")
object Space : Token(" ")
object Minus : Token("-")
object Not : Token("!")
object NewLine : Token("\n") {
  override fun toString() = "NewLine"
}

private val singleCharTokens = arrayOf(
  Dot, Comma, Quot, Semi, LeftPth, RightPth, NewLine,
  LeftBracket, RightBracket, At, Slash, Star, Space,
  Minus, Eq, Not
)

class KeyWord(value: String) : Token(value) {

  companion object {
    val values = arrayOf(
      "public", "private", "protected", "super", "static", "implements",
      "volatile", "import", "extends", "class", "new", "return", "try",
      "catch", "if", "else", "package", "final", "synchronized", "->", "::",
      "for","switch","while","break","continue"
    )

    val CLASS = KeyWord("class")
    val PUBLIC = KeyWord("public")
    val PRIVATE = KeyWord("private")
    val STATIC = KeyWord("static")
    val FINAL = KeyWord("final")
    val SYNCHRONIZED = KeyWord("synchronized")
    val SUPER = KeyWord("super")
    val THIS = KeyWord("this")
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    return this.value == (other as KeyWord).value
  }

  override fun hashCode(): Int {
    return javaClass.hashCode()
  }

}

class Name(value: String) : Token(value) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    return this.value == (other as Name).value
  }

  override fun hashCode(): Int {
    return javaClass.hashCode()
  }
}


fun main() {
//  val content = File("src/main/resources/case.txt").readText()
  val content = File("src/main/resources/BananaUserLoginActivity.java").readText()
  val tokens = Lexer(content).toTokenList()
//  tokens.map { it.value }.forEach { print(it) }
//  println()
  tokens.forEach {
    println(it)
  }
}
