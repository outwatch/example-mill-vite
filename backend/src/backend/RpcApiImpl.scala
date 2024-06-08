package backend

import cats.effect.IO

object RpcApiImpl extends rpc.RpcApi {
  def fun(x: Int) = IO.pure(x + 1)
}
