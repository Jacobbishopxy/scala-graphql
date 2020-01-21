package com.github.jacobbishopxy

import java.lang.reflect.Modifier

import sangria.schema.Context
import slick.jdbc.JdbcProfile
import slick.lifted.RepShape

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}


/**
 * Created by Jacob Xie on 1/3/2020
 */
package object scalaGraphql {


  def getField[T](c: Context[T, Unit]): Vector[String] =
    c.astFields.head.selections.map(_.renderCompact)

  object Copyable {

    def copy[T](o: T, m: Map[String, Any]): T = {
      val copier = new Copier(o.getClass)
      copier(o, m.toList: _*)
    }

    /**
     * Utility class for providing copying of a designated case class with minimal overhead.
     */
    class Copier(cls: Class[_]) {
      private val ctor = cls.getConstructors.apply(0)
      private val getters = cls.getDeclaredFields
        .filter {
          f =>
            val m = f.getModifiers
            Modifier.isPrivate(m) && Modifier.isFinal(m) && !Modifier.isStatic(m)
        }
        .take(ctor.getParameterTypes.length)
        .map(f => cls.getMethod(f.getName))

      /**
       * A reflective, non-generic version of case class copying.
       */
      def apply[T](o: T, v: (String, Any)*): T = {
        val byIx = v.map {
          case (name, value) =>
            val ix = getters.indexWhere(_.getName == name)
            if (ix < 0) throw new IllegalArgumentException("Unknown field: " + name)
            (ix, value.asInstanceOf[Object])
        }.toMap

        val args = getters.indices.map {
          i =>
            byIx.getOrElse(i, getters(i).invoke(o))
        }
        ctor.newInstance(args: _*).asInstanceOf[T]
      }
    }
  }

  trait DBComponent {
    val driver: JdbcProfile
    val dbCfg: String

    val db: driver.backend.DatabaseDef = driver.backend.Database.forConfig(dbCfg)
  }

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

  trait DynHelper extends SlickDynamic with DBComponent {

    val queryTimeout: FiniteDuration = 30.seconds

    import driver.api._
    import Copyable.copy

    case class DynCol[T <: Table[_]](col: String) {
      def str: Dynamic[T, String] = Dynamic[T, String](_.column(col))
      def dbl: Dynamic[T, Double] = Dynamic[T, Double](_.column(col))
      def int: Dynamic[T, Int] = Dynamic[T, Int](_.column(col))
      def optStr: Dynamic[T, Option[String]] = Dynamic[T, Option[String]](_.column(col))
      def optDbl: Dynamic[T, Option[Double]] = Dynamic[T, Option[Double]](_.column(col))
      def optInt: Dynamic[T, Option[Int]] = Dynamic[T, Option[Int]](_.column(col))
    }

    def constructDyn[T](m: Map[String, T], fields: Seq[String]): Seq[T] =
      fields.foldLeft(Seq.empty[T]) {
        case (acc, ele) => m.get(ele).fold(acc)(acc :+ _)
      }

    def resConvert[R](defaultCaseClass: R, fields: Seq[String], res: Seq[Seq[Any]]): List[R] =
      res.foldLeft(List.empty[R]) {
        case (acc, ele) =>
          Try(copy(defaultCaseClass, fields.zip(ele).toMap)) match {
            case Success(v) =>
              println(v)
              acc :+ v
            case Failure(_) =>
              acc
          }
      }

    def constructQueryFn[T <: Table[_], C, R](fieldMap: Map[String, Dynamic[T, _]],
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

}
