# Scala GraphQL

- Stack: Sangria + Slick + Akka Http

- Purpose: dynamic column query by request fields.

- Run server: `sbt run`

## Note 

1. `prices`: business code

    - `Init.scala`: demo data
    
    - `Model.scala`: data model
    
        - Model object: case class
        
        - Model trait: tableQuery definition
    
    - `Resolver.scala`: data handler
    
    - `SchemaDef.scala`: sangria schema


2. `package.scala`: core functionality

    - `SlickDynamic`: dynamically construct table columns
    
    - `CaseClassInstanceValueUpdate`: update case class instance by scala Map
    
    - `DynHelper`: simplify 


3. `Repo.scala`

    Gathering all business chunks

    
4. `Server.scala`

    Main


5. `test/DevSlickDynamicColsQuery.scala`

    test case



## Test

Copy following query string to [localhost](http://localhost:8070/graphql)

```
query {
  getStockPricesEOD(
    tickers: ["000001"] 
    start: "20190102" 
    end: "20190105"
  ) {
    date
    ticker
    open
    close
  }
}
```

And you will see message as below in terminal console.

```
que.statements: select "trade_date", "stock_code", "topen", "tclose" from "DEMO" where (("stock_code" in ('000001')) and (ifnull("is_valid",0) = 1)) and (("trade_date" >= '20190101') and ("trade_date" <= '20190105'))
StockPricesEOD(20190101,000001,None,None,None,None,None,None,None,None,None,None,Some(10.0),None,None,Some(13.0),None,None,None,None,None)
StockPricesEOD(20190102,000001,None,None,None,None,None,None,None,None,None,None,Some(10.0),None,None,Some(13.0),None,None,None,None,None)
StockPricesEOD(20190103,000001,None,None,None,None,None,None,None,None,None,None,Some(10.0),None,None,Some(13.0),None,None,None,None,None)
StockPricesEOD(20190104,000001,None,None,None,None,None,None,None,None,None,None,Some(10.0),None,None,Some(13.0),None,None,None,None,None)
StockPricesEOD(20190105,000001,None,None,None,None,None,None,None,None,None,None,Some(10.0),None,None,Some(13.0),None,None,None,None,None)
```
