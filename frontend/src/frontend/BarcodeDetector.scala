package frontend

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.Promise

@js.native
@JSGlobal
class BarcodeDetector(options: BarcodeDetectorOptions = js.native) extends js.Object {
  def detect(image: js.Any): Promise[js.Array[DetectedBarcode]] = js.native
}

trait BarcodeDetectorOptions extends js.Object {
  var formats: js.UndefOr[js.Array[String]] = js.undefined
}

trait DetectedBarcode extends js.Object {
  val rawValue: String
}
