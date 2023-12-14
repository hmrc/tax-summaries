/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.auth

import play.api.mvc.{AnyContent, BodyParser, ControllerComponents, Request, Result}
import play.api.test.Helpers.stubControllerComponents

import scala.concurrent.{ExecutionContext, Future}

object FakePertaxAuthAction extends AuthAction with PayeAuthAction {
  override protected def filter[A](request: Request[A]): Future[Option[Result]] = Future.successful(None)

  lazy val cc: ControllerComponents = stubControllerComponents()

  override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

  override protected def executionContext: ExecutionContext = cc.executionContext
}
