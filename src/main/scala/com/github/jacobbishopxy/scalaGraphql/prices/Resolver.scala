package com.github.jacobbishopxy.scalaGraphql.prices

import com.github.jacobbishopxy.scalaGraphql.DynHelper
import slick.jdbc.JdbcProfile
import slick.lifted.RepShape

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by Jacob Xie on 1/2/2020
 */
class Resolver(val driver: JdbcProfile, val dbCfg: String) extends Model with DynHelper {

  import driver.api._
  import Model._


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

  private val stockPricesEODFieldMap: Map[String, Dynamic[StockPricesEODTable, _ >: String with Double]] = Map(
    "date" -> "trade_date".toDyn.str,
    "ticker" -> "stock_code".toDyn.str,
    "name" -> "stock_name".toDyn.str,
    "exchange" -> "exchange".toDyn.str,
    "tCap" -> "tcap".toDyn.dbl,
    "mCap" -> "mcap".toDyn.dbl,
    "volume" -> "volume".toDyn.dbl,
    "amount" -> "amount".toDyn.dbl,
    "deals" -> "deals".toDyn.dbl,
    "turnoverRate" -> "turnover_rate".toDyn.dbl,
    "changeRate" -> "change_rate".toDyn.dbl,
    "amplitude" -> "amplitude".toDyn.dbl,
    "open" -> "topen".toDyn.dbl,
    "high" -> "high".toDyn.dbl,
    "low" -> "low".toDyn.dbl,
    "close" -> "tclose".toDyn.dbl,
    "preClose" -> "lclose".toDyn.dbl,
    "average" -> "average".toDyn.dbl,
    "backwardAdjRatio" -> "matiply_ratio".toDyn.dbl,
    "forwardAdjRatio" -> "backward_adjratio".toDyn.dbl,
    "isValid" -> "is_valid".toDyn.dbl,
  )

  // old version
  def getStockPricesEOD(fields: Seq[String])
                       (ticker: Seq[String],
                        startDate: String,
                        endDate: String): Seq[StockPricesEOD] = {


    val dyn = constructDyn(stockPricesEODFieldMap, fields)

    implicit def dynamicShape[Level <: ShapeLevel]: DynamicProductShape[Level] =
      new DynamicProductShape[Level](dyn.map(_ => RepShape))

    val que = StockPricesEODTableQuery
      .filter(d => d.ticker.inSet(ticker) && cond1(d) && cond2(startDate, endDate)(d))
      .map(a => dyn.map(d => d.f(a)))
      .result
    println(s"\nque.statements: ${que.statements.head}")

    val res = Await.result(db.run(que), 30.seconds)
    val ans = resConvert[StockPricesEOD](defaultStockPricesEOD, fields, res)
    println(ans)
    ans
  }

  // new version: using constructQueryFn
  private val query = (t: Seq[String], s: String, e: String) => StockPricesEODTableQuery
    .filter(d => d.ticker.inSet(t) && cond1(d) && cond2(s, e)(d))

  private val stockPricesFn = constructQueryFn(stockPricesEODFieldMap, defaultStockPricesEOD)(_, _)

  def getStockPricesEODPro(fields: Seq[String])
                          (ticker: Seq[String],
                           startDate: String,
                           endDate: String): Seq[StockPricesEOD] =
    stockPricesFn(query(ticker, startDate, endDate), fields)

}
