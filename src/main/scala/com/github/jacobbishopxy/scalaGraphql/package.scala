package com.github.jacobbishopxy

import sangria.schema.Context


/**
 * Created by Jacob Xie on 1/3/2020
 */
package object scalaGraphql {

  def getField[T](c: Context[T, Unit]): Vector[String] =
    c.astFields.head.selections.map(_.renderCompact)

}

