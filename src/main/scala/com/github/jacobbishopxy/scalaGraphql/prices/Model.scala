package com.github.jacobbishopxy.scalaGraphql.prices

import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import shapeless._
import slickless._

/**
 * Created by Jacob Xie on 12/31/2019
 */
trait Model {

  import Model._

  val driver: JdbcProfile

  import driver.api._

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

