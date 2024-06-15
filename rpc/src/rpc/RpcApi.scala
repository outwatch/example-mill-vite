package rpc

import cats.effect.IO
import upickle.default.ReadWriter

trait RpcApi {
  def send(messageId: Int, deviceId: String): IO[Unit]
  def create(content: String): IO[Unit]
  def getInbox(): IO[Vector[Message]]
  def trust(deviceId: String): IO[Unit]
}

case class Message(messageId: Int, content: String) derives ReadWriter
