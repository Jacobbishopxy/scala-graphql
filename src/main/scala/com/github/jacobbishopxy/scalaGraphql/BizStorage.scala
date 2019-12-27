package com.github.jacobbishopxy.scalaGraphql


import link.{
  SchemaDef => SchemaDefLink,
  ContextDef => ContextDefLink,
  Repo => RepoLink,
  AuthMiddleware => AuthLink
}


/**
 * Created by Jacob Xie on 12/25/2019
 */
object BizStorage {


  val schemaLink = SchemaDefLink.SchemaDefinition
  val deferredResolverLink = SchemaDefLink.Resolver
  val exceptionHandlerLink = SchemaDefLink.ErrorHandler

  val repoLink = ContextDefLink(RepoLink.createDatabase())

  val authLink = AuthLink

}






































