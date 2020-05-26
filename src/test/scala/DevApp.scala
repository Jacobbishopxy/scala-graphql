
import com.github.jacobbishopxy.scalaGraphql.{Service, getField}
import com.github.jacobbishopxy.scalaGraphql.dynamic.DynamicHelper

import io.circe._
import sangria.marshalling.circe._
import sangria.ast.Document
import sangria.execution.Executor
import sangria.macros.derive.{ExcludeFields, ObjectTypeDescription, ObjectTypeName, ReplaceField, deriveObjectType}
import sangria.marshalling.FromInput
import sangria.schema.{Argument, Field, ListInputType, ListType, ObjectType, OptionType, Schema, StringType, fields}
import sangria.util.tag.@@
import slick.jdbc.{H2Profile, JdbcProfile}
import slick.lifted.ProvenShape
import shapeless._
import slickless._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
 * Created by Jacob Xie on 5/21/2020
 */
object DevApp extends App {

  val repo = new Repositories(H2Profile, "h2mem")
  val schemas = Schemas

  object Sev extends Service {

    override def executeGQL(query: Document,
                            operationName: Option[String],
                            variables: Json,
                            tracing: Boolean): Future[Json] =
      Executor.execute(
        schema = schemas.SD,
        queryAst = query,
        userContext = repo,
        variables = if (variables.isNull) Json.obj() else variables,
        operationName = operationName
      )
  }

  Sev.start()

}


object Prices {

  trait Model {

    val driver: JdbcProfile

    import driver.api._
    import Model._

    class StockPricesEODTable(tag: Tag)
      extends Table[StockPricesEOD](tag, "DEMO") {

      def date: Rep[String] = column[String]("trade_date")

      def ticker: Rep[String] = column[String]("stock_code")

      def name: Rep[Option[String]] = column[Option[String]]("stock_name")

      def exchange: Rep[Option[String]] = column[Option[String]]("exchange")

      def tCap: Rep[Option[Double]] = column[Option[Double]]("tcap")

      def mCap: Rep[Option[Double]] = column[Option[Double]]("mcap")

      def volume: Rep[Option[Double]] = column[Option[Double]]("volume")

      def amount: Rep[Option[Double]] = column[Option[Double]]("amount")

      def deals: Rep[Option[Double]] = column[Option[Double]]("deals")

      def turnoverRate: Rep[Option[Double]] = column[Option[Double]]("turnover_rate")

      def changeRate: Rep[Option[Double]] = column[Option[Double]]("change_rate")

      def amplitude: Rep[Option[Double]] = column[Option[Double]]("amplitude")

      def open: Rep[Option[Double]] = column[Option[Double]]("topen")

      def high: Rep[Option[Double]] = column[Option[Double]]("high")

      def low: Rep[Option[Double]] = column[Option[Double]]("low")

      def close: Rep[Option[Double]] = column[Option[Double]]("tclose")

      def preClose: Rep[Option[Double]] = column[Option[Double]]("lclose")

      def average: Rep[Option[Double]] = column[Option[Double]]("average")

      def backwardAdjRatio: Rep[Option[Double]] = column[Option[Double]]("matiply_ratio")

      def forwardAdjRatio: Rep[Option[Double]] = column[Option[Double]]("backward_adjratio")

      def isValid: Rep[Option[Int]] = column[Option[Int]]("is_valid")

      override def * : ProvenShape[StockPricesEOD] = (
        date ::
          ticker ::
          name ::
          exchange ::
          tCap ::
          mCap ::
          volume ::
          amount ::
          deals ::
          turnoverRate ::
          changeRate ::
          amplitude ::
          open ::
          high ::
          low ::
          close ::
          preClose ::
          average ::
          backwardAdjRatio ::
          forwardAdjRatio ::
          isValid ::
          HNil
        ).mappedWith(Generic[StockPricesEOD])
    }

    val StockPricesEODTableQuery = TableQuery[StockPricesEODTable]

  }

  object Model {

    case class StockPricesEOD(date: String,
                              ticker: String,
                              name: Option[String],
                              exchange: Option[String],
                              tCap: Option[Double],
                              mCap: Option[Double],
                              volume: Option[Double],
                              amount: Option[Double],
                              deals: Option[Double],
                              turnoverRate: Option[Double],
                              changeRate: Option[Double],
                              amplitude: Option[Double],
                              open: Option[Double],
                              high: Option[Double],
                              low: Option[Double],
                              close: Option[Double],
                              preClose: Option[Double],
                              average: Option[Double],
                              backwardAdjRatio: Option[Double],
                              forwardAdjRatio: Option[Double],
                              isValid: Option[Int])

  }

  class Resolver(val driver: JdbcProfile, val dbCfg: String) extends Model with DynamicHelper {

    import driver.api._
    import Model._


    private def cond1: StockPricesEODTable => Rep[Boolean] =
      (d: StockPricesEODTable) => d.isValid.getOrElse(0) === 1

    private def cond2(startDate: String, endDate: String): StockPricesEODTable => Rep[Boolean] =
      (d: StockPricesEODTable) => d.date >= startDate && d.date <= endDate

    private val defaultStockPricesEOD = DefaultCaseClass[StockPricesEOD]

