package controllers

import com.google.inject._
import dto.{Error, JsonResult, Success, Unsuccess}
import play.api.libs.json.Json
import services.ProfileService
import scala.concurrent.Future
import dto.Profile._
import scala.concurrent.ExecutionContext.Implicits.global

class ProfileController @Inject() (scc: SecuredControllerComponents, profileService: ProfileService)
  extends SecuredController(scc: SecuredControllerComponents) {

  def profile(name: String) = AuthenticatedAction.async { implicit request =>
    Future (
      profileService.asembleProfile(name) match {
        case None =>
          NotFound(Json.toJson(JsonResult(Unsuccess.name, Error("There is no such user"))))

        case Some(u) =>
          Ok(Json.toJson(JsonResult(Success.name, Json.toJson(u).toString())))
      }
    )
  }
}
