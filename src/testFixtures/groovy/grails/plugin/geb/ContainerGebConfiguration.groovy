package grails.plugin.geb

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.util.logging.Slf4j
import org.testcontainers.containers.BrowserWebDriverContainer
import org.testcontainers.containers.VncRecordingContainer

@Slf4j
@CompileStatic
class ContainerGebConfiguration {
    String recordingDirectoryName

    boolean recording

    BrowserWebDriverContainer.VncRecordingMode recordingMode

    VncRecordingContainer.VncRecordingFormat recordingFormat

    ContainerGebConfiguration() {
        recording = Boolean.parseBoolean(System.getProperty('grails.geb.recording.enabled', true as String))
        recordingDirectoryName = System.getProperty('grails.geb.recording.directory', 'build/recordings')
        recordingMode = BrowserWebDriverContainer.VncRecordingMode.valueOf(System.getProperty('grails.geb.recording.mode', BrowserWebDriverContainer.VncRecordingMode.RECORD_FAILING.name()))
        recordingFormat = VncRecordingContainer.VncRecordingFormat.valueOf(System.getProperty('grails.geb.recording.format', VncRecordingContainer.VncRecordingFormat.MP4.name()))
    }

    @Memoized
    File getRecordingDirectory() {
        if(!recording) {
            return null
        }

        File recordingDirectory = new File(recordingDirectoryName)
        if(!recordingDirectory.exists()) {
            log.info("Could not find `${recordingDirectoryName}` directory for recording.  Creating...")
            recordingDirectory.mkdir()
        }
        else if(!recordingDirectory.isDirectory()) {
            throw new IllegalStateException("Recording Directory name expected to be `${recordingDirectoryName}, but found file instead.")
        }

        recordingDirectory
    }
}
