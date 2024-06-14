package rpc

import cats.effect.IO

trait RpcApi {
  def send(messageId: Int, userId: String): IO[Int]
  def create(content: Int): IO[Int]
}
