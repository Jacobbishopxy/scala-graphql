package com.github.jacobbishopxy.scalaGraphql.prices

import slick.ast.FieldSymbol
import slick.jdbc.SQLServerProfile.api._
import slick.lifted.ProvenShape


/**
 * Created by Jacob Xie on 12/31/2019
 */
object Model {

  case class StockPricesEOD(date: String,
                            ticker: String,
                            name: String,
                            exchange: String,
                            tCap: Double,
                            mCap: Double,
                            volume: Double,
                            amount: Double,
                            deals: Double,
                            turnoverRate: Double,
                            changeRate: Double,
                            amplitude: Double,
                            open: Double,
                            high: Double,
                            low: Double,
                            close: Double,
                            preClose: Double,
                            average: Double,
                            backwardAdjRatio: Double,
                            forwardAdjRatio: Double,
                            isValid: Int)

  class StockPricesEODTable(tag: Tag)
    extends Table[(String, String)](tag, "DEMO") {

    def date: Rep[String] = column[String]("trade_date")
    def ticker: Rep[String] = column[String]("stock_code")
    def name: Rep[String] = column[String]("stock_name")
    def exchange: Rep[String] = column[String]("exchange")
    def tCap: Rep[Double] = column[Double]("tcap")
    def mCap: Rep[Double] = column[Double]("mcap")
    def volume: Rep[Double] = column[Double]("volume")
    def amount: Rep[Double] = column[Double]("amount")
    def deals: Rep[Double] = column[Double]("deals")
    def turnoverRate: Rep[Double] = column[Double]("turnover_rate")
    def changeRate: Rep[Double] = column[Double]("change_rate")
    def amplitude: Rep[Double] = column[Double]("amplitude")
    def open: Rep[Double] = column[Double]("topen")
    def high: Rep[Double] = column[Double]("high")
    def low: Rep[Double] = column[Double]("low")
    def close: Rep[Double] = column[Double]("tclose")
    def preClose: Rep[Double] = column[Double]("lclose")
    def average: Rep[Double] = column[Double]("average")
    def backwardAdjRatio: Rep[Double] = column[Double]("matiply_ratio")
    def forwardAdjRatio: Rep[Double] = column[Double]("backward_adjratio")
    def isValid: Rep[Int] = column[Int]("is_valid")

    override def * : ProvenShape[(String, String)] = (date, ticker)
    override def create_* : Iterable[FieldSymbol] =
      collectFieldSymbols(
        (
          date,
          ticker,
          name,
          exchange,
          tCap,
          mCap,
          volume,
          amount,
          deals,
          turnoverRate,
          changeRate,
          amplitude,
          open,
          high,
          low,
          close,
          preClose,
          average,
          backwardAdjRatio,
          forwardAdjRatio,
          isValid
          ).shaped.toNode
      )
  }

  val StockPricesEODTableQuery = TableQuery[StockPricesEODTable]

}
