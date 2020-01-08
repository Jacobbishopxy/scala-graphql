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



  val fieldM = Map(
    "date" -> DynCol[StockPricesEOD2Table]("trade_date").str,
    "ticker" -> DynCol[StockPricesEOD2Table]("stock_code").str,
    "name" -> DynCol[StockPricesEOD2Table]("stock_name").str,
    "exchange" -> DynCol[StockPricesEOD2Table]("exchange").str,
    "tCap" -> DynCol[StockPricesEOD2Table]("tcap").dbl,
    "mCap" -> DynCol[StockPricesEOD2Table]("mcap").dbl,
    "volume" -> DynCol[StockPricesEOD2Table]("volume").dbl,
    "amount" -> DynCol[StockPricesEOD2Table]("amount").dbl,
    "deals" -> DynCol[StockPricesEOD2Table]("deals").dbl,
    "turnoverRate" -> DynCol[StockPricesEOD2Table]("turnover_rate").dbl,
    "changeRate" -> DynCol[StockPricesEOD2Table]("change_rate").dbl,
    "amplitude" -> DynCol[StockPricesEOD2Table]("amplitude").dbl,
    "open" -> DynCol[StockPricesEOD2Table]("topen").dbl,
    "high" -> DynCol[StockPricesEOD2Table]("high").dbl,
    "low" -> DynCol[StockPricesEOD2Table]("low").dbl,
    "close" -> DynCol[StockPricesEOD2Table]("tclose").dbl,
    "preClose" -> DynCol[StockPricesEOD2Table]("lclose").dbl,
    "average" -> DynCol[StockPricesEOD2Table]("average").dbl,
    "backwardAdjRatio" -> DynCol[StockPricesEOD2Table]("matiply_ratio").dbl,
    "forwardAdjRatio" -> DynCol[StockPricesEOD2Table]("backward_adjratio").dbl,
    "isValid" -> DynCol[StockPricesEOD2Table]("is_valid").int,
    "c1" -> DynCol[StockPricesEOD2Table]("c1").dbl,
    "c2" -> DynCol[StockPricesEOD2Table]("c2").dbl,
    "c3" -> DynCol[StockPricesEOD2Table]("c3").dbl,
    "c4" -> DynCol[StockPricesEOD2Table]("c4").dbl,
    "c5" -> DynCol[StockPricesEOD2Table]("c5").dbl,
    "c6" -> DynCol[StockPricesEOD2Table]("c6").dbl,
    "c7" -> DynCol[StockPricesEOD2Table]("c7").dbl,
    "c8" -> DynCol[StockPricesEOD2Table]("c8").dbl,
    "c9" -> DynCol[StockPricesEOD2Table]("c9").dbl,
    "c10" -> DynCol[StockPricesEOD2Table]("c10").dbl,
    "c11" -> DynCol[StockPricesEOD2Table]("c11").dbl,
    "c12" -> DynCol[StockPricesEOD2Table]("c12").dbl,
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
    println(s"\nque.statements: ${que.statements.head}\n")

    val res = Await.result(db.run(que), 30.seconds)
    println(res)

    val ans = resConvert[StockPricesEOD2](defaultStockPricesEOD, fields, res)
    println(ans)
    ans
  }


}
