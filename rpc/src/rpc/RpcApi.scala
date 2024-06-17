package rpc

import cats.effect.IO
import upickle.default.ReadWriter
import java.security.SecureRandom

trait RpcApi {
  def registerDevice(deviceSecret: String): IO[Unit]

  def getDeviceAddress: IO[String]
  def getOnDeviceMessages: IO[Vector[Message]]
  def getContacts: IO[Vector[PublicDeviceProfile]]

  def send(messageId: Int, deviceAddress: String): IO[Boolean]
  def create(content: String): IO[Unit]
  def trust(deviceAddress: String): IO[Boolean]
}

case class Message(messageId: Int, content: String) derives ReadWriter

case class PublicDeviceProfile(deviceAddress: String) derives ReadWriter

def generateDeviceAddress(length: Int): String = {
  val random = new SecureRandom()
  IArray.tabulate(length)(_ => wordList(random.nextInt(wordList.length))).mkString("-")
}

def generateDeviceSecret(length: Int): String = {
  // TODO: UUID
  val chars  = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
  val random = new SecureRandom()
  IArray.fill(length)(chars(random.nextInt(chars.length))).mkString
}
