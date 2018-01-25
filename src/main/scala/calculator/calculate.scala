package calculator

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer


import scala.io.StdIn
import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import org.slf4j.LoggerFactory

import scala.language.postfixOps

case class Scopes(jit: Option[String], scopes: List[Scope])
case class Scope(scope: String, actions: List[String])
case class JWT(iat: Long, jit: String, scopes: List[Scope])

case class JWTString(jwt: String)
case class Error(message: String, desc: String)
case class Response[A](data: Option[A], errors: Option[List[Error]])

case class DefaultResponse(status: String)


case class Add(x:Int, y: Int)
case class Subtract(x:Int, y:Int)
case class Multiply(x:Int, y:Int)
case class Divide(x:Int, y:Int)

object calculate extends App {

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  private val secretKey = System.getenv().getOrDefault("JWT_SECRET", "some-secret-key")
  private val header = JwtHeader("HS256")
  private val logger = LoggerFactory.getLogger(this.getClass)



  def json[A](status: StatusCode, a: A)(implicit e: Encoder[A]): HttpResponse = {
    HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`application/json`), a.asJson.noSpaces), status = status)
  }

  def createJWT(scopes: Scopes): String = {
    val claimsSet = JwtClaimsSet(JWT(
      System.currentTimeMillis(),
      scopes.jit.getOrElse(UUID.randomUUID().toString),
      scopes.scopes
    ).asJson.noSpaces)
    JsonWebToken(header, claimsSet, secretKey)
  }

  private def isJTIValid(jwt: String) = getClaims(jwt) match {
    //this functions check claims.jti to verify if he JWT ID is still valid
    case Some(claims) => true
    case None => false
  }

  private def getClaims(jwt: String) = jwt match {
    case JsonWebToken(_, claims, _) => decode[JWT](claims.asJsonString).toOption
    case _=> None
  }

  private def authenticated: Directive1[JWT] = {
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(jwt) if !isJTIValid(jwt) =>
        complete(StatusCodes.Unauthorized -> "JWT ID rejected.")

      case Some(jwt) if JsonWebToken.validate(jwt, secretKey) =>
        getClaims(jwt) match {
          case Some(value) => provide(value)
          case None => complete(StatusCodes.Unauthorized -> "JWT rejected.")
        }

      case _ => complete(StatusCodes.Unauthorized)
    }
  }

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
            case Right(subtract) => complete(StatusCodes.OK, s"\n${subtract.x - subtract.y}\n\n")
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

  println(s"The server is running at http://localhost:8080/")
  println("To stop the server press Ctrl+C")

  sys.addShutdownHook(system.terminate())
  val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)
}