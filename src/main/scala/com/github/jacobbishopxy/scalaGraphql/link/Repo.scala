package com.github.jacobbishopxy.scalaGraphql.link


import java.sql.Timestamp

import akka.http.scaladsl.model.DateTime
import sangria.execution.deferred.{HasId, RelationIds, SimpleRelation}
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.lifted.{ForeignKeyQuery, ProvenShape}
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps


/**
 * Created by Jacob Xie on 12/26/2019
 */
class Repo(db: Database) {

  import Repo._

  def allLinks: Future[Seq[Link]] = db.run(Links.result)

  def getLinks(ids: Seq[Int]): Future[Seq[Link]] = {
    db.run(
      Links.filter(_.id inSet ids).result
    )
  }

  def getLinksByUserIds(ids: Seq[Int]): Future[Seq[Link]] = {
    db.run {
      Links.filter(_.postedBy inSet ids).result
    }
  }

  def getUsers(ids: Seq[Int]): Future[Seq[User]] = {
    db.run(
      Users.filter(_.id inSet ids).result
    )
  }

  def getVotes(ids: Seq[Int]): Future[Seq[Vote]] = {
    db.run(
      Votes.filter(_.id inSet ids).result
    )
  }

  def getVotesByRelationIds(rel: RelationIds[Vote]): Future[Seq[Vote]] = {
    implicit object IntMarker

    db.run(
      Votes.filter { vote =>
        rel.rawIds.collect({
          case (SimpleRelation("byUser"), ids: Seq[Int]@unchecked) => vote.userId inSet ids
          case (SimpleRelation("byLink"), ids: Seq[Int]@unchecked) => vote.linkId inSet ids
        }).foldLeft(true: Rep[Boolean])(_ || _)

      } result
    )
  }


  def createUser(name: String, authProvider: AuthProviderSignUpData): Future[User] = {
    val newUser = User(0, name, authProvider.data.email, authProvider.data.password)

    val insertAndReturnUserQuery = (Users returning Users.map(_.id)) into {
      (user, id) => user.copy(id = id)
    }

    db.run {
      insertAndReturnUserQuery += newUser
    }

  }

  def createLink(url: String, description: String, postedBy: Int): Future[Link] = {

    val insertAndReturnLinkQuery = (Links returning Links.map(_.id)) into {
      (link, id) => link.copy(id = id)
    }
    db.run {
      insertAndReturnLinkQuery += Link(0, url, description, postedBy)
    }
  }

  def createVote(linkId: Int, userId: Int): Future[Vote] = {
    val insertAndReturnVoteQuery = (Votes returning Votes.map(_.id)) into {
      (vote, id) => vote.copy(id = id)
    }
    db.run {
      insertAndReturnVoteQuery += Vote(0, userId, linkId)
    }
  }

  def authenticate(email: String, password: String): Future[Option[User]] = db.run {
    Users.filter(u => u.email === email && u.password === password).result.headOption
  }

}

object Repo {

  implicit val dateTimeColumnType: JdbcType[DateTime] with BaseTypedType[DateTime] =
    MappedColumnType.base[DateTime, Timestamp](
      dt => new Timestamp(dt.clicks),
      ts => DateTime(ts.getTime)
    )

  trait Identifiable {
    def id: Int
  }

  object Identifiable {
    implicit def hasId[T <: Identifiable]: HasId[T, Int] = HasId(_.id)
  }

  case class Link(id: Int,
                  url: String,
                  description: String,
                  postedBy: Int,
                  createdAt: DateTime = DateTime.now) extends Identifiable

  case class User(id: Int,
                  name: String,
                  email: String,
                  password: String,
                  createdAt: DateTime = DateTime.now) extends Identifiable

  case class Vote(id: Int,
                  userId: Int,
                  linkId: Int,
                  createdAt: DateTime = DateTime.now) extends Identifiable

  case class AuthProviderEmail(email: String, password: String)

  case class AuthProviderSignUpData(data: AuthProviderEmail)

  case class AuthenticationException(message: String) extends Exception(message)

  case class AuthorisationException(message: String) extends Exception(message)


  class LinksTable(tag: Tag) extends Table[Link](tag, "LINKS") {

    def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def url: Rep[String] = column[String]("URL")
    def description: Rep[String] = column[String]("DESCRIPTION")
    def postedBy: Rep[Int] = column[Int]("USER_ID")
    def createdAt: Rep[DateTime] = column[DateTime]("CREATED_AT")

    def postedByFK: ForeignKeyQuery[UsersTable, User] = foreignKey("postedBy_FK", postedBy, Users)(_.id)

    override def * : ProvenShape[Link] = (id, url, description, postedBy, createdAt).mapTo[Link]
  }

  val Links = TableQuery[LinksTable]

  class UsersTable(tag: Tag) extends Table[User](tag, "USERS") {
    def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name: Rep[String] = column[String]("NAME")
    def email: Rep[String] = column[String]("EMAIL")
    def password: Rep[String] = column[String]("PASSWORD")
    def createdAt: Rep[DateTime] = column[DateTime]("CREATED_AT")

    override def * : ProvenShape[User] = (id, name, email, password, createdAt).mapTo[User]
  }

  val Users = TableQuery[UsersTable]

  class VotesTable(tag: Tag) extends Table[Vote](tag, "VOTES") {
    def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def userId: Rep[Int] = column[Int]("USER_ID")
    def linkId: Rep[Int] = column[Int]("LINK_ID")
    def createdAt: Rep[DateTime] = column[DateTime]("CREATED_AT")

    def userFK: ForeignKeyQuery[UsersTable, User] = foreignKey("user_FK", userId, Users)(_.id)
    def linkFK: ForeignKeyQuery[LinksTable, Link] = foreignKey("link_FK", linkId, Links)(_.id)

    override def * : ProvenShape[Vote] = (id, userId, linkId, createdAt).mapTo[Vote]
  }

  val Votes = TableQuery[VotesTable]


  val InitialDatabaseSetup =
    DBIO.seq(
      (Links.schema ++ Users.schema ++ Votes.schema).create,

      Users ++= Seq(
        User(1, "mario", "mario@example.com", "s3cr3t"),
        User(2, "Fred", "fred@flinstones.com", "wilmalove")
      ),

      Links ++= Seq(
        Link(1, "http://howtographql.com", "Awesome community driven GraphQL tutorial", 1, DateTime(2017, 9, 12)),
        Link(2, "http://graphql.org", "Official GraphQL webpage", 1, DateTime(2017, 10, 1)),
        Link(3, "https://facebook.github.io/graphql/", "GraphQL specification", 2, DateTime(2017, 10, 2))
      ),

      Votes ++= Seq(
        Vote(1, 1, 1),
        Vote(2, 1, 2),
        Vote(3, 1, 3),
        Vote(4, 2, 2)
      )
    )


  def createDatabase(): Repo = {
    val db = Database.forConfig("h2mem")

    Await.result(db.run(InitialDatabaseSetup), 10.seconds)

    new Repo(db)
  }

}



