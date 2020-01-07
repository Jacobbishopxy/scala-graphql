package com.github.jacobbishopxy.scalaGraphql.prices2

import com.github.jacobbishopxy.scalaGraphql.Repo
import sangria.schema._
import sangria.macros.derive._
import sangria.marshalling.FromInput
import sangria.util.tag.@@


/**
 * Created by Jacob Xie on 1/6/2020
 */
object SchemaDef {

  import Model._
  import com.github.jacobbishopxy.scalaGraphql.getField


  val StockPricesEOD2Type: ObjectType[Unit, StockPricesEOD2] =
    deriveObjectType(
      ReplaceField("date", Field("date", StringType, Some("日期"),
        resolve = _.value.date)),
      ReplaceField("exchange", Field("exchange", OptionType(StringType), Some("交易所"),
        resolve = c => if (c.value.exchange == "001001") "SH" else "SZ")),
      DocumentField("name", "股票简称")
    )

  val stockTickers: Argument[Seq[String @@ FromInput.CoercedScalaResult]] =
    Argument("tickers", ListInputType(StringType))

  val (startDate, endDate) =
    (Argument("start", StringType), Argument("end", StringType))

  val FieldDef: List[Field[Repo, Unit]] = fields[Repo, Unit](
    Field("getStockPricesEOD2", ListType(StockPricesEOD2Type),
      description = Some("获取股票日频行情2"),
      arguments = stockTickers :: startDate :: endDate :: Nil,
      resolve = c => {
        val fields = getField(c)
        val rsv = c.ctx.resolverPrices2.getStockPricesEOD2(fields)(_, _, _)
        rsv(c.arg(stockTickers), c.arg(startDate), c.arg(endDate))
      }
    )
  )
}
