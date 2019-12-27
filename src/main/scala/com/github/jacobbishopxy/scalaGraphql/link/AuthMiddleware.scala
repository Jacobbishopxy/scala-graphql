package com.github.jacobbishopxy.scalaGraphql.link

import sangria.execution.{BeforeFieldResult, FieldTag, Middleware, MiddlewareBeforeField, MiddlewareQueryContext}
import sangria.schema.Context

/**
 * Created by Jacob Xie on 12/27/2019
 */
object AuthMiddleware extends Middleware[ContextDef] with MiddlewareBeforeField[ContextDef] {
  case object Authorised extends FieldTag

  override type QueryVal = Unit
  override type FieldVal = Unit

  override def beforeQuery(context: MiddlewareQueryContext[ContextDef, _, _]): Unit = ()

  override def afterQuery(queryVal: QueryVal, context: MiddlewareQueryContext[ContextDef, _, _]): Unit = ()

  override def beforeField(queryVal: QueryVal,
                           mctx: MiddlewareQueryContext[ContextDef, _, _],
                           ctx: Context[ContextDef, _]): BeforeFieldResult[ContextDef, Unit] = {
    val requireAuth = ctx.field.tags contains Authorised

    if(requireAuth) ctx.ctx.ensureAuthenticated()

    continue
  }
}
