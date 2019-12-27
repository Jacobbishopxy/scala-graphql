

/**
 * Created by Jacob Xie on 12/26/2019
 */
object Dev extends App {

  import io.circe.{ Decoder, Encoder }
  import io.circe.syntax._

  case class User(id: Long, firstName: String, lastName: String)

  implicit val decodeUser: Decoder[User] =
    Decoder.forProduct3("id", "first_name", "last_name")(User.apply)

  implicit val encodeUser: Encoder[User] =
    Encoder.forProduct3("id", "first_name", "last_name")(u =>
      (u.id, u.firstName, u.lastName)
    )

  val foo = User(233L, "jacob", "bishop")

  println(foo.asJson)

}
