package com.github.jacobbishopxy.scalaGraphql.dynamic

import com.github.jacobbishopxy.scalaGraphql.{Copyable, DatabaseComponent}
import slick.lifted.RepShape
import shapeless._
import shapeless.labelled._

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

  type FieldMapType = Map[String, Dynamic[_, _ >: DynType]]

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


  private def typeStrToDyn[T <: Table[_]](keyName: String, typeStr: String): Dynamic[T, _ >: DynType] = typeStr match {
    case "String" => keyName.toDyn[T].str
    case "Double" => keyName.toDyn[T].dbl
    case "Int" => keyName.toDyn[T].int
    case "Boolean" => keyName.toDyn[T].bll
    case "Option[String]" => keyName.toDyn[T].optStr
    case "Option[Double]" => keyName.toDyn[T].optDbl
    case "Option[Int]" => keyName.toDyn[T].optInt
    case "Option[Boolean]" => keyName.toDyn[T].optBll
  }

  object ExpectedMetadata {

    sealed trait GetFieldTypes[T, TB <: Table[_]] {
      def getFieldTypes(renameDict: Option[Map[String, String]] = None): Map[String, Dynamic[TB, _ >: DynType]]
    }

    def apply[T, TB <: Table[_]](renameDict: Option[Map[String, String]] = None)
                                (implicit g: GetFieldTypes[T, TB]): Map[String, Dynamic[TB, _ >: DynType]] =
      g.getFieldTypes(renameDict)

    implicit def hNil[TB <: Table[_]]: GetFieldTypes[HNil, TB] = new GetFieldTypes[HNil, TB] {
      def getFieldTypes(renameDict: Option[Map[String, String]] = None): Map[String, Nothing] = Map.empty
    }

    implicit def hCons[K <: Symbol, V, T <: HList, TB <: Table[_]](implicit
                                                                   wit: Witness.Aux[K],
                                                                   typ: Typeable[V],
                                                                   rest: GetFieldTypes[T, TB]): GetFieldTypes[FieldType[K, V] :: T, TB] =
      new GetFieldTypes[FieldType[K, V] :: T, TB] {
        def getFieldTypes(renameDict: Option[Map[String, String]] = None): Map[String, Dynamic[TB, _ >: DynType]] = {
          val name = wit.value.name
          val typeDescribe = typ.describe

          val s: (String, Dynamic[TB, _ >: DynType]) = renameDict match {
            case None => name -> typeStrToDyn[TB](name, typeDescribe)
            case Some(d) => name -> typeStrToDyn[TB](d.getOrElse(name, name), typeDescribe)
          }

          rest.getFieldTypes(renameDict) + s
        }
      }

    implicit def caseClass[T, G, TB <: Table[_]](implicit
                                                 lg: LabelledGeneric.Aux[T, G],
                                                 rest: GetFieldTypes[G, TB]): GetFieldTypes[T, TB] =
      new GetFieldTypes[T, TB] {
        def getFieldTypes(renameDict: Option[Map[String, String]] = None): Map[String, Dynamic[TB, _ >: DynType]] =
          rest.getFieldTypes(renameDict)
      }
  }


}

