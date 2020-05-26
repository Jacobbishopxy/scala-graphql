package com.github.jacobbishopxy.scalaGraphql.utils

import shapeless._
import shapeless.labelled.FieldType

/**
 * Created by Jacob Xie on 5/26/2020
 */
object ExpectedMetadata {

  sealed trait GetFieldTypes[T] {
    def getFieldTypes: scala.collection.Map[String, String]
  }

  def apply[T](implicit g: GetFieldTypes[T]): scala.collection.Map[String, String] = g.getFieldTypes

  implicit def hNil: GetFieldTypes[HNil] =
    new GetFieldTypes[HNil] {
      def getFieldTypes: scala.collection.Map[String, Nothing] = Map.empty
    }

  implicit def hCons[K <: Symbol, V, T <: HList](implicit
                                                 wit: Witness.Aux[K],
                                                 typ: Typeable[V],
                                                 rest: GetFieldTypes[T]): GetFieldTypes[FieldType[K, V] :: T] =
    new GetFieldTypes[FieldType[K, V] :: T] {
      def getFieldTypes: scala.collection.Map[String, String] = {

        rest.getFieldTypes + (wit.value.name -> typ.describe)
      }
    }

  implicit def caseClass[T, G](implicit
                               lg: LabelledGeneric.Aux[T, G],
                               rest: GetFieldTypes[G]): GetFieldTypes[T] =
    new GetFieldTypes[T] {
      def getFieldTypes: scala.collection.Map[String, String] = rest.getFieldTypes
    }
}

