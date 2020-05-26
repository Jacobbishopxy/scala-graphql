package com.github.jacobbishopxy.scalaGraphql.utils

import shapeless._

/**
 * Created by Jacob Xie on 5/26/2020
 */
object DefaultCaseClass {

  sealed trait Instantiate[T] {
    def instantiate: T
  }

  def apply[T](implicit g: Instantiate[T]): T = g.instantiate

  implicit def string: Instantiate[String] = new Instantiate[String] {
    override def instantiate: String = ""
  }

  implicit def int: Instantiate[Int] = new Instantiate[Int] {
    override def instantiate: Int = 0
  }

  implicit def boolean: Instantiate[Boolean] = new Instantiate[Boolean] {
    override def instantiate: Boolean = false
  }

  implicit def double: Instantiate[Double] = new Instantiate[Double] {
    override def instantiate: Double = .0
  }

  implicit def optionString: Instantiate[Option[String]] = new Instantiate[Option[String]] {
    override def instantiate: Option[String] = None
  }

  implicit def optionInt: Instantiate[Option[Int]] = new Instantiate[Option[Int]] {
    override def instantiate: Option[Int] = None
  }

  implicit def optionBoolean: Instantiate[Option[Boolean]] = new Instantiate[Option[Boolean]] {
    override def instantiate: Option[Boolean] = None
  }

  implicit def optionDouble: Instantiate[Option[Double]] = new Instantiate[Option[Double]] {
    override def instantiate: Option[Double] = None
  }

  implicit def hNil: Instantiate[HNil] =
    new Instantiate[HNil] {
      override def instantiate: HNil = HNil
    }

  implicit def hList[H, T <: HList](implicit
                                    headGen: Instantiate[H],
                                    tailGen: Instantiate[T]): Instantiate[H :: T] =
    new Instantiate[H :: T] {
      override def instantiate: H :: T = headGen.instantiate :: tailGen.instantiate
    }

  implicit def caseClass[T, G](implicit
                               ga: Generic.Aux[T, G],
                               inst: Instantiate[G]): Instantiate[T] =
    new Instantiate[T] {
      override def instantiate: T = ga.from(inst.instantiate)
    }

}

