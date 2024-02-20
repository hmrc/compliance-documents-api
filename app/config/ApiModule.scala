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

package config

import com.google.inject.AbstractModule
import connectors.ComplianceDocumentsConnector
import controllers.VatRepaymentApiController
import controllers.actions.{AuthenticateApplicationAction, ValidateCorrelationIdHeaderAction}
import controllers.definition.ApiDocumentationController
import services.{ResourceService, ValidationService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector

class ApiModule extends AbstractModule {

  lazy val connectorBindings: Unit = {
    bind(classOf[ComplianceDocumentsConnector]).asEagerSingleton()
    bind(classOf[AuthConnector]).to(classOf[DefaultAuthConnector]).asEagerSingleton()
  }

  lazy val controllerBindings: Unit = {
    bind(classOf[VatRepaymentApiController]).asEagerSingleton()
    bind(classOf[ApiDocumentationController]).asEagerSingleton()
  }

  lazy val actionBindings: Unit = {
    bind(classOf[AuthenticateApplicationAction]).asEagerSingleton()
    bind(classOf[ValidateCorrelationIdHeaderAction]).asEagerSingleton()
  }

  lazy val serviceBindings: Unit = {
    bind(classOf[ResourceService]).asEagerSingleton()
    bind(classOf[ValidationService]).asEagerSingleton()
  }

  override def configure(): Unit = {
    actionBindings
    connectorBindings
    controllerBindings
    serviceBindings
  }
}
