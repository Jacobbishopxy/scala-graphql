package com.github.jacobbishopxy.scalaGraphql

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe._
import io.circe.optics.JsonPath._
import io.circe.parser._
import sangria.ast.Document
import sangria.execution.{ErrorWithResolver, QueryAnalysisError}
import sangria.marshalling.circe._
import sangria.parser.DeliveryScheme.Try
import sangria.parser.{QueryParser, SyntaxError}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}


/**
 * Created by Jacob Xie on 5/21/2020
 */
trait Service extends CorsSupport {
  import RequestUnmarshaller._

  implicit val system: ActorSystem = ActorSystem("sangria-server")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher


  val port: Int = 8088

  def executeGQL(query: Document,
                 operationName: Option[String],
                 variables: Json,
                 tracing: Boolean): Future[Json]

  private def executeGraphQL(query: Document,
                             operationName: Option[String],
                             variables: Json,
                             tracing: Boolean): StandardRoute =
    complete(
      executeGQL(query, operationName, variables, tracing)
        .map(OK → _)
        .recover {
          case error: QueryAnalysisError ⇒ BadRequest → error.resolveError
          case error: ErrorWithResolver ⇒ InternalServerError → error.resolveError
        }
    )

  private def formatError(error: Throwable): Json = error match {
    case syntaxError: SyntaxError ⇒
      Json.obj("errors" → Json.arr(
        Json.obj(
          "message" → Json.fromString(syntaxError.getMessage),
          "locations" → Json.arr(Json.obj(
            "line" → Json.fromBigInt(syntaxError.originalError.position.line),
            "column" → Json.fromBigInt(syntaxError.originalError.position.column))))))
    case NonFatal(e) ⇒
      formatError(e.getMessage)
    case e ⇒
      throw e
  }

  private def formatError(message: String): Json =
    Json.obj("errors" → Json.arr(Json.obj("message" → Json.fromString(message))))

  private val route: Route =
    optionalHeaderValueByName("X-Apollo-Tracing") { tracing ⇒
      path("graphql") {
        get {
          explicitlyAccepts(`text/html`) {
            getFromResource("assets/playground.html")
          } ~
            parameters('query, 'operationName.?, 'variables.?) { (query, operationName, variables) ⇒
              QueryParser.parse(query) match {
                case Success(ast) ⇒
                  variables.map(parse) match {
                    case Some(Left(error)) ⇒ complete(BadRequest, formatError(error))
                    case Some(Right(json)) ⇒ executeGraphQL(ast, operationName, json, tracing.isDefined)
                    case None ⇒ executeGraphQL(ast, operationName, Json.obj(), tracing.isDefined)
                  }
                case Failure(error) ⇒ complete(BadRequest, formatError(error))
              }
            }
        } ~
          post {
            parameters('query.?, 'operationName.?, 'variables.?) { (queryParam, operationNameParam, variablesParam) ⇒
              entity(as[Json]) { body ⇒
                val query = queryParam orElse root.query.string.getOption(body)
                val operationName = operationNameParam orElse root.operationName.string.getOption(body)
                val variablesStr = variablesParam orElse root.variables.string.getOption(body)

                query.map(QueryParser.parse(_)) match {
                  case Some(Success(ast)) ⇒
                    variablesStr.map(parse) match {
                      case Some(Left(error)) ⇒ complete(BadRequest, formatError(error))
                      case Some(Right(json)) ⇒ executeGraphQL(ast, operationName, json, tracing.isDefined)
                      case None ⇒ executeGraphQL(ast, operationName, root.variables.json.getOption(body) getOrElse Json.obj(), tracing.isDefined)
                    }
                  case Some(Failure(error)) ⇒ complete(BadRequest, formatError(error))
                  case None ⇒ complete(BadRequest, formatError("No query to execute"))
                }
              } ~
                entity(as[Document]) { document ⇒
                  variablesParam.map(parse) match {
                    case Some(Left(error)) ⇒ complete(BadRequest, formatError(error))
                    case Some(Right(json)) ⇒ executeGraphQL(document, operationNameParam, json, tracing.isDefined)
                    case None ⇒ executeGraphQL(document, operationNameParam, Json.obj(), tracing.isDefined)
                  }
                }
            }
          }
      }
    } ~
      (get & pathEndOrSingleSlash) {
        redirect("/graphql", PermanentRedirect)
      }


  def start(): Unit = {
    Http().bindAndHandle(
        corsHandler(route),
        "0.0.0.0",
        sys.props.get("http.port").fold(port)(_.toInt)
      )
    println(s"open a browser with URL: http://localhost:$port\n")
  }

}

