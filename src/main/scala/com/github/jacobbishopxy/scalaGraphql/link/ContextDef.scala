package com.github.jacobbishopxy.scalaGraphql.link

import Repo.{AuthenticationException, AuthorisationException, User}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by Jacob Xie on 12/26/2019
 */
case class ContextDef(repo: Repo, currentUser: Option[User]=None) {

  def login(email: String, password: String): User = {
    val userOpt = Await.result(repo.authenticate(email, password), 10.seconds)

    userOpt.getOrElse(
      throw AuthenticationException("Email or password are incorrect!")
    )
  }

  def ensureAuthenticated(): Unit = if (currentUser.isEmpty)
    throw AuthorisationException("You do not have permission. Please sign in.")
}
