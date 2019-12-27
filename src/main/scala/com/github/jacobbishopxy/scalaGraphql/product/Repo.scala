package com.github.jacobbishopxy.scalaGraphql.product

/**
 * Created by Jacob Xie on 12/25/2019
 */
class Repo {

  import Repo._

  private val Products = List(
    Product("1", "Cheesecake", "Tasty"),
    Product("2", "Health Potion", "+50 HP")
  )

  def product(id: String): Option[Product] =
    Products.find(_.id == id)

  def products: List[Product] = Products

}

object Repo {

  trait Identifiable {
    def id: String
  }

  case class Picture(width: Int, height: Int, url: Option[String])

  case class Product(id: String, name: String, description: String) extends Identifiable {
    def picture(size: Int): Picture =
      Picture(width = size, height = size, url = Some(s"//cdn.com/$size/$id.jpg"))
  }

}