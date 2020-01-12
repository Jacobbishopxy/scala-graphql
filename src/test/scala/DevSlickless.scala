import slick.jdbc.H2Profile.api._
import shapeless.{::, HNil}
import slickless._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by Jacob Xie on 1/6/2020
 */
object DevSlickless extends App {

  class Users(tag: Tag) extends Table[Long :: String :: HNil](tag, "users") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def email = column[String]("email")

    def * = id :: email :: HNil
  }

  val UsersTableQuery = TableQuery[Users]


  val db = Database.forConfig("h2mem")

  val init = DBIO.seq(
    UsersTableQuery.schema.create,
    UsersTableQuery ++= Seq(
      1L :: "jacob@qq.com" :: HNil,
      2L :: "mz@qq.com" :: HNil,
      3L :: "sam@qq.com" :: HNil
    )
  )

  Await.result(db.run(init), Duration.Inf)

  val query = db.run(UsersTableQuery.filter(_.id === 3L).result)

  val res = Await.result(query, Duration.Inf)

  println(res)

}


