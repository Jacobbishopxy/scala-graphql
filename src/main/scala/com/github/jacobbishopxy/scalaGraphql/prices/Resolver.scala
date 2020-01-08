package com.github.jacobbishopxy.scalaGraphql.prices

import slick.jdbc.H2Profile.api._
import slick.lifted.RepShape

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
 * Created by Jacob Xie on 1/2/2020
 */
class Resolver(db: Database) {

  import Model._
  import com.github.jacobbishopxy.scalaGraphql.SlickDynamic._
  import com.github.jacobbishopxy.scalaGraphql.CaseClassInstanceValueUpdate._

  private def cond1: StockPricesEODTable => Rep[Boolean] =
    (d: StockPricesEODTable) => d.isValid === 1

  private def cond2(startDate: String, endDate: String): StockPricesEODTable => Rep[Boolean] =
    (d: StockPricesEODTable) => d.date >= startDate && d.date <= endDate

  private val defaultStockPricesEOD =
    StockPricesEOD(
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
      isValid = 0
    )

  def getStockPricesEOD(fields: Seq[String])
                       (ticker: Seq[String],
                        startDate: String,
                        endDate: String): Seq[StockPricesEOD] = {

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
        Dynamic[StockPricesEODTable, String](_.column(fieldMap.getOrElse(col, "")))
      else
        Dynamic[StockPricesEODTable, Double](_.column(fieldMap.getOrElse(col, "")))
    )

    implicit def dynamicShape[Level <: ShapeLevel]: DynamicProductShape[Level] =
      new DynamicProductShape[Level](dyn.map(_ => RepShape))

    val que = StockPricesEODTableQuery
      .filter(d => d.ticker.inSet(ticker) && cond1(d) && cond2(startDate, endDate)(d))
      .map(a => dyn.map(d => d.f(a)))
      .result
    println(s"\nque.statements: ${que.statements.head}\n")

    val res = Await.result(db.run(que), 30.seconds)
    println(res)

    val ans = res.foldLeft(List.empty[StockPricesEOD])((acc, ele) => {
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
