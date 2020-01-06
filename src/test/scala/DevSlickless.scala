import slick.jdbc.H2Profile.api._
import shapeless.{::, HList, HNil}
import slick.ast.FieldSymbol
import slick.lifted.{ProvenShape, RepShape}
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

object DevSlicklessDynamic extends App {


  import com.github.jacobbishopxy.scalaGraphql.SlickDynamic._

  val db = Database.forConfig("h2mem")


  // date, ticker, o, h, l, c, v, a
  type KLine = String :: String :: Double :: Double :: Double :: Double :: Double :: Double :: HNil


  class StockPricesEODTable(tag: Tag) extends Table[KLine](tag, "SP_EOD") {

    def date: Rep[String] = column[String]("date")
    def ticker: Rep[String] = column[String]("ticker")
    def o: Rep[Double] = column[Double]("o")
    def h: Rep[Double] = column[Double]("h")
    def l: Rep[Double] = column[Double]("l")
    def c: Rep[Double] = column[Double]("c")
    def v: Rep[Double] = column[Double]("v")
    def a: Rep[Double] = column[Double]("a")

    override def * : ProvenShape[KLine] = date :: ticker :: o :: h :: l :: c :: v :: a :: HNil
    override def create_* : Iterable[FieldSymbol] =
      collectFieldSymbols(*.shaped.toNode)
  }

  val StockPricesEODTableQuery = TableQuery[StockPricesEODTable]

  def cond(startDate: String, endDate: String): StockPricesEODTable => Rep[Boolean] =
    (d: StockPricesEODTable) => d.date >= startDate && d.date <= endDate


  def queryForTickerDate(fields: Seq[String])
                        (ticker: Seq[String],
                         startDate: String,
                         endDate: String) = {

    val stringCols = Seq("ticker", "date")

    val dyn = fields.map { col =>
      if (stringCols.contains(col))
        Dynamic[StockPricesEODTable, String](_.column(col))
      else
        Dynamic[StockPricesEODTable, Double](_.column(col))
    }

    implicit def dynamicShape[Level <: ShapeLevel]: DynamicProductShape[Level] =
      new DynamicProductShape[Level](dyn.map(_ => RepShape))

    val que = StockPricesEODTableQuery
      .filter(d => d.ticker.inSet(ticker) && cond(startDate, endDate)(d))
      .map(a => dyn.map(d => d.f(a)))
      .result

    println(s"que.statements: ${que.statements.head}")

    val res = Await.result(db.run(que), 30.seconds)
    res
  }


  val initActions = DBIO.seq(
    StockPricesEODTableQuery.schema.create,
    StockPricesEODTableQuery.map(r =>
      (
        r.ticker,
        r.date,
        r.column[Double]("o"),
        r.column[Double]("h"),
        r.column[Double]("l"),
        r.column[Double]("c"),
        r.column[Double]("v"),
        r.column[Double]("a")
      )
    ) ++= Seq(
      ("000001", "20190201", 10, 11, 9, 10, 100, 2000),
      ("000001", "20191001", 12, 13, 8, 11, 105, 2500)
    ),
  )

  Await.result(db.run(initActions), Duration.Inf)

  val queryPartial = queryForTickerDate(List("ticker", "date", "h"))(_, _, _)

  val res = queryPartial(Seq("000001"), "20190101", "20191231")

  println(res)

}

