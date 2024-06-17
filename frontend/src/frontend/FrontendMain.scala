package frontend

import cats.effect.IO
import cats.effect.IOApp
import outwatch.*
import outwatch.dsl.*
import colibri.*
import colibri.reactive.*
import authn.frontend.*
import authn.frontend.authnJS.keratinAuthn.distTypesMod.Credentials
import org.scalajs.dom.window.localStorage
import org.scalajs.dom
import org.scalajs.dom.{window, Navigator, Position}
import scala.scalajs.js

// Outwatch documentation: https://outwatch.github.io/docs/readme.html

object Main extends IOApp.Simple {
  def run = lift {

    val deviceId = unlift(RpcClient.getDeviceId).getOrElse(rpc.generateSecureKey(10))
    localStorage.setItem("deviceId", deviceId)
    unlift(RpcClient.call.registerDevice(deviceId))

//    val positionObservable = Observable.create { observer =>
//      val watchId = window.navigator.geolocation.watchPosition(position => observer.unsafeOnNext(position))
//      Cancelable(() => window.navigator.geolocation.clearWatch(watchId))
//    }

    val refreshTrigger = VarEvent[Unit]()

    val myComponent = {
      div(
        addContact,
        showPublicDeviceId,
        createMessage(refreshTrigger),
        inbox(refreshTrigger),
        // camera,
      )
    }

    // render the component into the <div id="app"></div> in index.html
    unlift(Outwatch.renderReplace[IO]("#app", myComponent))
  }
}

def camera = {
  val detector = BarcodeDetector(new {
    formats = js.Array("qr_code")
  })

  video(
    height := "200px",
    width := "200px",
    VMod.attr[Boolean]("autoplay", identity) := true,
    VMod.prop("srcObject") <-- RxLater.future(
      window.navigator.mediaDevices
        .getUserMedia(new {
          video = true
          audio = false
        })
        .toFuture
    ),
    onDomMount
      .transform(x =>
        Observable
          .intervalMillis(100)
          .switchMap(_ =>
            x.mapFuture { element =>
              println("DETECTING...")
              detector.detect(element).toFuture
            }
          )
      )
      .foreach { result =>
        println(result.map(_.rawValue).mkString(", "))
      },
  )
}

def createMessage(refreshTrigger: VarEvent[Unit]) = {
  import webcodegen.shoelace.SlButton.{value as _, *}
  import webcodegen.shoelace.SlInput.{value as _, *}

  val messageString = Var("").transformVarRead(rx => Rx.observableSync(rx.observable.merge(refreshTrigger.observable.as(""))))

  div(
    slInput(placeholder := "type message", value <-- messageString, onSlChange.map(_.target.value) --> messageString),
    slButton("create", onClick.mapEffect(_ => RpcClient.call.create(messageString.now())).as(()) --> refreshTrigger),
  )
}

def showPublicDeviceId = {
  import webcodegen.shoelace.SlCopyButton.{value as _, *}
  import webcodegen.shoelace.SlQrCode.*

  div(
    b("Your public device id"),
    RpcClient.call.getPublicDeviceId.map { publicDeviceId =>
      VMod(
        div(publicDeviceId),
        slCopyButton(value := publicDeviceId),
        slQrCode(value := publicDeviceId),
      )
    },
  )
}

def addContact = {
  import webcodegen.shoelace.SlInput.{value as _, *}
  import webcodegen.shoelace.SlButton.{value as _, *}

  val contactPublicDeviceId = Var("")

  div(
    display.flex,
    slInput(
      placeholder := "Public device id of contact",
      value <-- contactPublicDeviceId,
      onSlChange.map(_.target.value) --> contactPublicDeviceId,
    ),
    slButton("Add", onClick(contactPublicDeviceId).foreachEffect(RpcClient.call.trust(_).void)),
  )
}

def inbox(refreshTrigger: RxEvent[Unit]) = {
  import webcodegen.shoelace.SlButton.{value as _, *}
  import webcodegen.shoelace.SlSelect.{onSlFocus as _, onSlBlur as _, onSlAfterHide as _, open as _, *}
  import webcodegen.shoelace.SlOption.{value as _, *}
  import webcodegen.shoelace.SlDialog.*
  import webcodegen.shoelace.SlDialog

  val contacts = RxLater.effect(RpcClient.call.getContacts)

  val inboxStream = refreshTrigger.observable.prepend(()).asEffect(RpcClient.call.getOnDeviceMessages)

  val selectedProfile = VarLater[String]()

  div(
    checked := true,
    inboxStream.map(_.map { message =>
      val openDialog = Var(false)
      div(
        display.flex,
        div(message.content),
        slButton("Send to contact", onClick.as(true) --> openDialog),
        slDialog(
          open <-- openDialog,
          onSlAfterHide.onlyOwnEvents.as(false) --> openDialog,
          div(
            b(message.content),
            height := "500px",
            slSelect(
              onSlChange.map(_.target.value).collect { case s: String => s } --> selectedProfile,
              contacts.map(_.map { contact =>
                slOption(value := contact.publicDeviceId, contact.publicDeviceId)
              }),
            ),
          ),
          div(
            slotFooter,
            display.flex,
            slButton("Send to contact", onClick(selectedProfile).foreachEffect(RpcClient.call.send(message.messageId, _).void)),
          ),
        ),
      )
    }),
  )
}
