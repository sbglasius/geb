/*
 * Copyright 2024 original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.geb

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.util.logging.Slf4j
import org.testcontainers.containers.BrowserWebDriverContainer
import org.testcontainers.containers.VncRecordingContainer

import static org.testcontainers.containers.BrowserWebDriverContainer.*
import static org.testcontainers.containers.VncRecordingContainer.*

/**
 * Handles parsing various recording configuration used by {@link grails.plugin.geb.ContainerGebRecordingExtension}
 *
 * @author James Daugherty
 * @since 5.0
 */
@Slf4j
@CompileStatic
class RecordingSettings {

    private static VncRecordingMode DEFAULT_RECORDING_MODE = VncRecordingMode.SKIP
    private static VncRecordingFormat DEFAULT_RECORDING_FORMAT = VncRecordingFormat.MP4

    String recordingDirectoryName
	VncRecordingMode recordingMode
	VncRecordingFormat recordingFormat

    RecordingSettings() {
        recordingDirectoryName = System.getProperty('grails.geb.recording.directory', 'build/recordings')
        recordingMode = VncRecordingMode.valueOf(System.getProperty('grails.geb.recording.mode', DEFAULT_RECORDING_MODE.name()))
        recordingFormat = VncRecordingFormat.valueOf(System.getProperty('grails.geb.recording.format', DEFAULT_RECORDING_FORMAT.name()))
    }

    boolean isRecordingEnabled() {
        recordingMode != VncRecordingMode.SKIP
    }

    @Memoized
    File getRecordingDirectory() {
        if (!recordingEnabled) {
            return null
        }

        File recordingDirectory = new File(recordingDirectoryName)
        if (!recordingDirectory.exists()) {
            log.info('Could not find `{}` Directory for recording. Creating...', recordingDirectoryName)
            recordingDirectory.mkdirs()
        } else if (!recordingDirectory.isDirectory()) {
            throw new IllegalStateException("Configured recording directory '${recordingDirectory}' is expected to be a directory, but found file instead.")
        }

        return recordingDirectory
    }
}
