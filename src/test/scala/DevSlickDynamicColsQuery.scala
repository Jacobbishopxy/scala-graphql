import scala.language.postfixOps
import slick.jdbc.H2Profile.api._
import slick.ast.{FieldSymbol, TypedType}
import slick.lifted.{ProvenShape, RepShape}

import scala.concurrent.Await
import scala.reflect.ClassTag
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
 * Created by Jacob Xie on 1/3/2020
 */
object DevSlickDynamicColsQuery extends App {

  object CaseClassInstanceValueUpdate {
    implicit class ValueUpdate[T](i: T) {
      def valueUpdate(m: Map[String, Any]): T = {
        for ((name, value) <- m) setField(name, value)
        i
      }

      private def setField(fieldName: String, fieldValue: Any): Unit = {
        i.getClass.getDeclaredFields.find(_.getName == fieldName) match {
          case Some(field) =>
            field.setAccessible(true)
            field.set(i, fieldValue)
          case None =>
            throw new IllegalArgumentException(s"No field named $fieldName")
        }
      }
    }
  }

  case class StockPricesEOD(date: String = "",
                            ticker: String = "",
                            o: Double = 0,
                            h: Double = 0,
                            l: Double = 0,
                            c: Double = 0,
                            v: Double = 0,
                            a: Double = 0)

  val defaultStockPricesEOD = StockPricesEOD()

  class StockPricesEODTable(tag: Tag)
    extends Table[(String, String)](tag, "SP_EOD") {

    def date: Rep[String] = column[String]("date")
    def ticker: Rep[String] = column[String]("ticker")
    def o: Rep[Double] = column[Double]("o")
    def h: Rep[Double] = column[Double]("h")
    def l: Rep[Double] = column[Double]("l")
    def c: Rep[Double] = column[Double]("c")
    def v: Rep[Double] = column[Double]("v")
    def a: Rep[Double] = column[Double]("a")

    override def * : ProvenShape[(String, String)] = (date, ticker)
    override def create_* : Iterable[FieldSymbol] =
      collectFieldSymbols((date, ticker, o, h, l, c, v, a).shaped.toNode)
  }

  val StockPricesEODTableQuery = TableQuery[StockPricesEODTable]

  /**
   * dynamic
   */
  case class Dynamic[T <: Table[_], C](f: T => Rep[C])(implicit val ct: TypedType[C])

  class DynamicProductShape[Level <: ShapeLevel](val shapes: Seq[Shape[_ <: ShapeLevel, _, _, _]])
    extends MappedProductShape[Level, Seq[Any], Seq[Any], Seq[Any], Seq[Any]] {

    val classTag: ClassTag[Seq[Any]] = implicitly[ClassTag[Seq[Any]]]

    override def getIterator(value: Seq[Any]): Iterator[Any] = value.iterator
    override def getElement(value: Seq[Any], idx: Int): Any = value(idx)
    override def buildValue(elems: IndexedSeq[Any]): Any = elems
    override def copy(shapes: Seq[Shape[_ <: ShapeLevel, _, _, _]]): Shape[Level, _, _, _] =
      new DynamicProductShape(shapes)

  }

  def cond(startDate: String, endDate: String): StockPricesEODTable => Rep[Boolean] =
    (d: StockPricesEODTable) => d.date >= startDate && d.date <= endDate

  def queryForTickerDate(fields: Seq[String])
                        (ticker: Seq[String],
                         startDate: String,
                         endDate: String): List[StockPricesEOD] = {

    import CaseClassInstanceValueUpdate._

    val stringCols = Seq("ticker", "date")

    val dyn = fields.map(col =>
      if (stringCols.contains(col))
        Dynamic[StockPricesEODTable, String](_.column(col))
      else
        Dynamic[StockPricesEODTable, Double](_.column(col))
    )

    implicit def dynamicShape[Level <: ShapeLevel]: DynamicProductShape[Level] =
      new DynamicProductShape[Level](dyn.map(_ => RepShape))

    val q = StockPricesEODTableQuery
      .filter(d => d.ticker.inSet(ticker) && cond(startDate, endDate)(d))
      .map(a => dyn.map(d => d.f(a)))

    val res = q.result
    println(s"res.statements: ${res.statements.head}")

    val r = Await.result(db.run(res), 30.seconds)
    r.foldLeft(List.empty[StockPricesEOD])((acc, ele) => {
      val d = Try(defaultStockPricesEOD.valueUpdate(fields.zip(ele).toMap))
      println(d)
      d match {
        case Success(v) => acc :+ v
        case Failure(_) => acc
      }
    })
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

  /**
   * test
   */

  val db = Database.forConfig("h2mem")

  Await.result(db.run(initActions), Duration.Inf)

  val queryPartial = queryForTickerDate(List("ticker", "date", "h"))(_, _, _)

  val res = queryPartial(Seq("000001"), "20190101", "20191231")

  println(res)

}
