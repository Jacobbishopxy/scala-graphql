package com.github.jacobbishopxy.scalaGraphql.prices2

import slick.jdbc.H2Profile.api._
import slick.lifted.RepShape

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
 * Created by Jacob Xie on 1/6/2020
 */
class Resolver(db: Database) {

  import Model._
  import com.github.jacobbishopxy.scalaGraphql.SlickDynamic._
  import com.github.jacobbishopxy.scalaGraphql.CaseClassInstanceValueUpdate._

  private def cond1: StockPricesEOD2Table => Rep[Boolean] =
    (d: StockPricesEOD2Table) => d.isValid === 1

  private def cond2(startDate: String, endDate: String): StockPricesEOD2Table => Rep[Boolean] =
    (d: StockPricesEOD2Table) => d.date >= startDate && d.date <= endDate

  private val defaultStockPricesEOD =
    StockPricesEOD2(
      date = "",
      ticker = "",
      name = "",
      exchange = "",
      tCap = 0,
      mCap = 0,
      volume = 0,
      amount = 0,
      deals = 0,
      turnoverRate = 0,
      changeRate = 0,
      amplitude = 0,
      open = 0,
      high = 0,
      low = 0,
      close = 0,
      preClose = 0,
      average = 0,
      backwardAdjRatio = 0,
      forwardAdjRatio = 0,
      isValid = 0,
      c1 = 0,
      c2 = 0,
      c3 = 0,
      c4 = 0,
      c5 = 0,
      c6 = 0,
      c7 = 0,
      c8 = 0,
      c9 = 0,
      c10 = 0,
      c11 = 0,
      c12 = 0,
    )

  def getStockPricesEOD2(fields: Seq[String])
                        (ticker: Seq[String],
                         startDate: String,
                         endDate: String): List[StockPricesEOD2] = {

    val stringCols = Seq("ticker", "exchange", "date")
    val fieldMap = Map(
      "date" -> "trade_date",
      "ticker" -> "stock_code",
      "name" -> "stock_name",
      "exchange" -> "exchange",
      "tCap" -> "tcap",
      "mCap" -> "mcap",
      "volume" -> "volume",
      "amount" -> "amount",
      "deals" -> "deals",
      "turnoverRate" -> "turnover_rate",
      "changeRate" -> "change_rate",
      "amplitude" -> "amplitude",
      "open" -> "topen",
      "high" -> "high",
      "low" -> "low",
      "close" -> "tclose",
      "preClose" -> "lclose",
      "average" -> "average",
      "backwardAdjRatio" -> "matiply_ratio",
      "forwardAdjRatio" -> "backward_adjratio",
    )

    val dyn = fields.map(col =>
      if (stringCols.contains(col))
        Dynamic[StockPricesEOD2Table, String](_.column(fieldMap.getOrElse(col, col)))
      else
        Dynamic[StockPricesEOD2Table, Double](_.column(fieldMap.getOrElse(col, col)))
    )

    implicit def dynamicShape[Level <: ShapeLevel]: DynamicProductShape[Level] =
      new DynamicProductShape[Level](dyn.map(_ => RepShape))

    val que = StockPricesEOD2TableQuery
      .filter(d => d.ticker.inSet(ticker) && cond1(d) && cond2(startDate, endDate)(d))
      .map(a => dyn.map(d => d.f(a)))
      .result
    println(s"\nque.statements: ${que.statements.head}\n")

    val res = Await.result(db.run(que), 30.seconds)
    println(res)

    val ans = res.foldLeft(List.empty[StockPricesEOD2])((acc, ele) => {
      val d = Try(defaultStockPricesEOD.valueUpdate(fields.zip(ele).toMap))
      println(d)
      d match {
        case Success(v) => acc :+ v
        case Failure(_) => acc
      }
    })
    println("\n")
    println(ans)
    ans
  }


}
