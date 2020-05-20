package com.github.jacobbishopxy.scalaGraphql.prices


import slick.jdbc.JdbcProfile
import scala.concurrent.Await
import scala.concurrent.duration._


/**
 * Created by Jacob Xie on 1/3/2020
 */
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
