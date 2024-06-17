package rpc

import cats.effect.IO
import upickle.default.ReadWriter
import java.security.SecureRandom

trait RpcApi {
  def registerDevice(deviceSecret: String): IO[Unit]

  def getDeviceAddress: IO[String]
  def getOnDeviceMessages: IO[Vector[Message]]
  def getContacts: IO[Vector[String]]

  def sendMessage(messageId: Int, deviceAddress: String): IO[Boolean]
  def createMessage(content: String): IO[Unit]
  def addContact(targetDeviceAddress: String): IO[Boolean]
}

case class Message(messageId: Int, content: String) derives ReadWriter

def generateSecureDeviceAddress(length: Int): String = {
  val random = new SecureRandom()
  IArray.tabulate(length)(_ => wordList(random.nextInt(wordList.length))).mkString("-")
}

def generateSecureDeviceSecret(length: Int): String = {
  // TODO: UUID
  val chars  = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
  val random = new SecureRandom()
  IArray.fill(length)(chars(random.nextInt(chars.length))).mkString
}
