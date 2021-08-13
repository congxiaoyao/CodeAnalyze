package com.congxiaoyao.lang

import java.io.File

fun main() {
//  val content = File("src/main/resources/case.txt").readText()
  val content = File("src/main/resources/BananaUserLoginActivity.java").readText()
//  val content = File("src/main/resources/BananaConfirmLoanFragmentV2.java").readText()
//  val content = File("src/main/resources/MaterialProgressBar.java").readText()

  val tokens = Lexer(content).toTokenList()
  val parser = Parser(tokens)
  val jClass = parser.readJClass()!!
  jClass.parseBody()

  val method = jClass.methods.first { it.name == "sendRequestUserLogin" }
  method.callingMethod().forEach { println(it) }

}

class JClass(val name: String) {
  val body = mutableListOf<Token>()
  val methods = mutableListOf<JMethod>()
  val innerClass = mutableListOf<JClass>()
  val fields = mutableListOf<JField>()

  fun parseBody() {
    fillInnerClass()
    fillMethods()
    fillFields()
    innerClass.forEach { it.parseBody() }
  }

  private fun fillInnerClass() {
    val parser = Parser(body)
    val removeRanges = mutableListOf<IntRange>()
    while (true) {
      val inner = parser.readJClass() ?: break
      val range = parser.resultRange
      removeRanges += (range.first until range.second)
      innerClass += inner
      inner.fillInnerClass()
    }
    val newBody = body.filterIndexed { index, _ ->
      !removeRanges.any { index in it }
    }
    body.clear()
    body.addAll(newBody)
  }

  private fun fillMethods() {
    val parser = Parser(body)
    while (true) {
      val method = parser.readJMethod() ?: break
      methods += method
    }
  }

  private fun fillFields() {
    val parse = Parser(body)
    while (true) {
      val field = parse.readField() ?: break
      fields += field
    }
  }
}

class JMethod(val name: String) {
  val params = mutableListOf<Token>()
  val body = mutableListOf<Token>()

  fun callingMethod(): List<String> {
    val parse = Parser(body)
    val result = mutableListOf<String>()
    while (true) {
      val call = parse.readMethodCall() ?: break
      result += call
    }
    return result
  }
}

class JField(val name: String, val type: String) {
  val initializer = mutableListOf<Token>()
}

class Parser(private val tokens: List<Token>) {
  private var pointer: Int = 0
  private val token get() = tokens.getOrNull(pointer)
  private fun token(n: Int) = tokens.getOrNull(pointer + n)
  private val hasToken get() = pointer < tokens.size

  var resultRange = 0 to 0
    private set

  private var level = 0

  fun readField(): JField? {
    if (!hasToken) return null
    if (token is LeftBracket) level++
    if (token is RightBracket)  level--
    if (level > 0) {
      skip()
      return readField()
    }
    if (token !is Name) {
      skip()
      return readField()
    }
    val type = (token as Name).value
    skip()
    skipContinuousSpaceAndNewLine()
    if (token !is Name) {
      skip()
      return readField()
    }
    val name = (token as Name).value
    skip()
    skipContinuousSpaceAndNewLine()
    if (token is Eq) {
      val field = JField(name, type)
      skip()
      skipContinuousSpaceAndNewLine()
      var leftBracketCount = 0
      do {
        if (token is LeftBracket) {
          leftBracketCount++
          level++
        }
        else if (token is RightBracket) {
          leftBracketCount--
          level--
        }
        field.initializer += token!!
        skip()
      } while (hasToken && (leftBracketCount > 0 || (leftBracketCount == 0 && token !is Semi)))
      return field
    } else if (token is Semi) {
      return JField(name, type)
    } else {
      return null
    }
  }

  fun readMethodCall(): String? {
    val name = readToAnyName() ?: return null
    val namePointer = pointer
    skip()
    skipContinuousSpaceAndNewLine()
    if (token !is LeftPth) return readMethodCall()
    var leftPthCount = 0
    do {
      if (token is LeftPth) leftPthCount++
      else if (token is RightPth) leftPthCount--
      skip()
    } while (hasToken && leftPthCount > 0)
    skipContinuousSpaceAndNewLine()
    if (token is LeftBracket) return readMethodCall()
    pointer = namePointer
    var isLocalMethodCall = false
    do pointer-- while (token is Space || token is NewLine)
    if (token is Dot) {
      do pointer-- while (token is Space || token is NewLine)
      if (token == KeyWord.SUPER || token == KeyWord.THIS) {
        isLocalMethodCall = true
      }
    } else {
      isLocalMethodCall = true
    }
    pointer = namePointer + 1
    return if (isLocalMethodCall) {
      name
    } else {
      readMethodCall()
    }
  }

  fun readJClass(): JClass? {
    if (!readToToken(KeyWord.CLASS)) return null

    do pointer-- while (token is Space || token is NewLine)
    if (token is Dot) {
      readToToken(KeyWord.CLASS)
      skip()
      return readJClass()
    }

    var offset = 0
    while (token(offset) == KeyWord.CLASS ||
      token(offset) == KeyWord.PRIVATE ||
      token(offset) == KeyWord.PUBLIC ||
      token(offset) == KeyWord.FINAL ||
      token(offset) == KeyWord.STATIC
    ) {
      offset--
      do offset-- while (token(offset) is Space || token(offset) is NewLine)
    }

    resultRange = (offset + 1 + pointer) to 0

    val className = readToAnyName() ?: return null
    if (!readToToken(LeftBracket)) return null
    val jClass = JClass(className)
    var leftBracketCount = 0
    do {
      if (token is LeftBracket) leftBracketCount++
      else if (token is RightBracket) leftBracketCount--
      jClass.body += token!!
      skip()
    } while (hasToken && leftBracketCount > 0)
    resultRange = resultRange.first to pointer

    jClass.body.removeFirst()
    jClass.body.removeLast()

    return jClass
  }

  fun readJMethod(): JMethod? {
    //返回值
    readToAnyName() ?: return null;skip()

    //空格
    if (token !is Space && token !is NewLine) return readJMethod()
    skipContinuousSpaceAndNewLine()
    //方法名
    val methodName = ((token as? Name)?.value ?: return readJMethod()); skip()
    val jMethod = JMethod(methodName)
    //空格
    skipContinuousSpaceAndNewLine()
    //左括号
    if (token !is LeftPth) return readJMethod();skip()
    //参数定义
    while (token !is RightPth) {
      if (!hasToken) return readJMethod()
      jMethod.params.add(token!!)
      skip()
    }
    skip()
    skipContinuousSpaceAndNewLine()
    if (token !is LeftBracket) return readJMethod();
    var leftBracketCount = 0
    do {
      if (token is LeftBracket) leftBracketCount++
      else if (token is RightBracket) leftBracketCount--
      jMethod.body += token!!
      skip()
    } while (hasToken && leftBracketCount > 0)

    jMethod.body.removeFirst()
    jMethod.body.removeLast()

    return jMethod
  }

  private fun skipContinuousSpaceAndNewLine() {
    while (token is Space || token is NewLine) skip()
  }

  private fun readToToken(target: Token): Boolean {
    val oldPointer = pointer
    while (hasToken) {
      if (token == target) {
        return true
      } else {
        skip()
      }
    }
    pointer = oldPointer
    return false
  }

  private fun readToAnyName(): String? {
    val oldPointer = pointer
    while (hasToken) {
      if (token is Name) {
        return token!!.value
      } else {
        skip()
      }
    }
    pointer = oldPointer
    return null
  }

  private fun skip(n: Int = 1) {
    pointer += n
  }
}