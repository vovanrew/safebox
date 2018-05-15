package utils

import java.io._
import java.net.{HttpURLConnection, URL, URLConnection}
import java.util.Base64

import sys.process._
import java.net.URL
import java.io.File

import akka.actor.Cancellable

import scala.reflect.io.Path

object Utils {

  def fileDownloader(url: String, filePath: Path) = {
    new URL(url) #> filePath.jfile !
  }

}