    private val fm = Map(
      "date" -> "trade_date",
      "ticker" -> "stock_code",
      "name" -> "stock_name",
      "tCap" -> "tcap",
      "mCap" -> "mcap",
      "turnoverRate" -> "turnover_rate",
      "changeRate" -> "change_rate",
      "open" -> "topen",
      "close" -> "tclose",
      "preClose" -> "lclose",
      "backwardAdjRatio" -> "matiply_ratio",
      "forwardAdjRatio" -> "backward_adjratio",
      "isValid" -> "is_valid"
    )

    private val stockPricesEODFieldMap: Map[String, Dynamic[StockPricesEODTable, _ >: DynType]] =
      GenerateFieldMap[StockPricesEOD, StockPricesEODTable](Some(fm))

    private val query = (t: Seq[String], s: String, e: String) => StockPricesEODTableQuery
      .filter(d => d.ticker.inSet(t) && cond1(d) && cond2(s, e)(d))

    private val stockPricesFn = constructQueryFnSeqResult(stockPricesEODFieldMap, defaultStockPricesEOD)(_, _)

    def getStockPricesEOD(fields: Seq[String])
                         (ticker: Seq[String],
                          startDate: String,
                          endDate: String): Seq[StockPricesEOD] =
      stockPricesFn(query(ticker, startDate, endDate), fields)

  }

  object SchemaDef {

    import Model._

    val StockPricesEODType: ObjectType[Unit, StockPricesEOD] =
      deriveObjectType(
        ObjectTypeName("StockPricesEOD"),
        ObjectTypeDescription("股票日频行情"),
        ReplaceField("date", Field("date", StringType, Some("日期"),
          resolve = _.value.date)),
        ReplaceField("exchange", Field("exchange", OptionType(StringType), Some("交易所"),
          resolve = c => if (c.value.exchange.getOrElse("") == "001001") "SH" else "SZ")),
        ExcludeFields("isValid")
      )

    val stockTickers: Argument[Seq[String @@ FromInput.CoercedScalaResult]] =
      Argument("tickers", ListInputType(StringType))

    val (startDate, endDate) =
      (Argument("start", StringType), Argument("end", StringType))
  }

  class Init(val driver: JdbcProfile, dbCfg: String) extends Model {

    import driver.api._

    private val db = driver.backend.Database.forConfig(dbCfg)

    val initActions = DBIO.seq(
      StockPricesEODTableQuery.schema.create,
      StockPricesEODTableQuery.map(r => (
        r.date,
        r.ticker,
        r.name,
        r.exchange,
        r.column[Option[Double]]("tcap"),
        r.column[Option[Double]]("mcap"),
        r.column[Option[Double]]("volume"),
        r.column[Option[Double]]("amount"),
        r.column[Option[Double]]("deals"),
        r.column[Option[Double]]("turnover_rate"),
        r.column[Option[Double]]("change_rate"),
        r.column[Option[Double]]("amplitude"),
        r.column[Option[Double]]("topen"),
        r.column[Option[Double]]("high"),
        r.column[Option[Double]]("low"),
        r.column[Option[Double]]("tclose"),
        r.column[Option[Double]]("lclose"),
        r.column[Option[Double]]("average"),
        r.column[Option[Double]]("matiply_ratio"),
        r.column[Option[Double]]("backward_adjratio"),
        r.column[Option[Int]]("is_valid"),
      )) ++= Seq(
        ("20190101", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190102", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190103", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190104", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190105", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190106", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190107", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190108", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190109", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190110", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190111", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190112", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190113", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190114", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190115", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190116", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190117", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190118", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190119", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190120", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190121", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190122", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190123", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190124", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190125", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190126", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190127", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190128", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190129", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190130", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190131", "000001", Some("n1"), Some("001002"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),

        ("20190101", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190102", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190103", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190104", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190105", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190106", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190107", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190108", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190109", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190110", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190111", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190112", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190113", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190114", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190115", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190116", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190117", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190118", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190119", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190120", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190121", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190122", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190123", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190124", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190125", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190126", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190127", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190128", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190129", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190130", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
        ("20190131", "600000", Some("n2"), Some("001001"), Some(2D), Some(3D), Some(4D), Some(5D), Some(6D), Some(7D), Some(8D), Some(9D), Some(10D), Some(11D), Some(12D), Some(13D), Some(14D), Some(15D), Some(16D), Some(17D), Some(1)),
      )
    )


    def initDatabase(): Unit = {
      Await.result(db.run(initActions), 30.seconds)
      println("init database complete")
    }

  }

}


class Repositories(val driver: JdbcProfile, dbCfg: String) {

  val resolverPrices = new Prices.Resolver(driver, dbCfg)

  // init database, for demo
  new Prices.Init(driver, dbCfg).initDatabase()
}


object Schemas {

  import Prices.SchemaDef._

  private val pricesFields = fields[Repositories, Unit](
    Field("getStockPricesEOD", ListType(StockPricesEODType),
      description = Some("获取股票日频行情"),
      arguments = stockTickers :: startDate :: endDate :: Nil,
      resolve = c => {
        val fields = getField(c)
        val rsv = c.ctx.resolverPrices.getStockPricesEOD(fields)(_, _, _)
        rsv(c.arg(stockTickers), c.arg(startDate), c.arg(endDate))
      }),
  )

  private val queryFields: List[Field[Repositories, Unit]] =
    pricesFields


  private val queryType: ObjectType[Repositories, Unit] =
    ObjectType("Query", fields[Repositories, Unit](queryFields: _*))

  // schema definition
  val SD: Schema[Repositories, Unit] = Schema(queryType)
}
