package rpc

import cats.effect.IO
import upickle.default.ReadWriter

trait RpcApi {
  def register(username: String, password: String): IO[Unit]
  def send(messageId: Int, userId: String): IO[Unit]
  def create(content: String): IO[Unit]
  def getInbox(): IO[Vector[Message]]
  def addContact(contactUserId: String): IO[Unit]
}

case class Message(messageId: Int, content: String) derives ReadWriter
