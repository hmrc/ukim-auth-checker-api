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

package connectors

import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.{
  DatedAuthorisationRequest,
  PdsAuthCheckerRequest,
  PdsAuthCheckerResponse,
  ValidationErrorResponse
}
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{JsResult, Json}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{
  HeaderCarrier,
  HttpResponse,
  StringContextOps,
  UpstreamErrorResponse
}
import uk.gov.hmrc.ukimauthcheckerapi.config.AppConfig

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[PdsAuthCheckerConnectorImpl])
trait PdsAuthCheckerConnector {
  def check(request: DatedAuthorisationRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext
  ): Future[Either[ValidationErrorResponse, PdsAuthCheckerResponse]]
}

@Singleton
class PdsAuthCheckerConnectorImpl @Inject() (
    httpClientV2: HttpClientV2,
    appConfig: AppConfig
) extends PdsAuthCheckerConnector {
  def check(request: DatedAuthorisationRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext
  ): Future[Either[ValidationErrorResponse, PdsAuthCheckerResponse]] = {

    val authType = "UKIM"
    val url = appConfig.pdsAuthCheckerUrl.addPathParts("authorisations")
    val pdsRequest =
      PdsAuthCheckerRequest(request.date, authType, request.eoris)
    httpClientV2
      .post(url"$url")
      .withBody(Json.toJson(pdsRequest))
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case OK =>
            response.json
              .validate[PdsAuthCheckerResponse]
              .map(result => Future.successful(Right(result)))
              .recoverTotal(error => Future.failed(JsResult.Exception(error)))
          case BAD_REQUEST =>
            response.json
              .validate[ValidationErrorResponse]
              .map(result => Future.successful(Left(result)))
              .recoverTotal(error => Future.failed(JsResult.Exception(error)))
          case _ =>
            Future.failed(UpstreamErrorResponse(response.body, response.status))
        }
      }
  }
}
