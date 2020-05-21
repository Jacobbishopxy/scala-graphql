package com.github.jacobbishopxy

import java.lang.reflect.Modifier

import sangria.schema.Context
import slick.jdbc.JdbcProfile


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

  trait DatabaseComponent {
    val driver: JdbcProfile
    val dbCfg: String

    val db: driver.backend.DatabaseDef = driver.backend.Database.forConfig(dbCfg)
  }

}
