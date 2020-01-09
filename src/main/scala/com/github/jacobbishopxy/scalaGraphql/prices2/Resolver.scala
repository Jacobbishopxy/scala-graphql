package com.github.jacobbishopxy.scalaGraphql.prices2

import slick.jdbc.H2Profile.api._
import slick.lifted.RepShape

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by Jacob Xie on 1/6/2020
 */
class Resolver(db: Database) {

  import Model._
  import com.github.jacobbishopxy.scalaGraphql.SlickDynamic._
  import com.github.jacobbishopxy.scalaGraphql.DynHelper._

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


  private val fieldM: Map[String, Dynamic[StockPricesEOD2Table, _ >: String with Double with Int]] =
    Map(
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
      "isValid" -> "is_valid".toDyn.int,
      "c1" -> "c1".toDyn.dbl,
      "c2" -> "c2".toDyn.dbl,
      "c3" -> "c3".toDyn.dbl,
      "c4" -> "c4".toDyn.dbl,
      "c5" -> "c5".toDyn.dbl,
      "c6" -> "c6".toDyn.dbl,
      "c7" -> "c7".toDyn.dbl,
      "c8" -> "c8".toDyn.dbl,
      "c9" -> "c9".toDyn.dbl,
      "c10" -> "c10".toDyn.dbl,
      "c11" -> "c11".toDyn.dbl,
      "c12" -> "c12".toDyn.dbl,
    )


  def getStockPricesEOD2(fields: Seq[String])
                        (ticker: Seq[String],
                         startDate: String,
                         endDate: String): List[StockPricesEOD2] = {

    val dyn = constructDyn(fieldM, fields)

    implicit def dynamicShape[Level <: ShapeLevel]: DynamicProductShape[Level] =
      new DynamicProductShape[Level](dyn.map(_ => RepShape))

    val que = StockPricesEOD2TableQuery
      .filter(d => d.ticker.inSet(ticker) && cond1(d) && cond2(startDate, endDate)(d))
      .map(a => dyn.map(d => d.f(a)))
      .result

    val res = Await.result(db.run(que), 30.seconds)

    resConvert[StockPricesEOD2](defaultStockPricesEOD, fields, res)
  }


}
