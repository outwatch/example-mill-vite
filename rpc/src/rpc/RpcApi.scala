package rpc

import cats.effect.IO
import upickle.default.ReadWriter
import java.security.SecureRandom

trait RpcApi {
  def registerDevice(deviceId: String): IO[Unit]

  def getPublicDeviceId: IO[String]
  def getOnDeviceMessages: IO[Vector[Message]]
  def getContacts: IO[Vector[PublicDeviceProfile]]

  def send(messageId: Int, publicDeviceId: String): IO[Boolean]
  def create(content: String): IO[Unit]
  def trust(contactPublicDeviceId: String): IO[Boolean]
}

case class Message(messageId: Int, content: String) derives ReadWriter

case class PublicDeviceProfile(publicDeviceId: String) derives ReadWriter

def generateSecureKey(length: Int): String = {
  val random = new SecureRandom()
  IArray.tabulate(length)(_ => wordList(random.nextInt(wordList.length))).mkString("-")
}
