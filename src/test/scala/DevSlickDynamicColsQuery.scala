import scala.language.postfixOps
import slick.jdbc.H2Profile.api._
import slick.ast.{FieldSymbol, TypedType}
import slick.lifted.{ProvenShape, RepShape}
import shapeless.syntax.std.product._
import shapeless._
import syntax.std.tuple._
import labelled.{FieldType, field}

import scala.concurrent.Await
import scala.reflect.ClassTag
import scala.concurrent.duration._

/**
 * Created by Jacob Xie on 1/3/2020
 */
object DevSlickDynamicColsQuery extends App {

  object Map2CaseClass {

    trait FromMap[L <: HList] {
      def apply(m: Map[String, Any]): Option[L]
    }

    trait LowPriorityFromMap {
      implicit def hconsFromMap1[K <: Symbol, V, T <: HList](implicit
                                                             witness: Witness.Aux[K],
                                                             typeable: Typeable[V],
                                                             fromMapT: Lazy[FromMap[T]]): FromMap[FieldType[K, V] :: T] =
        (m: Map[String, Any]) => for {
          v <- m.get(witness.value.name)
          h <- typeable.cast(v)
          t <- fromMapT.value(m)
        } yield field[K](h) :: t
    }

    object FromMap extends LowPriorityFromMap {
      implicit val hnilFromMap: FromMap[HNil] = (_: Map[String, Any]) => Some(HNil)

      implicit def hconsFromMap0[K <: Symbol, V, R <: HList, T <: HList](implicit
                                                                         witness: Witness.Aux[K],
                                                                         gen: LabelledGeneric.Aux[V, R],
                                                                         fromMapH: FromMap[R],
                                                                         fromMapT: FromMap[T]): FromMap[FieldType[K, V] :: T] =
        (m: Map[String, Any]) => for {
          v <- m.get(witness.value.name)
          r <- Typeable[Map[String, Any]].cast(v)
          h <- fromMapH(r)
          t <- fromMapT(m)
        } yield field[K](gen.from(h)) :: t
    }

    class ConvertHelper[A] {
      def from[R <: HList](m: Map[String, Any])(implicit
                                                gen: LabelledGeneric.Aux[A, R],
                                                fromMap: FromMap[R]): Option[A] =
        fromMap(m).map(gen.from)
    }

    def to[A]: ConvertHelper[A] = new ConvertHelper[A]

  }


  case class StockPricesEOD(date: String = "",
                            ticker: String = "",
                            o: Double = 0,
                            h: Double = 0,
                            l: Double = 0,
                            c: Double = 0,
                            v: Double = 0,
                            a: Double = 0)

  val defaultStockPricesEODMap = StockPricesEOD().toMap.map {
    case (k, v) => k.toString.tail -> v
  }

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
      val d = defaultStockPricesEODMap ++ fields.zip(ele).toMap
      println(d)
      Map2CaseClass.to[StockPricesEOD].from(d) match {
        case Some(v) => acc :+ v
        case None => acc
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
