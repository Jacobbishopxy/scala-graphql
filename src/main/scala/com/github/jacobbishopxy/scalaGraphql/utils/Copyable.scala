package com.github.jacobbishopxy.scalaGraphql.utils

import java.lang.reflect.Modifier

/**
 * Created by Jacob Xie on 5/26/2020
 */
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

