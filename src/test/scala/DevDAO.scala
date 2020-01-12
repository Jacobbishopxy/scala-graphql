
import scala.language.higherKinds
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by Jacob Xie on 1/12/2020
 */
object DevDAO extends App {

  import slick.jdbc.H2Profile

  val dao = new DAO(H2Profile)

  import dao.driver.api._

  val db: dao.driver.backend.Database = Database.forConfig("h2mem")

  Await.result(db.run(dao.create), Duration.Inf)
  Await.result(db.run(dao.insert("jacob", "23")), Duration.Inf)
  val foo = Await.result(db.run(dao.get("jacob")), Duration.Inf)
  println(foo)

}

/** All database code goes into the DAO (data access object) class which
 * is parameterized by a Slick driver that implements JdbcProfile.
 */
class DAO(val driver: JdbcProfile) {
  // Import the Scala API from the driver

  import driver.api._

  class Props(tag: Tag) extends Table[(String, String)](tag, "PROPS") {
    def key: Rep[String] = column[String]("KEY", O.PrimaryKey)
    def value: Rep[String] = column[String]("VALUE")
    override def * : ProvenShape[(String, String)] = (key, value)
  }
  val props = TableQuery[Props]

  /** Create the database schema */
  def create: DBIO[Unit] =
    props.schema.create

  /** Insert a key/value pair */
  def insert(k: String, v: String): DBIO[Int] =
    props += (k, v)

  /** Get the value for the given key */
  def get(k: String): DBIO[Option[String]] =
    (for (p <- props if p.key === k) yield p.value).result.headOption

  /** Get the first element for a Query from this DAO */
  def getFirst[M, U, C[_]](q: Query[M, U, C]): DBIO[U] =
    q.result.head
}

