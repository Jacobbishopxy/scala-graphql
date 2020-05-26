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

3. `utils`

    - [`Copyable`](src/main/scala/com/github/jacobbishopxy/scalaGraphql/utils/Copyable.scala)
    
    - [`DefaultCaseClass`](src/main/scala/com/github/jacobbishopxy/scalaGraphql/utils/DefaultCaseClass.scala)
    
    - [`ExpectedMetadata`](src/main/scala/com/github/jacobbishopxy/scalaGraphql/utils/ExpectedMetadata.scala)

4. [`Service.scala`](src/main/scala/com/github/jacobbishopxy/scalaGraphql/Service.scala)



## Test

1. [`DevApp`](src/test/scala/DevApp.scala)

    test business code using 

2. [`DevSlickDynamicColsQuery`](src/test/scala/DevSlickDynamicColsQuery.scala)

    test slick dynamic query
