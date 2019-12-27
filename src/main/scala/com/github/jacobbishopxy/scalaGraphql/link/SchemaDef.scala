package com.github.jacobbishopxy.scalaGraphql.link

import akka.http.scaladsl.model.DateTime
import sangria.ast.StringValue
import sangria.execution.{ExceptionHandler, HandledException}
import sangria.execution.deferred._
import sangria.schema._
import sangria.macros.derive._
import sangria.validation.Violation
import sangria.execution.deferred._
import sangria.marshalling.FromInput
import sangria.util.tag.@@


/**
 * Created by Jacob Xie on 12/26/2019
 */
object SchemaDef {

  import Repo._

  case object DateTimeCoerceViolation extends Violation {
    override def errorMessage: String = "Error parsing DateTime!"
  }

  implicit val GraphQLDateTime: ScalarType[DateTime] = ScalarType[DateTime](
    "DateTime",
    coerceOutput = (dt, _) => dt.toString,
    coerceInput = {
      case StringValue(dt, _, _, _, _) => DateTime.fromIsoDateTimeString(dt).toRight(DateTimeCoerceViolation)
      case _ => Left(DateTimeCoerceViolation)
    },
    coerceUserInput = {
      case s: String => DateTime.fromIsoDateTimeString(s).toRight(DateTimeCoerceViolation)
      case _ => Left(DateTimeCoerceViolation)
    }
  )

  val IdentifiableType: InterfaceType[ContextDef, Identifiable] = InterfaceType(
    "Identifiable",
    fields[ContextDef, Identifiable](
      Field("id", IntType, resolve = _.value.id)
    )
  )

  val linkByUserRel: Relation[Link, Link, Int] = Relation[Link, Int]("byUser", l => Seq(l.postedBy))
  val voteByUserRel: Relation[Vote, Vote, Int] = Relation[Vote, Int]("byUser", v => Seq(v.userId))
  val voteByLinkRel: Relation[Vote, Vote, Int] = Relation[Vote, Int]("byLink", v => Seq(v.linkId))

  implicit lazy val UserType: ObjectType[ContextDef, User] = deriveObjectType[ContextDef, User](
    Interfaces[ContextDef, User](IdentifiableType),
    AddFields(
      Field("links", ListType(LinkType), resolve = c => linksFetcher.deferRelSeq(linkByUserRel, c.value.id)),
      Field("votes", ListType(VoteType), resolve = c => votesFetcher.deferRelSeq(voteByUserRel, c.value.id))
    )
  )

  implicit lazy val LinkType: ObjectType[ContextDef, Link] = deriveObjectType[ContextDef, Link](
    Interfaces[ContextDef, Link](IdentifiableType),
    ReplaceField(
      "postedBy",
      Field("postedBy", UserType, resolve = c => usersFetcher.defer(c.value.postedBy))
    ),
    AddFields(
      Field("votes", ListType(VoteType), resolve = c => votesFetcher.deferRelSeq(voteByLinkRel, c.value.id))
    )
  )

  val linksFetcher: Fetcher[ContextDef, Link, Link, Int] = Fetcher.rel(
    (ctx: ContextDef, ids: Seq[Int]) => ctx.repo.getLinks(ids),
    (ctx: ContextDef, ids: RelationIds[Link]) => ctx.repo.getLinksByUserIds(ids(linkByUserRel))
  )

  val usersFetcher: Fetcher[ContextDef, User, User, Int] = Fetcher(
    (ctx: ContextDef, ids: Seq[Int]) => ctx.repo.getUsers(ids)
  )

  implicit val VoteType: ObjectType[ContextDef, Vote] = deriveObjectType[ContextDef, Vote](
    Interfaces[ContextDef, Vote](IdentifiableType),
    ExcludeFields("userId"),
    AddFields(
      Field("user", UserType, resolve = c => usersFetcher.defer(c.value.userId))
    )
  )

  val votesFetcher: Fetcher[ContextDef, Vote, Vote, Int] = Fetcher.rel(
    (ctx: ContextDef, ids: Seq[Int]) => ctx.repo.getVotes(ids),
    (ctx: ContextDef, ids: RelationIds[Vote]) => ctx.repo.getVotesByRelationIds(ids)
  )

  val Id: Argument[Int] = Argument("id", IntType)
  val Ids: Argument[Seq[Int @@ FromInput.CoercedScalaResult]] = Argument("ids", ListInputType(IntType))

