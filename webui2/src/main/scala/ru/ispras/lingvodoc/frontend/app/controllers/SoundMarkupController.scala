package ru.ispras.lingvodoc.frontend.app.controllers

import org.scalajs.dom
import scala.collection.mutable
import scala.scalajs.js
import ru.ispras.lingvodoc.frontend.app.services.{ModalInstance, ModalService}
import com.greencatsoft.angularjs.core.{Timeout, Scope}
import com.greencatsoft.angularjs.{Angular, AbstractController, injectable}
import ru.ispras.lingvodoc.frontend.app.model.{Perspective, Language, Dictionary}
import ru.ispras.lingvodoc.frontend.app.services.BackendService
import ru.ispras.lingvodoc.frontend.extras.facades.{WaveSurfer, WaveSurferOpts}
import ru.ispras.lingvodoc.frontend.extras.elan.{Tier, ELANPArserException, ELANDocumentJquery}
import org.scalajs.dom.{EventTarget, console}
import org.singlespaced.d3js.{Selection, d3}
import scala.scalajs.js.JSConverters._
import ru.ispras.lingvodoc.frontend.app.utils.LingvodocExecutionContext.Implicits.executionContext

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

@js.native
trait SoundMarkupScope extends Scope {
  var ruler: Double = js.native // coordinate of wavesurfer ruler
  var elan: ELANDocumentJquery = js.native
  var ws: WaveSurfer = js.native // for debugging, remove later
}

