package calculator

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

case class Add(x:Int, y: Int)
case class Subtract(x:Int, y:Int)
case class Multiply(x:Int, y:Int)
case class Divide(x:Int, y:Int)

object calculate extends App {

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()

  implicit val executionContext = system.dispatcher

  val routes =
    path("add") {
//      get {
//        val result = 6
//        complete(HttpEntity(ContentTypes.`application/json`,
//          s"""
//            |{
//            |  "result": $result
//            |}
//            |""".stripMargin))
//      }~
      post {
        entity(as[String]) { e =>
          decode[Add](e) match {
            case Left(e) => complete(StatusCodes.BadRequest, e.getMessage)
            case Right(add) => complete(StatusCodes.OK, s"\n${add.x + add.y}\n\n")
          }
        }
      }
    } ~
    path("subtract") {
      post {
        entity(as[String]) { e =>
          decode[Subtract](e) match {
            case Left(e) => complete(StatusCodes.BadRequest, e.getMessage)
            case Right(subtract) => complete((StatusCodes.OK, s"\n${subtract.x - subtract.y}\n\n"))
          }
        }
      }
    }~
    path("multiply") {
      post {
        entity(as[String]) { e =>
          decode[Multiply](e) match {
            case Left(e) => complete(StatusCodes.BadRequest, s"\n${e.getMessage}\n")
            case Right(multiply) => complete(StatusCodes.OK, s"\n${multiply.x * multiply.y}\n\n")
          }

        }
      }
    }~
    path("divide") {
      post {
         entity(as[String]) { e =>
           decode[Divide](e) match {
             case Left(e) => complete(StatusCodes.BadRequest, e.getMessage)
             case Right(divide) => complete(StatusCodes.OK, s"\n${divide.x /divide.y}\n\n")
           }
         }
      }
    }
    val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)

    println(s"The server is running at http://localhost:8080/\nPress RETURN to stop.....")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
