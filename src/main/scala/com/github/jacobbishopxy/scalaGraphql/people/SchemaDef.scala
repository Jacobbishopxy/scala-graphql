package com.github.jacobbishopxy.scalaGraphql.people

import sangria.execution.deferred.{Fetcher, HasId, Relation, RelationIds}
import sangria.macros.derive._
import sangria.schema._

/**
 * Created by Jacob Xie on 12/25/2019
 */
object SchemaDef {

  import Repo._

  def constantComplexity[Ctx](complexity: Double): (Ctx, Args, Double) => Double =
    (_: Ctx, _: Args, child: Double) ⇒ child + complexity

  val friend: Relation[Person, (Seq[String], Person), String] = Relation[Person, (Seq[String], Person), String]("friend", _._1, _._2)

  val personFetcher: Fetcher[Repo, Person, (Seq[String], Person), String] = Fetcher.relCaching(
    (repo: Repo, ids: Seq[String]) ⇒ repo.people(ids),
    (repo: Repo, ids: RelationIds[Person]) ⇒ repo.findFriends(ids(friend))
  )(HasId(_.id))

  val PersonType: ObjectType[Unit, Person] = deriveObjectType(
    DocumentField("firstName", "What you yell at me"),
    DocumentField("lastName", "What you yell at me when I've been bad"),
    DocumentField("username", "Log in as this"),
    AddFields(
      Field(
        "fullName",
        StringType,
        description = Some("A name sandwich"),
        resolve = c ⇒ s"${c.value.firstName} ${c.value.lastName}"
      ),
      Field(
        "friends",
        ListType(PersonType),
        description = Some("People who lent you money"),
        complexity = Some(constantComplexity(50)),
        resolve = c ⇒ personFetcher.deferRelSeq(friend, c.value.id))
    )
  )

  val QueryType: ObjectType[Repo, Unit] =
    ObjectType(
      "Query",
      fields[Repo, Unit](
        Field("allPeople", ListType(PersonType),
          description = Some("Everyone, everywhere"),
          complexity = Some(constantComplexity(100)),
          resolve = _.ctx.allPeople
        ),
        Field("person", OptionType(PersonType),
          arguments = Argument("id", StringType) :: Nil,
          complexity = Some(constantComplexity(10)),
          resolve = c ⇒ personFetcher.deferOpt(c.arg[String]("id"))
        )
      )
    )
}