@injectable("SoundMarkupController")
class SoundMarkupController(scope: SoundMarkupScope,
                            timeout: Timeout,
                            instance: ModalInstance[Unit],
                            modal: ModalService,
                            backend: BackendService,
                            params: js.Dictionary[js.Function0[js.Any]])
  extends AbstractController[SoundMarkupScope](scope) {
  var waveSurfer: Option[WaveSurfer] = None
  var soundMarkup: Option[String] = None
//  val soundAddress = params.get("soundAddress").map(_.toString)
  val soundAddress = Some("http://localhost/getting_closer.wav")
  val dictionaryClientId = params.get("dictionaryClientId").map(_.toString.toInt)
  val dictionaryObjectId = params.get("dictionaryObjectId").map(_.toString.toInt)

  // hack to distinguish situation when ws is sought by human or by us
  var isWSSeeked = false
  // true after first drag event, false after dragend
  var isDragging = false
  // true when we move right border of selection rectangle, false when left
  var rightBorderIsMoving = true

  // d3 selection rectangle element
  var selectionRectangle: Option[Selection[EventTarget]] = None
  // d3 drag object
  var dragRectange: js.Dynamic = _


  // add scope to window for debugging
  dom.window.asInstanceOf[js.Dynamic].myScope = scope

//  (dictionaryClientId, dictionaryObjectId).zipped.foreach((dictionaryClientId, dictionaryObjectId) => {
//    backend.getSoundMarkup(dictionaryClientId, dictionaryObjectId) onSuccess {
//      case markup => parseMarkup(markup)
//    }
//  })
  parseMarkup("fff")

  // hack to initialize controller after loading the view
  // see http://stackoverflow.com/questions/21715256/angularjs-event-to-call-after-content-is-loaded
  @JSExport
  def createWaveSurfer(): Unit = {
    if (waveSurfer.isEmpty) {
      val wso = WaveSurferOpts("#waveform", waveColor = "violet", progressColor = "purple",
                               cursorWidth = 1, cursorColor = "#000")
      waveSurfer = Some(WaveSurfer.create(wso))
      (waveSurfer, soundAddress).zipped.foreach((ws, sa) => {
        ws.load(sa)
      })
      waveSurfer.foreach(_.on("seek", onWSSeek _)) // bind seek event
      waveSurfer.foreach(_.on("audioprocess", onWSPlaying _)) // bind playing event
      scope.ws = waveSurfer.get
      init()
    } // do not write anything here, outside if!
  }

  // In contract to the constructor, this method is called when waversurfer is already loaded
  def init(): Unit = {
    dragRectange = d3.behavior.drag().asInstanceOf[js.Dynamic]
      .on("dragstart", onSelectionDragStart _)
      .on("drag", onSelectionDragging _)
      .on("dragend", onSelectionDragEnd _)
    d3.select("#backgroundRect").asInstanceOf[js.Dynamic].call(dragRectange)
  }

  def parseMarkup(markup: String): Unit = {
    val test_markup =
      """<?xml version="1.0" encoding="UTF-8"?>
<ANNOTATION_DOCUMENT AUTHOR="TextGridTools" DATE="2016-07-28T15:41:21+00:00" FORMAT="2.7" VERSION="2.7" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://www.mpi.nl/tools/elan/EAFv2.7.xsd">
<HEADER MEDIA_FILE="" TIME_UNITS="milliseconds">
<PROPERTY NAME="lingvodocLastUsedAnnotationId">0</PROPERTY>
<PROPERTY NAME="lingvodocLastUsedAnnotationId">0</PROPERTY>
</HEADER>
<TIME_ORDER>
<TIME_SLOT TIME_SLOT_ID="ts1" TIME_VALUE="0"/>
<TIME_SLOT TIME_SLOT_ID="ts2" TIME_VALUE="64"/>
<TIME_SLOT TIME_SLOT_ID="ts3" TIME_VALUE="135"/>
<TIME_SLOT TIME_SLOT_ID="ts4" TIME_VALUE="303"/>
<TIME_SLOT TIME_SLOT_ID="ts5" TIME_VALUE="389"/>
<TIME_SLOT TIME_SLOT_ID="ts6" TIME_VALUE="472"/>
<TIME_SLOT TIME_SLOT_ID="ts7" TIME_VALUE="574"/>
</TIME_ORDER>
<TIER DEFAULT_LOCALE="en" LINGUISTIC_TYPE_REF="default-lt" TIER_ID="Mary">
<ANNOTATION>
<ALIGNABLE_ANNOTATION ANNOTATION_ID="a1" TIME_SLOT_REF1="ts1" TIME_SLOT_REF2="ts2">
<ANNOTATION_VALUE>(22в)</ANNOTATION_VALUE>
</ALIGNABLE_ANNOTATION>
</ANNOTATION>
<ANNOTATION>
<ALIGNABLE_ANNOTATION ANNOTATION_ID="a2" TIME_SLOT_REF1="ts2" TIME_SLOT_REF2="ts3">
<ANNOTATION_VALUE>w</ANNOTATION_VALUE>
</ALIGNABLE_ANNOTATION>
</ANNOTATION>
<ANNOTATION>
<ALIGNABLE_ANNOTATION ANNOTATION_ID="a3" TIME_SLOT_REF1="ts3" TIME_SLOT_REF2="ts4">
<ANNOTATION_VALUE>a</ANNOTATION_VALUE>
</ALIGNABLE_ANNOTATION>
</ANNOTATION>
<ANNOTATION>
<ALIGNABLE_ANNOTATION ANNOTATION_ID="a4" TIME_SLOT_REF1="ts4" TIME_SLOT_REF2="ts5">
<ANNOTATION_VALUE>n</ANNOTATION_VALUE>
</ALIGNABLE_ANNOTATION>
</ANNOTATION>
<ANNOTATION>
<ALIGNABLE_ANNOTATION ANNOTATION_ID="a5" TIME_SLOT_REF1="ts5" TIME_SLOT_REF2="ts6">
<ANNOTATION_VALUE>o</ANNOTATION_VALUE>
</ALIGNABLE_ANNOTATION>
</ANNOTATION>
<ANNOTATION>
<ALIGNABLE_ANNOTATION ANNOTATION_ID="a6" TIME_SLOT_REF1="ts6" TIME_SLOT_REF2="ts7">
<ANNOTATION_VALUE>(ЛЗС)</ANNOTATION_VALUE>
</ALIGNABLE_ANNOTATION>
</ANNOTATION>
</TIER>
<TIER DEFAULT_LOCALE="en" LINGUISTIC_TYPE_REF="Translation" TIER_ID="Ann">
<ANNOTATION>
<REF_ANNOTATION ANNOTATION_ID="ref1" ANNOTATION_REF="ref1">
<ANNOTATION_VALUE>haha</ANNOTATION_VALUE>
</REF_ANNOTATION>
</ANNOTATION>
</TIER>

<LINGUISTIC_TYPE GRAPHIC_REFERENCES="false" LINGUISTIC_TYPE_ID="default-lt" TIME_ALIGNABLE="true"/>
<LINGUISTIC_TYPE LINGUISTIC_TYPE_ID="Sp Transcript" TIME_ALIGNABLE="true" GRAPHIC_REFERENCES="false"/>
<LINGUISTIC_TYPE LINGUISTIC_TYPE_ID="Translation" TIME_ALIGNABLE="false" GRAPHIC_REFERENCES="false" CONSTRAINTS="Symbolic_Association"/>

<LOCALE COUNTRY_CODE="US" LANGUAGE_CODE="en"/>

<CONSTRAINT STEREOTYPE="Time_Subdivision" DESCRIPTION="Time subdivision of parent annotation's time interval, no time gaps allowed within this interval"/>
<CONSTRAINT STEREOTYPE="Symbolic_Subdivision" DESCRIPTION="Symbolic subdivision of a parent annotation. Annotations refering to the same parent are ordered"/>
<CONSTRAINT STEREOTYPE="Symbolic_Association" DESCRIPTION="1-1 association with a parent annotation"/>
<CONSTRAINT STEREOTYPE="Included_In" DESCRIPTION="Time alignable annotations within the parent annotation's time interval, gaps are allowed"/>

<CONTROLLED_VOCABULARY CV_ID="Gesture Hand" DESCRIPTION="Hand gesture codes">
<CV_ENTRY DESCRIPTION="Right Hand">R</CV_ENTRY>
<CV_ENTRY DESCRIPTION="Left Hand">L</CV_ENTRY>
<CV_ENTRY DESCRIPTION="Both Hands">B</CV_ENTRY>
</CONTROLLED_VOCABULARY>

<LEXICON_REF DATCAT_ID="MmM5MDkwYTIyZjExMmMyYzAxMmY0NDAyOWJjZDA5ZjU=" DATCAT_NAME="Begripnaam"
LEXICON_ID="MmM5MDkwYTIyZjExMmMyYzAxMmY0NDAyOWMzYTBhMjI="
LEXICON_NAME="SignLinC" LEX_REF_ID="lr1" NAME="SL_Lex1" TYPE="LEXUS (MPI)"
URL="http://corpus1.mpi.nl/mpi/lexusDojo/services/LexusWebService"/>

<EXTERNAL_REF EXT_REF_ID="er1" TYPE="cve_id" VALUE="CVE_ID40"/>
<EXTERNAL_REF EXT_REF_ID="er2" TYPE="cve_id" VALUE="CVE_ID41"/>
<EXTERNAL_REF EXT_REF_ID="er3" TYPE="ecv"
VALUE="http://www.mpi.nl/tools/elan/atemp/gest.ecv"/>
<EXTERNAL_REF EXT_REF_ID="er4" TYPE="iso12620" VALUE="http://www.isocat.org/datcat/DC-1333"/>
</ANNOTATION_DOCUMENT>
      """
      scope.elan = ELANDocumentJquery(test_markup)
      console.log(scope.elan.toString)
      scope.ruler = 0
  }

  @JSExport
  def getWaveSurferWidth = js.Dynamic.global.document.getElementById("waveform").scrollWidth.toString.toDouble

  @JSExport
  def getWaveSurferHeight = js.Dynamic.global.document.getElementById("waveform").scrollHeight.toString.toDouble

  def svgSeek(offset: Double, forceApply: Boolean = false): Unit = {
    isWSSeeked = true
    setRulerOffset(offset, forceApply)
    val progress = offset / getWaveSurferWidth
    waveSurfer.foreach(_.seekTo(progress))
  }

  def setRulerProgress(progress: Double, forceApply: Boolean = false, applyTimeout: Boolean = false): Unit =
    setRulerOffset(progress * getWaveSurferWidth, forceApply, applyTimeout)

  def setRulerOffset(offset: Double, forceApply: Boolean = false, applyTimeout: Boolean = false): Unit = {
    val action = () => { scope.ruler = offset }
    if (applyTimeout)
      timeout(action)
    else if (forceApply)
      scope.$apply({
        action()
      })
    else
      action()
  }

  // not needed
  @JSExport
  def getDrawerWidth: Double = {
    waveSurfer.map(_.drawer.width.toString.toDouble).getOrElse(0)
  }
  @JSExport
  def getDrawerHeight: Double = {
    waveSurfer.map(_.drawer.height.toString.toDouble).getOrElse(0)
  }

  @JSExport
  def playPause() = waveSurfer.foreach(_.playPause())

  @JSExport
  def play(start: Int, end: Int) = waveSurfer.foreach(_.play(start, end))

  @JSExport
  def save(): Unit = {
    instance.close(())
  }

  @JSExport
  def cancel(): Unit = {
    instance.close(())
  }

  def onWSSeek(progress: Double): Unit = {
    console.log("ws seeked")
    if (isWSSeeked)
      isWSSeeked = false
    else
      setRulerProgress(progress, forceApply = true)
  }

  def onWSPlaying(): Unit = {
    val progress = waveSurfer.map(ws => ws.getCurrentTime() / ws.getDuration())
    progress.foreach(p => setRulerProgress(p, applyTimeout = true))
  }

  @JSExport
  def onSVGSeek(event: js.Dynamic): Unit = {
    console.log("svg seeking")
    svgSeek(event.offsetX.asInstanceOf[Double])
  }

  def onSelectionDragStart() = {
    console.log("starting dragging")
  }

  def onSelectionDragging() = {
    console.log("dragging")
    if (!isDragging) { // executed on first drag event
      selectionRectangle.foreach(_.remove())
      selectionRectangle = Some(d3.select("#soundSVG").append("rect")
        .attr("x", d3.event.asInstanceOf[js.Dynamic].x.toString.toDouble)
        .attr("y", 0)
        .attr("width", 0)
        .attr("height", getWaveSurferHeight))
      isDragging = true
    }
    else { // executed on every subsequent drag event
      val (oldx, cursorx) = (selectionRectangle.get.attr("x").toString.toDouble, Math.max(0, d3.event.asInstanceOf[js.Dynamic].x.toString.toDouble))
      val oldWidth = selectionRectangle.get.attr("width").toString.toDouble

      if ((rightBorderIsMoving && cursorx > oldx) || (!rightBorderIsMoving && cursorx >= oldx + oldWidth)) {
        selectionRectangle.foreach(_.attr("width", cursorx - oldx))
        rightBorderIsMoving = true
      }
      else {
        selectionRectangle.foreach(_.attr("x", cursorx).attr("width", oldx + oldWidth - cursorx))
        rightBorderIsMoving = false
      }

      svgSeek(cursorx, forceApply = true)
    }
  }
  def onSelectionDragEnd() = {
    console.log("ending dragging")
    isDragging = false
  }
}

