package ru.ispras.lingvodoc.frontend.app.controllers

import com.greencatsoft.angularjs.core.{Event, Scope}
import com.greencatsoft.angularjs.{AbstractController, injectable}
import org.scalajs.dom._
import org.scalajs.dom.raw.HTMLInputElement
import ru.ispras.lingvodoc.frontend.app.controllers.common.{DictionaryTable, GroupValue, Value}
import ru.ispras.lingvodoc.frontend.app.exceptions.ControllerException
import ru.ispras.lingvodoc.frontend.app.model._
import ru.ispras.lingvodoc.frontend.app.services._
import ru.ispras.lingvodoc.frontend.app.utils.LingvodocExecutionContext.Implicits.executionContext
import ru.ispras.lingvodoc.frontend.app.utils.Utils
import ru.ispras.lingvodoc.frontend.extras.facades.{WaveSurfer, WaveSurferOpts}

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}


@js.native
trait ViewDictionaryModalScope extends Scope {
  var dictionaryTable: DictionaryTable = js.native
  var count: Int = js.native
  var offset: Int = js.native
  var size: Int = js.native
}

@injectable("ViewDictionaryModalController")
class ViewDictionaryModalController(scope: ViewDictionaryModalScope,
                                    modal: ModalService,
                                    instance: ModalInstance[Seq[Entity]],
                                    backend: BackendService,
                                    params: js.Dictionary[js.Function0[js.Any]])
  extends AbstractController[ViewDictionaryModalScope](scope) {

  private[this] val dictionaryClientId = params("dictionaryClientId").asInstanceOf[Int]
  private[this] val dictionaryObjectId = params("dictionaryObjectId").asInstanceOf[Int]
  private[this] val perspectiveClientId = params("perspectiveClientId").asInstanceOf[Int]
  private[this] val perspectiveObjectId = params("perspectiveObjectId").asInstanceOf[Int]
  private[this] val linkPerspectiveClientId = params("linkPerspectiveClientId").asInstanceOf[Int]
  private[this] val linkPerspectiveObjectId = params("linkPerspectiveObjectId").asInstanceOf[Int]
  private[this] val lexicalEntry = params("lexicalEntry").asInstanceOf[LexicalEntry]
  private[this] val field = params("field").asInstanceOf[Field]
  private[this] val links = params("links").asInstanceOf[js.Array[Link]]

  private[this] val dictionaryId = CompositeId(dictionaryClientId, dictionaryObjectId)
  private[this] val perspectiveId = CompositeId(perspectiveClientId, perspectiveObjectId)
  private[this] val linkPerspectiveId = CompositeId(linkPerspectiveClientId, linkPerspectiveObjectId)

  private[this] var perspectiveTranslation: Option[TranslationGist] = None

  scope.count = 0
  scope.offset = 0
  scope.size = 20

  private[this] var createdEntities = Seq[Entity]()

  private[this] var dataTypes = Seq[TranslationGist]()
  private[this] var perspectiveFields = Seq[Field]()
  private[this] var linkedPerspectiveFields = Seq[Field]()

  // wavesurfer
  private [this] var waveSurfer: Option[WaveSurfer] = None
  private var _pxPerSec = 50 // minimum pxls per second, all timing is bounded to it
  val pxPerSecStep = 30 // zooming step
  // zoom in/out step; fake value to avoid division by zero; on ws load, it will be set correctly
  private var _duration: Double = 42.0
  var fullWSWidth = 0.0 // again, will be known after audio load
  var wsHeight = 128
  var soundMarkup: Option[String] = None



  load()


  @JSExport
  def createWaveSurfer(): Unit = {
    if (waveSurfer.isEmpty) {
      // params should be synchronized with sm-ruler css
      val wso = WaveSurferOpts("#waveform", waveColor = "violet", progressColor = "purple",
        cursorWidth = 1, cursorColor = "red",
        fillParent = true, minPxPerSec = pxPerSec, scrollParent = false,
        height = wsHeight)
      waveSurfer = Some(WaveSurfer.create(wso))
    }
  }

  def pxPerSec = _pxPerSec

  def pxPerSec_=(mpps: Int) = {
    _pxPerSec = mpps
    waveSurfer.foreach(_.zoom(mpps))
  }

  @JSExport
  def play(soundAddress: String) = {
    (waveSurfer, Some(soundAddress)).zipped.foreach((ws, sa) => {
      ws.load(sa)
    })
  }

  @JSExport
  def playPause() = waveSurfer.foreach(_.playPause())

  @JSExport
  def play(start: Int, end: Int) = waveSurfer.foreach(_.play(start, end))

  @JSExport
  def zoomIn() = { pxPerSec += pxPerSecStep; }

  @JSExport
  def zoomOut() = { pxPerSec -= pxPerSecStep; }


  @JSExport
  def viewSoundMarkup(soundAddress: String, markupAddress: String) = {
    val options = ModalOptions()
    options.templateUrl = "/static/templates/modal/soundMarkup.html"
    options.controller = "SoundMarkupController"
    options.backdrop = false
    options.keyboard = false
    options.size = "lg"
    options.resolve = js.Dynamic.literal(
      params = () => {
        js.Dynamic.literal(
          soundAddress = soundAddress.asInstanceOf[js.Object],
          markupAddress = markupAddress.asInstanceOf[js.Object],
          dictionaryClientId = dictionaryClientId.asInstanceOf[js.Object],
          dictionaryObjectId = dictionaryObjectId.asInstanceOf[js.Object]
        )
      }
    ).asInstanceOf[js.Dictionary[js.Any]]

    val instance = modal.open[Unit](options)
  }

  @JSExport
  def dataTypeString(dataType: TranslationGist): String = {
    dataType.atoms.find(a => a.localeId == 2) match {
      case Some(atom) =>
        atom.content
      case None => throw new ControllerException("")
    }
  }


  @JSExport
  def viewLinkedPerspective(entry: LexicalEntry, field: Field, values: js.Array[Value]) = {

    val options = ModalOptions()
    options.templateUrl = "/static/templates/modal/viewLinkedDictionary.html"
    options.controller = "ViewDictionaryModalController"
    options.backdrop = false
    options.keyboard = false
    options.size = "lg"
    options.resolve = js.Dynamic.literal(
      params = () => {
        js.Dynamic.literal(
          dictionaryClientId = dictionaryClientId.asInstanceOf[js.Object],
          dictionaryObjectId = dictionaryObjectId.asInstanceOf[js.Object],
          perspectiveClientId = perspectiveClientId,
          perspectiveObjectId = perspectiveObjectId,
          linkPerspectiveClientId = field.link.get.clientId,
          linkPerspectiveObjectId = field.link.get.objectId,
          lexicalEntry = entry.asInstanceOf[js.Object],
          field = field.asInstanceOf[js.Object],
          links = values.map { _.asInstanceOf[GroupValue].link }
        )
      }
    ).asInstanceOf[js.Dictionary[js.Any]]

    val instance = modal.open[Seq[Entity]](options)
    instance.result map { entities =>
      entities.foreach(e => scope.dictionaryTable.addEntity(entry, e))
    }
  }

  @JSExport
  def linkedPerspectiveName(): String = {
    perspectiveTranslation match {
      case Some(gist) =>
        val localeId = Utils.getLocale().getOrElse(2)
        gist.atoms.find(_.localeId == localeId) match {
          case Some(atom) => atom.content
          case None => ""
        }
      case None => ""
    }
  }


  @JSExport
  def close() = {
    instance.close(createdEntities)
  }


  private[this] def load() = {

    backend.getPerspective(perspectiveId) map {
      p =>
        backend.translationGist(p.translationGistClientId, p.translationGistObjectId) map {
          gist =>
            perspectiveTranslation = Some(gist)
        }
    }

    backend.dataTypes() onComplete {
      case Success(allDataTypes) =>
        dataTypes = allDataTypes
        // get fields of main perspective
        backend.getFields(dictionaryId, perspectiveId) onComplete {
          case Success(fields) =>
            perspectiveFields = fields
            // get fields of this perspective
            backend.getFields(dictionaryId, linkPerspectiveId) onComplete {
              case Success(linkedFields) =>
                linkedPerspectiveFields = linkedFields
                val reqs = links.toSeq.map { link => backend.getLexicalEntry(dictionaryId, linkPerspectiveId, CompositeId(link.clientId, link.objectId)) }
                Future.sequence(reqs) onComplete {
                  case Success(lexicalEntries) =>
                    scope.dictionaryTable = DictionaryTable.build(linkedFields, dataTypes, lexicalEntries)
                  case Failure(e) => console.log(e.getMessage)
                }
              case Failure(e) => console.log(e.getMessage)
            }
          case Failure(e) => console.log(e.getMessage)
        }

      case Failure(e) => console.log(e.getMessage)
    }
  }
}