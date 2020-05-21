package com.github.jacobbishopxy.scalaGraphql

import sangria.ast.Document
import sangria.execution.Executor
import sangria.marshalling.circe._
import io.circe._
import slick.jdbc.H2Profile

import scala.concurrent.Future


/**
 * Created by Jacob Xie on 12/25/2019
 */
object Server extends App {

  val repo = new Repositories(H2Profile, "h2mem")
  val schemas = Schemas

  object Sev extends Service {

    override def executeGQL(query: Document,
                            operationName: Option[String],
                            variables: Json,
                            tracing: Boolean): Future[Json] =
      Executor.execute(
        schema = schemas.SD,
        queryAst = query,
        userContext = repo,
        variables = if (variables.isNull) Json.obj() else variables,
        operationName = operationName
      )
  }

  Sev.start()
}
