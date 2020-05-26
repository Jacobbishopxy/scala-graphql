import shapeless._
import shapeless.labelled._


/**
 * Created by Jacob Xie on 5/25/2020
 */
object DevCaseClassToMap extends App {

  object ExpectedMetadata {

    sealed trait GetFieldTypes[T] {
      def getFieldTypes(renameDict: Option[Map[String, String]] = None): Map[String, String]
    }

    def apply[T](renameDict: Option[Map[String, String]] = None)
                (implicit g: GetFieldTypes[T]): Map[String, String] = g.getFieldTypes(renameDict)

    implicit def hNil: GetFieldTypes[HNil] =
      new GetFieldTypes[HNil] {
        def getFieldTypes(renameDict: Option[Map[String, String]] = None): Map[String, Nothing] = Map.empty
      }

    implicit def hCons[K <: Symbol, V, T <: HList](implicit
                                                   wit: Witness.Aux[K],
                                                   typ: Typeable[V],
                                                   rest: GetFieldTypes[T]): GetFieldTypes[FieldType[K, V] :: T] =
      new GetFieldTypes[FieldType[K, V] :: T] {
        def getFieldTypes(renameDict: Option[Map[String, String]] = None): Map[String, String] = {

          val s: (String, String) = renameDict match {
            case None => wit.value.name -> typ.describe
            case Some(d) => d.getOrElse(wit.value.name, wit.value.name) -> typ.describe
          }
          rest.getFieldTypes(renameDict) + s
        }
      }

    implicit def caseClass[T, G](implicit
                                 lg: LabelledGeneric.Aux[T, G],
                                 rest: GetFieldTypes[G]): GetFieldTypes[T] =
      new GetFieldTypes[T] {
        def getFieldTypes(renameDict: Option[Map[String, String]] = None): Map[String, String] =
          rest.getFieldTypes(renameDict)
      }
  }

  case class Student(id: Int, name: String, ext: Option[String], ext1: Option[Boolean])

  val foo = ExpectedMetadata[Student](Some(Map("name" -> "ha")))
  println(foo)


}
