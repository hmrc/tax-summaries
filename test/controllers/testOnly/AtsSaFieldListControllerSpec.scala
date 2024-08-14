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

package controllers.testOnly

import play.api.http.Status._
import play.api.libs.json.{JsArray, JsObject}
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status, stubControllerComponents}
import utils.BaseSpec

import scala.concurrent.ExecutionContext

class AtsSaFieldListControllerSpec extends BaseSpec {

  private lazy val cc: ControllerComponents = stubControllerComponents()

  private implicit lazy val ec: ExecutionContext = inject[ExecutionContext]

  private val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  private val controller = new AtsSaFieldListController(
    cc
  )

  "getFieldList" must {
    "return 200 + correct no of fields for 2022" in {
      val result      = controller.getFieldList(2022)(request)
      status(result) mustBe OK
      val actual      = contentAsJson(result).as[JsObject]
      val actualArray = (actual \ "items").as[JsArray]
      actualArray.value.size mustBe 132
    }
    "return 200 + correct no of fields for 2023" in {
      val result      = controller.getFieldList(2023)(request)
      status(result) mustBe OK
      val actual      = contentAsJson(result).as[JsObject]
      val actualArray = (actual \ "items").as[JsArray]
      actualArray.value.size mustBe 132
    }
    "return 200 + correct no of fields for 2024" in {
      val result      = controller.getFieldList(2024)(request)
      status(result) mustBe OK
      val actual      = contentAsJson(result).as[JsObject]
      val actualArray = (actual \ "items").as[JsArray]
      actualArray.value.size mustBe 133
    }

  }
}
