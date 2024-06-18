package rpc

import cats.effect.IO
import upickle.default.ReadWriter
import java.security.SecureRandom

trait RpcApi {
  def registerDevice(deviceSecret: String): IO[Unit]

  def getDeviceAddress: IO[String]
  def getMessagesOnDevice: IO[Vector[Message]]
  def getMessagesAtLocation(location: Location.GCS): IO[Vector[Message]]
  def getContacts: IO[Vector[String]]

  def sendMessage(messageId: Int, deviceAddress: String): IO[Boolean]
  def pickupMessage(messageId: Int, location: Location.GCS): IO[Boolean]
  def dropMessage(messageId: Int, location: Location.GCS): IO[Boolean]
  def createMessage(content: String): IO[Unit]
  def addContact(targetDeviceAddress: String): IO[Boolean]
}

enum Location derives ReadWriter:
  case GCS(lat: Double, lon: Double)
  case WebMercator(x: Double, y: Double)

  def toWebMercator: Location.WebMercator = this match
    case Location.GCS(lat, lon) =>
      import math._
      val x = 6378137.0 * (lon * Pi / 180.0)
      val y = 6378137.0 * log(tan((Pi / 4.0) + (lat * Pi / 360.0)))
      Location.WebMercator(x, y)
    case wm: Location.WebMercator => wm

case class Message(messageId: Int, content: String) derives ReadWriter

def generateSecureDeviceAddress(length: Int): String = {
  val random = new SecureRandom()
  IArray.tabulate(length)(_ => wordList(random.nextInt(wordList.length))).mkString("-")
}
