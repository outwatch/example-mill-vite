package rpc

import cats.effect.IO
import upickle.default.ReadWriter
import java.security.SecureRandom

trait RpcApi {
  def registerDevice(deviceId: String): IO[Unit]
  def send(messageId: Int, deviceId: String): IO[Unit]
  def create(content: String): IO[Unit]
  def getInbox(): IO[Vector[Message]]
  def getPublicDeviceId(): IO[String]
  def trust(contactPublicDeviceId: String): IO[Unit]
}

case class Message(messageId: Int, content: String) derives ReadWriter

def generateSecureKey(length: Int): String = {
  val random = new SecureRandom()
  IArray.tabulate(length)(_ => wordList(random.nextInt(wordList.length))).mkString("-")
}
