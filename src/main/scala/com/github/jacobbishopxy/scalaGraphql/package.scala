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


  object Map2CaseClass {

    import shapeless._
    import labelled.{FieldType, field}

    trait FromMap[L <: HList] {
      def apply(m: Map[String, Any]): Option[L]
    }

    trait LowPriorityFromMap {
      implicit def hconsFromMap1[K <: Symbol, V, T <: HList](implicit
                                                             witness: Witness.Aux[K],
                                                             typeable: Typeable[V],
                                                             fromMapT: Lazy[FromMap[T]]): FromMap[FieldType[K, V] :: T] =
        (m: Map[String, Any]) => for {
          v <- m.get(witness.value.name)
          h <- typeable.cast(v)
          t <- fromMapT.value(m)
        } yield field[K](h) :: t
    }

    object FromMap extends LowPriorityFromMap {
      implicit val hnilFromMap: FromMap[HNil] = (_: Map[String, Any]) => Some(HNil)

      implicit def hconsFromMap0[K <: Symbol, V, R <: HList, T <: HList](implicit
                                                                         witness: Witness.Aux[K],
                                                                         gen: LabelledGeneric.Aux[V, R],
                                                                         fromMapH: FromMap[R],
                                                                         fromMapT: FromMap[T]): FromMap[FieldType[K, V] :: T] =
        (m: Map[String, Any]) => for {
          v <- m.get(witness.value.name)
          r <- Typeable[Map[String, Any]].cast(v)
          h <- fromMapH(r)
          t <- fromMapT(m)
        } yield field[K](gen.from(h)) :: t
    }

    class ConvertHelper[A] {
      def from[R <: HList](m: Map[String, Any])(implicit
                                                gen: LabelledGeneric.Aux[A, R],
                                                fromMap: FromMap[R]): Option[A] =
        fromMap(m).map(gen.from)
    }

    def to[A]: ConvertHelper[A] = new ConvertHelper[A]

  }

}
