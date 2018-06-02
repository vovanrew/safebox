package services

import com.google.inject._
import dto.{Profile, User}

@Singleton
class ProfileService @Inject() (userService: UserService, fileManagerService: FileManagerService) {

  def asembleProfile(name: String): Option[Profile] = {

    val user = userService.getUserWithName(name)
    val profile = user match {
      case Some(user) =>
        val files = fileManagerService.getUserFiles(user.id)
        Some(Profile(user, files))

      case None =>
        None
    }

    profile
  }

  def formUserProfile(user: User): Profile = {
    val files = fileManagerService.getUserFiles(user.id)
    Profile(user, files)
  }

}
