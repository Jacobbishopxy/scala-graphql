package com.github.jacobbishopxy.scalaGraphql.prices

import com.github.jacobbishopxy.scalaGraphql.DynHelper
import slick.jdbc.JdbcProfile

/**
 * Created by Jacob Xie on 1/2/2020
 */
class Resolver(val driver: JdbcProfile, val dbCfg: String) extends Model with DynHelper {

  import driver.api._
  import Model._


  private def cond1: StockPricesEODTable => Rep[Boolean] =
    (d: StockPricesEODTable) => d.isValid.getOrElse(0) === 1

  private def cond2(startDate: String, endDate: String): StockPricesEODTable => Rep[Boolean] =
    (d: StockPricesEODTable) => d.date >= startDate && d.date <= endDate

  private val defaultStockPricesEOD =
    StockPricesEOD(
      date = "",
      ticker = "",
      name = None,
      exchange =None,
      tCap = None,
      mCap = None,
      volume = None,
      amount = None,
      deals = None,
      turnoverRate = None,
      changeRate = None,
      amplitude = None,
      open = None,
      high = None,
      low = None,
      close = None,
      preClose = None,
      average = None,
      backwardAdjRatio = None,
      forwardAdjRatio = None,
      isValid = None,
    )

  private val stockPricesEODFieldMap: Map[String, Dynamic[StockPricesEODTable, _ >: DynType]] = Map(
    "date" -> "trade_date".toDyn.str,
    "ticker" -> "stock_code".toDyn.str,
    "name" -> "stock_name".toDyn.optStr,
    "exchange" -> "exchange".toDyn.optStr,
    "tCap" -> "tcap".toDyn.optDbl,
    "mCap" -> "mcap".toDyn.optDbl,
    "volume" -> "volume".toDyn.optDbl,
    "amount" -> "amount".toDyn.optDbl,
    "deals" -> "deals".toDyn.optDbl,
    "turnoverRate" -> "turnover_rate".toDyn.optDbl,
    "changeRate" -> "change_rate".toDyn.optDbl,
    "amplitude" -> "amplitude".toDyn.optDbl,
    "open" -> "topen".toDyn.optDbl,
    "high" -> "high".toDyn.optDbl,
    "low" -> "low".toDyn.optDbl,
    "close" -> "tclose".toDyn.optDbl,
    "preClose" -> "lclose".toDyn.optDbl,
    "average" -> "average".toDyn.optDbl,
    "backwardAdjRatio" -> "matiply_ratio".toDyn.optDbl,
    "forwardAdjRatio" -> "backward_adjratio".toDyn.optDbl,
    "isValid" -> "is_valid".toDyn.optInt,
  )

  private val query = (t: Seq[String], s: String, e: String) => StockPricesEODTableQuery
    .filter(d => d.ticker.inSet(t) && cond1(d) && cond2(s, e)(d))

  private val stockPricesFn = constructQueryFnSeqResult(stockPricesEODFieldMap, defaultStockPricesEOD)(_, _)

  def getStockPricesEOD(fields: Seq[String])
                       (ticker: Seq[String],
                        startDate: String,
                        endDate: String): Seq[StockPricesEOD] =
    stockPricesFn(query(ticker, startDate, endDate), fields)

}
