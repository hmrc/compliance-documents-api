/*
 * Copyright 2020 HM Revenue & Customs
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

package utils

import play.api.libs.json.{JsNull, JsObject, JsSuccess, JsValue}

object LoggerHelper {
  def logProcess(className: String,
                 methodName: String,
                 message: String,
                 correlationId: Option[String] = None,
                 docSize: Option[JsValue] = None,
                 requestId: Option[Long] = None): String = {
    s"In class $className, method $methodName: \n$message${
      addLogOrNothing("correlationId", correlationId)
    }${
      addLogOrNothing("document size", getSize(docSize))
    }${
      addLongOrNothing("request ID", requestId)
    }"
  }

  def addLogOrNothing(propName: String, documentProperty: Option[String]): String = {
    if (documentProperty.isDefined) s", $propName: ${documentProperty.get}" else ""
  }

  def addLongOrNothing(propName: String, documentProperty: Option[Long]): String = {
    if (documentProperty.isDefined) s", $propName: ${documentProperty.get}" else ""
  }

  def getSize(document: Option[JsValue]): Option[String] = {
    if (document.isDefined && !(document.get == JsNull) && document.isInstanceOf[Option[JsObject]]) {
      (document.get.asInstanceOf[JsObject] \ "documentBinary").validate[String] match {
        case JsSuccess(value, _) => Some(value.length.toString)
        case _ => None
      }
    } else {
      None
    }
  }
}
