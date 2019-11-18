import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import java.io.File


class CodeTrackerPlugin(private val widget: PluginStatusBarWidget) {
    var trackingState : TrackingState = TrackingState.OFF
        set(trackingState : TrackingState) {
            field = trackingState
            widget.updateState()
        }


    // todo: rename tracking documents
    private val trackingDocuments: MutableMap<Document, File> = HashMap()
    private val trackingFiles: MutableMap<VirtualFile, File> = HashMap()

    private val pathToTrackingFolder = "/home/elena/Documents/spbu/jb/dip/data2/"

    fun addMultiDocumentListener(project: Project) {
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(object : DocumentListener {
            override fun beforeDocumentChange(event: DocumentEvent) {
                if (trackingState == TrackingState.ON && isValidChange(event)) {
                    trackingDocuments.getOrPut(event.document, { createTrackingFile(event) } )
                }
            }

            override fun documentChanged(event: DocumentEvent) {
                if (trackingState == TrackingState.ON && isValidChange(event)) {
                    addChangeToFile(event)
                }
            }
        })
    }

    private fun addChangeToFile(event: DocumentEvent) {
        val trackingFile = trackingDocuments[event.document] ?: return
        val fragmentDocument = event.document
        val file = FileDocumentManager.getInstance().getFile(fragmentDocument)

        val offset = event.offset
        val newLength = event.newLength

        // actual logic depends on which line we want to call 'changed' when '\n' is inserted
        val firstLine = fragmentDocument.getLineNumber(offset)
        val lastLine =
            if (newLength == 0) {
                firstLine
            } else {
                fragmentDocument.getLineNumber(offset + newLength - 1)
            }

        trackingFile.appendText("file: ${file?.name}; ${file?.hashCode()}, offset: $offset, newLength: $newLength, firstLine: $firstLine, lastLine: $lastLine, fragment:\n${fragmentDocument.text}\n\n")
    }

    private fun isValidChange(event: DocumentEvent) : Boolean {
        return EditorFactory.getInstance().getEditors(event.document).isNotEmpty() && FileDocumentManager.getInstance().getFile(event.document) != null
    }

    private fun createTrackingFile(event: DocumentEvent): File {
        val file = FileDocumentManager.getInstance().getFile(event.document)
        val trackingFile = File("$pathToTrackingFolder${file?.nameWithoutExtension}_${file.hashCode()}_${event.document.hashCode()}.txt")
        FileUtil.createIfDoesntExist(trackingFile)
        trackingFile.writeText("${event.document.text}\n")
        return trackingFile
    }


    fun switchedState() : TrackingState {
        return when(trackingState) {
            TrackingState.OFF -> TrackingState.ON
            TrackingState.ON -> TrackingState.OFF
        }
    }
}

enum class TrackingState {
    OFF,
    ON
}