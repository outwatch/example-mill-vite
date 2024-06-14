package backend

import cats.effect.IO

object RpcApiImpl extends rpc.RpcApi {
  def send(messageId: Int, userId: String): IO[Int] = {
    ???
  }
  def create(content: Int): IO[Int] = {
    ???
  }
}
