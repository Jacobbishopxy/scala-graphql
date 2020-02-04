package com.github.jacobbishopxy.scalaGraphql.prices

import com.github.jacobbishopxy.scalaGraphql.Repo
import sangria.macros.derive._
import sangria.marshalling.FromInput
import sangria.schema._
import sangria.util.tag.@@


/**
 * Created by Jacob Xie on 1/2/2020
 */
object SchemaDef {

  import Model._
  import com.github.jacobbishopxy.scalaGraphql.getField

  val StockPricesEODType: ObjectType[Unit, StockPricesEOD] =
    deriveObjectType(
      ObjectTypeName("StockPricesEOD"),
      ObjectTypeDescription("股票日频行情"),
      ReplaceField("date", Field("date", StringType, Some("日期"),
        resolve = _.value.date)),
      ReplaceField("exchange", Field("exchange", OptionType(StringType), Some("交易所"),
        resolve = c => if (c.value.exchange.getOrElse("") == "001001") "SH" else "SZ")),
      ExcludeFields("isValid")
    )

  val stockTickers: Argument[Seq[String @@ FromInput.CoercedScalaResult]] =
    Argument("tickers", ListInputType(StringType))

  val (startDate, endDate) =
    (Argument("start", StringType), Argument("end", StringType))

  val FieldDef: List[Field[Repo, Unit]] = fields[Repo, Unit](
    Field("getStockPricesEOD", ListType(StockPricesEODType),
      description = Some("获取股票日频行情"),
      arguments = stockTickers :: startDate :: endDate :: Nil,
      resolve = c => {
        val fields = getField(c)
        val rsv = c.ctx.resolverPrices.getStockPricesEOD(fields)(_, _, _)
        rsv(c.arg(stockTickers), c.arg(startDate), c.arg(endDate))
      }),
  )
}
