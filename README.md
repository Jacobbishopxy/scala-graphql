# Scala GraphQL

Sangria + Slick + Akka Http


run server: `sbt run`

## Note 

1. `prices`

    - `Init.scala`: demo data
    
    - `Model.scala`: data model
    
    - `Resolver.scala`: data handler
    
    - `SchemaDef.scala`: sangria schema


2. `package.scala`

    - `SlickDynamic.scala`: dynamically construct table columns
    
    - `Map2CaseClass.scala`: convert scala Map to scala case class


3. `Repo.scala`

    To gather all business chunk

    
4. `Server.scala`

    Main


5. `test/DevSlickDynamicColsQuery.scala`

    test case



## Test

Copy following query string to [localhost](http://localhost:8080/graphql)

```
query {
  getStockPricesEOD(
    tickers: ["000001"] 
    start: "20190102" 
    end: "20190105"
  ) {
    ticker
    date
    exchange
    close
  }
}
```

And you will see message as below.

```
res.statements: select "stock_code", "trade_date", "exchange", "tclose" from "DEMO" where (("stock_code" in ('000001')) and ("is_valid" = 1)) and (("trade_date" >= '20190102') and ("trade_date" <= '20190105'))


Map(amplitude -> 0.0, name -> , preClose -> 0.0, isValid -> 0, tCap -> 0.0, open -> 0.0, amount -> 0.0, exchange -> SZ, low -> 0.0, date -> 20190102, forwardAdjRatio -> 0.0, average -> 0.0, ticker -> 000001, deals -> 0.0, close -> 13.0, mCap -> 0.0, backwardAdjRatio -> 0.0, volume -> 0.0, changeRate -> 0.0, high -> 0.0, turnoverRate -> 0.0)
Map(amplitude -> 0.0, name -> , preClose -> 0.0, isValid -> 0, tCap -> 0.0, open -> 0.0, amount -> 0.0, exchange -> SZ, low -> 0.0, date -> 20190103, forwardAdjRatio -> 0.0, average -> 0.0, ticker -> 000001, deals -> 0.0, close -> 13.0, mCap -> 0.0, backwardAdjRatio -> 0.0, volume -> 0.0, changeRate -> 0.0, high -> 0.0, turnoverRate -> 0.0)
Map(amplitude -> 0.0, name -> , preClose -> 0.0, isValid -> 0, tCap -> 0.0, open -> 0.0, amount -> 0.0, exchange -> SZ, low -> 0.0, date -> 20190104, forwardAdjRatio -> 0.0, average -> 0.0, ticker -> 000001, deals -> 0.0, close -> 13.0, mCap -> 0.0, backwardAdjRatio -> 0.0, volume -> 0.0, changeRate -> 0.0, high -> 0.0, turnoverRate -> 0.0)
Map(amplitude -> 0.0, name -> , preClose -> 0.0, isValid -> 0, tCap -> 0.0, open -> 0.0, amount -> 0.0, exchange -> SZ, low -> 0.0, date -> 20190105, forwardAdjRatio -> 0.0, average -> 0.0, ticker -> 000001, deals -> 0.0, close -> 13.0, mCap -> 0.0, backwardAdjRatio -> 0.0, volume -> 0.0, changeRate -> 0.0, high -> 0.0, turnoverRate -> 0.0)


List(StockPricesEOD(20190102,000001,,SZ,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,13.0,0.0,0.0,0.0,0.0,0), StockPricesEOD(20190103,000001,,SZ,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,13.0,0.0,0.0,0.0,0.0,0), StockPricesEOD(20190104,000001,,SZ,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,13.0,0.0,0.0,0.0,0.0,0), StockPricesEOD(20190105,000001,,SZ,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,13.0,0.0,0.0,0.0,0.0,0))
```
