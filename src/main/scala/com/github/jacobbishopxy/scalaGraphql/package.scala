package com.github.jacobbishopxy

import sangria.schema.Context

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
        for ((name, value) <- m) setField(name, value)
        i
      }

      private def setField(fieldName: String, fieldValue: Any): Unit = {
        i.getClass.getDeclaredFields.find(_.getName == fieldName) match {
          case Some(field) =>
            field.setAccessible(true)
            field.set(i, fieldValue)
          case None =>
            throw new IllegalArgumentException(s"No field named $fieldName")
        }
      }
    }
  }

}