  val QueryType: ObjectType[ContextDef, Unit] =
    ObjectType(
      "Query",
      fields[ContextDef, Unit](
        Field("allLinks", ListType(LinkType), resolve = c => c.ctx.repo.allLinks),
        Field(
          "link",
          OptionType(LinkType),
          arguments = Id :: Nil,
          resolve = c => linksFetcher.deferOpt(c.arg[Int]("id"))
        ),
        Field(
          "links",
          ListType(LinkType),
          arguments = List(Ids),
          resolve = c => linksFetcher.deferSeq(c.arg(Ids))
        ),
        Field(
          "users",
          ListType(UserType),
          arguments = List(Ids),
          resolve = c => usersFetcher.deferSeq(c.arg(Ids))
        ),
        Field(
          "votes",
          ListType(VoteType),
          arguments = List(Ids),
          resolve = c => votesFetcher.deferSeq(c.arg(Ids))
        )
      )
    )


  /**
   * Mutation
   */

  import sangria.marshalling.circe._
  import io.circe.{Decoder, Encoder}
  import io.circe.syntax._

  implicit val decodeAuthProviderEmail: Decoder[AuthProviderEmail] =
    Decoder.forProduct2("email", "password")(AuthProviderEmail.apply)

//  implicit val encodeAuthProviderEmail: Encoder[AuthProviderEmail] =
//    Encoder.forProduct2("email", "password")(u =>
//      (u.email, u.password)
//    )

  implicit val decodeAuthProviderSignUpData: Decoder[AuthProviderSignUpData] =
    Decoder.forProduct1("data")(AuthProviderSignUpData.apply)

//  implicit val encodeAuthProviderSignUpData: Encoder[AuthProviderSignUpData] =
//    Encoder.forProduct1("data")(i => (i.data))


  //  implicit val authProviderEmailFormat =
  //    AuthProviderEmail.asJson
  //  implicit val authProviderSignUpDataFormat =
  //    AuthProviderSignUpData.asJson

  implicit val AuthProviderEmailInputType: InputObjectType[AuthProviderEmail] =
    deriveInputObjectType[AuthProviderEmail](
      InputObjectTypeName("AUTH_PROVIDER_EMAIL")
    )

  implicit val AuthProviderSignUpDataInputType: InputObjectType[AuthProviderSignUpData] =
    deriveInputObjectType[AuthProviderSignUpData]()

  val NameArg: Argument[String] = Argument("name", StringType)
  val AuthProviderArg: Argument[AuthProviderSignUpData] =
    Argument("authProvider", AuthProviderSignUpDataInputType)
  val UrlArg: Argument[String] = Argument("url", StringType)
  val DescArg: Argument[String] = Argument("description", StringType)
  val PostedByArg: Argument[Int] = Argument("postedById", IntType)
  val LinkIdArg: Argument[Int] = Argument("linkId", IntType)
  val UserIdArg: Argument[Int] = Argument("userId", IntType)
  val EmailArg: Argument[String] = Argument("email", StringType)
  val PasswordArg: Argument[String] = Argument("password", StringType)

  val MutationType: ObjectType[ContextDef, Unit] =
    ObjectType(
      "Mutation",
      fields[ContextDef, Unit](
        Field(
          "createUser",
          UserType,
          arguments = NameArg :: AuthProviderArg :: Nil,
          resolve = c => c.ctx.repo.createUser(c.arg(NameArg), c.arg(AuthProviderArg))
        ),
        Field(
          "createLink",
          LinkType,
          arguments = UrlArg :: DescArg :: PostedByArg :: Nil,
          resolve = c => c.ctx.repo.createLink(c.arg(UrlArg), c.arg(DescArg), c.arg(PostedByArg))
        ),
        Field(
          "createVote",
          VoteType,
          arguments = LinkIdArg :: UserIdArg :: Nil,
          resolve = c => c.ctx.repo.createVote(c.arg(LinkIdArg), c.arg(UserIdArg))
        ),
        Field(
          "login",
          UserType,
          arguments = EmailArg :: PasswordArg :: Nil,
          resolve = c => UpdateCtx(
            c.ctx.login(c.arg(EmailArg), c.arg(PasswordArg))
          ) { user => c.ctx.copy(currentUser = Some(user)) }
        )
      )
    )

  val Resolver: DeferredResolver[ContextDef] =
    DeferredResolver.fetchers(linksFetcher, usersFetcher, votesFetcher)

  val ErrorHandler: ExceptionHandler = ExceptionHandler {
    case (_, AuthenticationException(message)) => HandledException(message)
    case (_, AuthorisationException(message)) => HandledException(message)
  }

  val SchemaDefinition: Schema[ContextDef, Unit] = Schema(QueryType, Some(MutationType))

}

















