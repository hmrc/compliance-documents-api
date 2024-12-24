/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package scala.utils

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json
import utils.LoggerHelper

class LoggerHelperSpec extends AnyFunSuite with Matchers {

  test("logProcess should log with all parameters provided") {
    val className = "TestLogger"
    val methodName = "testMethod"
    val message = "This is a test message"
    val correlationId = Some("12345")
    val docSize = Some(Json.parse("""{"documentBinary":"abcdef"}"""))

    val result = LoggerHelper.logProcess(className, methodName, message, correlationId, docSize)

    result should be ("[TestLogger][testMethod] This is a test message, correlationId: 12345, document size: 6")
  }

  test("logProcess should log without correlationId and docSize") {
    val className = "TestLogger"
    val methodName = "testMethod"
    val message = "This is a test message"

    val result = LoggerHelper.logProcess(className, methodName, message)

    result should be ("[TestLogger][testMethod] This is a test message")
  }

  test("logProcess should log with correlationId but without docSize") {
    val className = "TestLogger"
    val methodName = "testMethod"
    val message = "This is a test message"
    val correlationId = Some("12345")

    val result = LoggerHelper.logProcess(className, methodName, message, correlationId)

    result should be ("[TestLogger][testMethod] This is a test message, correlationId: 12345")
  }

  test("logProcess should log with docSize but without correlationId") {
    val className = "TestLogger"
    val methodName = "testMethod"
    val message = "This is a test message"
    val docSize = Some(Json.parse("""{"documentBinary":"abcdef"}"""))

    val result = LoggerHelper.logProcess(className, methodName, message, None, docSize)

    result should be ("[TestLogger][testMethod] This is a test message, document size: 6")
  }

  test("getSize should return None if docSize is None") {
    val result = LoggerHelper.getSize(None)
    result should be (None)
  }

  test("getSize should return None if documentBinary is not present") {
    val docSize = Some(Json.parse("""{"otherKey":"value"}"""))
    val result = LoggerHelper.getSize(docSize)
    result should be (None)
  }

  test("getSize should return length of documentBinary if present") {
    val docSize = Some(Json.parse("""{"documentBinary":"abcdef"}"""))
    val result = LoggerHelper.getSize(docSize)
    result should be (Some("6"))
  }

  test("addLogOrNothing should return empty string if documentProperty is None") {
    val result = LoggerHelper.addLogOrNothing("testProp", None)
    result should be ("")
  }

  test("addLogOrNothing should return formatted string if documentProperty is Some") {
    val result = LoggerHelper.addLogOrNothing("testProp", Some("value"))
    result should be (", testProp: value")
  }
}

