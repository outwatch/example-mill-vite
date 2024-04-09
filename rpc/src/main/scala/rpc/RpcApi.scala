package rpc

import scala.concurrent.Future
import cats.effect.IO
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

trait RpcApi {
  def fun(a: Int): IO[Int]
}

object JsonCodecs {
  given JsonValueCodec[Int] = JsonCodecMaker.make
}
