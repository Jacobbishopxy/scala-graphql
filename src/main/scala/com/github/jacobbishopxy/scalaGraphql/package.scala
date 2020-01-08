package com.github.jacobbishopxy

import sangria.schema.Context

import scala.util.{Failure, Success, Try}


/**
 * Created by Jacob Xie on 1/3/2020
 */
package object scalaGraphql {


  def getField[T](c: Context[T, Unit]): Vector[String] =
    c.astFields.head.selections.map(_.renderCompact)


  object SlickDynamic {

    import slick.ast.TypedType
    import slick.jdbc.H2Profile.api._
    import scala.reflect.ClassTag

    case class Dynamic[T <: Table[_], C](f: T => Rep[C])(implicit val ct: TypedType[C])

    class DynamicProductShape[Level <: ShapeLevel](val shapes: Seq[Shape[_ <: ShapeLevel, _, _, _]])
      extends MappedProductShape[Level, Seq[Any], Seq[Any], Seq[Any], Seq[Any]] {

      val classTag: ClassTag[Seq[Any]] = implicitly[ClassTag[Seq[Any]]]

      override def getIterator(value: Seq[Any]): Iterator[Any] = value.iterator
      override def getElement(value: Seq[Any], idx: Int): Any = value(idx)
      override def buildValue(elems: IndexedSeq[Any]): Any = elems
      override def copy(shapes: Seq[Shape[_ <: ShapeLevel, _, _, _]]): Shape[Level, _, _, _] =
        new DynamicProductShape(shapes)

    }
  }


  object CaseClassInstanceValueUpdate {

    implicit class ValueUpdate[T](i: T) {

      def valueUpdate(m: Map[String, Any]): T = {
        m.foreach { case (name, value) => setField(name, value) }
        i
      }

      private def setField(fieldName: String, fieldValue: Any): Unit =
        i.getClass.getDeclaredFields.find(_.getName == fieldName) match {
          case Some(field) =>
            field.setAccessible(true)
            field.set(i, fieldValue)
          case None =>
            throw new IllegalArgumentException(s"No field named $fieldName")
        }
    }
  }


  object DynHelper {

    import slick.jdbc.H2Profile.api._
    import SlickDynamic._
    import CaseClassInstanceValueUpdate._

    case class DynCol[S <: Table[_]](col: String) {
      def str: Dynamic[S, String] = Dynamic[S, String](_.column(col))
      def dbl: Dynamic[S, Double] = Dynamic[S, Double](_.column(col))
      def int: Dynamic[S, Int] = Dynamic[S, Int](_.column(col))
      def optStr: Dynamic[S, Option[String]] = Dynamic[S, Option[String]](_.column(col))
      def optDbl: Dynamic[S, Option[Double]] = Dynamic[S, Option[Double]](_.column(col))
      def optInt: Dynamic[S, Option[Int]] = Dynamic[S, Option[Int]](_.column(col))
    }

    def constructDyn[T](m: Map[String, T], fields: Seq[String]): Seq[T] =
      fields.foldLeft(Seq.empty[T]) {
        case (acc, ele) => m.get(ele).fold(acc)(acc :+ _)
      }

    def resConvert[R](defaultCaseClass: R, fields: Seq[String], res: Seq[Seq[Any]]): List[R] =
      res.foldLeft(List.empty[R]) {
        case (acc, ele) =>
          val d = Try(defaultCaseClass.valueUpdate(fields.zip(ele).toMap))
          println(d)
          d match {
            case Success(v) => acc :+ v
            case Failure(_) => acc
          }
      }
  }

}
