package rpc

import cats.effect.IO

trait RpcApi {
  def fun(a: Int): IO[Int]
}
