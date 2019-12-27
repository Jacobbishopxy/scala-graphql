package com.github.jacobbishopxy.scalaGraphql.people

import slick.jdbc.H2Profile.api._
import slick.lifted.{ForeignKeyQuery, Index, ProvenShape}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

/**
 * Created by Jacob Xie on 12/25/2019
 */
class Repo(db: Database) {

  import Repo._

  private def friendsQuery(personIds: Seq[String]): Query[(FriendTable, PersonTable), (Friend, Person), Seq] =
    Friends.filter(_.personId inSet personIds)
      .join(People).on(_.friendId === _.id)

  def allPeople: Future[Seq[Person]] =
    db.run(People.result)

  def people(ids: Seq[String]): Future[Seq[Person]] =
    db.run(People.filter(_.id inSet ids).result)

  def person(id: String): Future[Option[Person]] =
    db.run(People.filter(_.id === id).result.headOption)

  def findFriends(personIds: Seq[String]): Future[Vector[(Seq[String], Person)]] =
    db.run(friendsQuery(personIds).result).map(result ⇒
      result.groupBy(_._2.id).toVector.map {
        case (_, friends) ⇒ friends.map(_._1.personId) → friends.head._2
      })

  def close(): Unit = db.close()
}

object Repo {

  /**
   * models
   */

  case class Person(id: String, firstName: String, lastName: String, username: String, email: String)

  case class Friend(personId: String, friendId: String)


  /**
   * tables
   */

  class PersonTable(tag: Tag) extends Table[Person](tag, "PEOPLE") {
    def id: Rep[String] = column[String]("PERSON_ID", O.PrimaryKey)
    def firstName: Rep[String] = column[String]("FIRST_NAME")
    def lastName: Rep[String] = column[String]("LAST_NAME")
    def username: Rep[String] = column[String]("USERNAME")
    def email: Rep[String] = column[String]("EMAIL")

    override def * : ProvenShape[Person] = (id, firstName, lastName, username, email) <> ((Person.apply _).tupled, Person.unapply)
  }

  val People = TableQuery[PersonTable]

  class FriendTable(tag: Tag) extends Table[Friend](tag, "FRIENDS") {
    def personId: Rep[String] = column[String]("PERSON_ID")
    def friendId: Rep[String] = column[String]("FRIEND_ID")

    def person: ForeignKeyQuery[PersonTable, Person] = foreignKey("PERSON_FK", personId, People)(_.id)
    def friend: ForeignKeyQuery[PersonTable, Person] = foreignKey("FRIEND_FK", friendId, People)(_.id)
    def idx: Index = index("UNIQUE_IDX", (personId, friendId), unique = true)

    override def * : ProvenShape[Friend] = (personId, friendId) <> ((Friend.apply _).tupled, Friend.unapply)
  }

  val Friends = TableQuery[FriendTable]


  /**
   * table initialize
   */

  val InitialDatabaseSetup =
    DBIO.seq(
      (People.schema ++ Friends.schema).create,

      People ++= Seq(
        Person("1000", "Brianna", "Stephenson", "brianna.stephenson", "justo.eu@Lorem.edu"),
        Person("1001", "Leslie", "Vasquez", "leslie.vasquez", "eu@Duiscursusdiam.co.uk"),
        Person("1002", "Garrison", "Douglas", "garrison.douglas", "rutrum@tristiquesenectuset.com"),
        Person("1003", "Jena", "Brady", "jena.brady", "blandit.Nam@Inat.ca"),
        Person("1004", "Evan", "Cain", "evan.cain", "sem.ut.dolor@etarcu.co.uk"),
        Person("1005", "Alexandra", "Evans", "alexandra.evans", "nisi.Mauris@Fuscealiquet.co.uk"),
        Person("1006", "Nigel", "May", "nigel.may", "semper.et@metussitamet.ca")
      ),

      Friends ++= Seq(
        Friend("1000", "1001"),
        Friend("1000", "1004"),
        Friend("1000", "1006"),
        Friend("1001", "1003"),
        Friend("1001", "1005"),
        Friend("1002", "1004"),
        Friend("1003", "1005"),
        Friend("1003", "1006"),
        Friend("1005", "1001"),
        Friend("1005", "1004")
      )
    )

  def createDatabase(): Repo = {
    val db = Database.forConfig("h2mem")

    Await.result(db.run(InitialDatabaseSetup), 10 seconds)

    new Repo(db)
  }

}
