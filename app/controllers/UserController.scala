package controllers

import com.google.inject._
import dto._
import play.api.mvc.{AbstractController, ControllerComponents}
import dto.User._
import dto.Error._
import play.api.libs.json.{JsError, JsSuccess, Json}
import services.UserService
import dto.JsonResult._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserController @Inject() (scc: SecuredControllerComponents, userService: UserService)
  extends SecuredController(scc: SecuredControllerComponents) {

  def register = Action(parse.json).async { request =>
    Future (
      request.body.validate[User] match {

        case e: JsError =>
          BadRequest(Json.toJson(JsonResult(Unsuccess.name, Error("Json validation error"))))

        case user: JsSuccess[User] =>
          userService.createUser(user.get) match {
            case None =>
              BadRequest(Json.toJson(JsonResult(Unsuccess.name, Error("There is such email"))))

            case Some(u) =>
              Ok(Json.toJson(JsonResult(data = u)))
        }
      }
    )
  }

  def login = Action(parse.json).async { implicit request =>
    Future (
      request.body.validate[User] match {

        case e: JsError =>
          BadRequest(Json.toJson(JsonResult(Unsuccess.name, Error("Json validation error"))))

        case user: JsSuccess[User] =>
          userService.validateUser(user.get) match {
            case None =>
              BadRequest(Json.toJson(JsonResult(Unsuccess.name, Error("Invalid data"))))

            case Some(u) =>
              Ok(Json.toJson(JsonResult(data = u)))
                .withSession(("id", u.id.toString), ("name", u.name), ("email", u.email))
          }
      }
    )
  }

  def profile(name: String) = AuthenticatedAction.async { implicit request =>
    Future (
        userService.getUserWithName(name) match {
          case None =>
            NotFound(Json.toJson(JsonResult(Unsuccess.name, Error("There is no such user"))))

          case Some(u) =>
            Ok(Json.toJson(JsonResult(data = u)))
        }
    )
  }
}
