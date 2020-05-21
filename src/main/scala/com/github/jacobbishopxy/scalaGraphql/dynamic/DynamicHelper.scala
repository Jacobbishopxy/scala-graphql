package com.github.jacobbishopxy.scalaGraphql.dynamic

import com.github.jacobbishopxy.scalaGraphql.{Copyable, DatabaseComponent}
import slick.lifted.RepShape

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
 * Created by Jacob Xie on 5/21/2020
 */
trait DynamicHelper extends SlickDynamic with DatabaseComponent {
  val queryTimeout: FiniteDuration = 30.seconds

  import driver.api._
  import Copyable.copy

  type DynType = String
    with Double
    with Int
    with Boolean
    with Option[String]
    with Option[Double]
    with Option[Int]
    with Option[Boolean]

  case class DynCol[T <: Table[_]](col: String) {
    def str: Dynamic[T, String] = Dynamic[T, String](_.column(col))

    def dbl: Dynamic[T, Double] = Dynamic[T, Double](_.column(col))

    def int: Dynamic[T, Int] = Dynamic[T, Int](_.column(col))

    def bll: Dynamic[T, Boolean] = Dynamic[T, Boolean](_.column(col))

    def optStr: Dynamic[T, Option[String]] = Dynamic[T, Option[String]](_.column(col))

    def optDbl: Dynamic[T, Option[Double]] = Dynamic[T, Option[Double]](_.column(col))

    def optInt: Dynamic[T, Option[Int]] = Dynamic[T, Option[Int]](_.column(col))

    def optBll: Dynamic[T, Option[Boolean]] = Dynamic[T, Option[Boolean]](_.column(col))
  }

  def constructDyn[T](m: Map[String, T], fields: Seq[String]): Seq[T] =
    fields.foldLeft(Seq.empty[T]) {
      case (acc, ele) => m.get(ele).fold(acc)(acc :+ _)
    }

  def resConvert[R](defaultCaseClass: R, fields: Seq[String], res: Seq[Seq[Any]]): List[R] =
    res.foldLeft(List.empty[R]) {
      case (acc, ele) =>
        Try(copy(defaultCaseClass, fields.zip(ele).toMap)) match {
          case Success(v) => acc :+ v
          case Failure(_) => acc
        }
    }

  def constructQueryFnSeqResult[T <: Table[_], C, R](fieldMap: Map[String, Dynamic[T, _]],
                                                     defaultCase: C)
                                                    (query: Query[T, C, Seq],
                                                     selectedFields: Seq[String]): Seq[C] = {

    val dyn = constructDyn(fieldMap, selectedFields)

    implicit def dynamicShape[Level <: ShapeLevel]: DynamicProductShape[Level] =
      new DynamicProductShape[Level](dyn.map(_ => RepShape))

    val que = query
      .map(a => dyn.map(d => d.f(a)))
      .result

    val res = Await.result(db.run(que), queryTimeout)
    resConvert(defaultCase, selectedFields, res)
  }

  implicit class DynMaker(d: String) {
    def toDyn[T <: Table[_]]: DynCol[T] = DynCol[T](d)
  }

}
