# Scala GraphQL

- Stack: Sangria + Slick + Akka Http

- Purpose: a package support dynamic column query by request fields.

- Run test server: please check [this test case](src/test/scala/DevApp.scala).


## Structure

![img](./scala-graphql.png)

1. `dynamic`

    - [`DynamicHelper`](src/main/scala/com/github/jacobbishopxy/scalaGraphql/dynamic/DynamicHelper.scala)
    
    - [`SlickDynamic`](src/main/scala/com/github/jacobbishopxy/scalaGraphql/dynamic/SlickDynamic.scala)

2. `support`

    - [`CorsSupport`](src/main/scala/com/github/jacobbishopxy/scalaGraphql/support/CorsSupport.scala)
    
    - [`RequestUnmarshaller`](src/main/scala/com/github/jacobbishopxy/scalaGraphql/support/RequestUnmarshaller.scala)

3. [`package.scala`](src/main/scala/com/github/jacobbishopxy/scalaGraphql/package.scala)

4. [`Service.scala`](src/main/scala/com/github/jacobbishopxy/scalaGraphql/Service.scala)



## Test

1. [`DevApp`](src/test/scala/DevApp.scala)

    test business code using 

2. [`DevSlickDynamicColsQuery`](src/test/scala/DevSlickDynamicColsQuery.scala)

    test case



## Try

Copy following query string to [localhost](http://localhost:8088/graphql)

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

Then you will see message as below in the terminal console.

```
que.statements: select "trade_date", "stock_code", "topen", "tclose" from "DEMO" where (("stock_code" in ('000001')) and (ifnull("is_valid",0) = 1)) and (("trade_date" >= '20190101') and ("trade_date" <= '20190105'))
StockPricesEOD(20190101,000001,None,None,None,None,None,None,None,None,None,None,Some(10.0),None,None,Some(13.0),None,None,None,None,None)
StockPricesEOD(20190102,000001,None,None,None,None,None,None,None,None,None,None,Some(10.0),None,None,Some(13.0),None,None,None,None,None)
StockPricesEOD(20190103,000001,None,None,None,None,None,None,None,None,None,None,Some(10.0),None,None,Some(13.0),None,None,None,None,None)
StockPricesEOD(20190104,000001,None,None,None,None,None,None,None,None,None,None,Some(10.0),None,None,Some(13.0),None,None,None,None,None)
StockPricesEOD(20190105,000001,None,None,None,None,None,None,None,None,None,None,Some(10.0),None,None,Some(13.0),None,None,None,None,None)
```

Resulting in [localhost](http://localhost:8088/graphql) as following:

```
{
  "data": {
    "getStockPricesEOD": [
      {
        "date": "20190101",
        "ticker": "000001",
        "open": 10,
        "close": 13
      },
      {
        "date": "20190102",
        "ticker": "000001",
        "open": 10,
        "close": 13
      },
      {
        "date": "20190103",
        "ticker": "000001",
        "open": 10,
        "close": 13
      },
      {
        "date": "20190104",
        "ticker": "000001",
        "open": 10,
        "close": 13
      },
      {
        "date": "20190105",
        "ticker": "000001",
        "open": 10,
        "close": 13
      }
    ]
  }
}
```
