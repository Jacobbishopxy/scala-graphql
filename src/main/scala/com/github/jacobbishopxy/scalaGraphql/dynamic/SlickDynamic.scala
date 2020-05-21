package com.github.jacobbishopxy.scalaGraphql.dynamic

import slick.jdbc.JdbcProfile

/**
 * Created by Jacob Xie on 5/21/2020
 */
trait SlickDynamic {

  val driver: JdbcProfile

  import slick.ast.TypedType
  import driver.api._
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
