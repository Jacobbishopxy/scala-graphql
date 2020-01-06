package com.github.jacobbishopxy.scalaGraphql.prices2

import slick.jdbc.H2Profile.api._
import slick.ast.FieldSymbol
import slick.lifted.ProvenShape
import slickless._
import shapeless._


/**
 * Created by Jacob Xie on 1/6/2020
 */
object Model {

  case class StockPricesEOD2(date: String,
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
                             isValid: Int,
                             c1: Double,
                             c2: Double,
                             c3: Double,
                             c4: Double,
                             c5: Double,
                             c6: Double,
                             c7: Double,
                             c8: Double,
                             c9: Double,
                             c10: Double,
                             c11: Double,
                             c12: Double)


  class StockPricesEOD2Table(tag: Tag)
    extends Table[StockPricesEOD2](tag, "DEMO2") {

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
    def c1: Rep[Double] = column[Double]("c1")
    def c2: Rep[Double] = column[Double]("c2")
    def c3: Rep[Double] = column[Double]("c3")
    def c4: Rep[Double] = column[Double]("c4")
    def c5: Rep[Double] = column[Double]("c5")
    def c6: Rep[Double] = column[Double]("c6")
    def c7: Rep[Double] = column[Double]("c7")
    def c8: Rep[Double] = column[Double]("c8")
    def c9: Rep[Double] = column[Double]("c9")
    def c10: Rep[Double] = column[Double]("c10")
    def c11: Rep[Double] = column[Double]("c11")
    def c12: Rep[Double] = column[Double]("c12")

    override def * : ProvenShape[StockPricesEOD2] = (
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
        c1 ::
        c2 ::
        c3 ::
        c4 ::
        c5 ::
        c6 ::
        c7 ::
        c8 ::
        c9 ::
        c10 ::
        c11 ::
        c12 ::
        HNil
      ).mappedWith(Generic[StockPricesEOD2])
  }

  val StockPricesEOD2TableQuery = TableQuery[StockPricesEOD2Table]

}
