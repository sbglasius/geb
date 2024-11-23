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
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import org.testcontainers.containers.BrowserWebDriverContainer
import org.testcontainers.containers.VncRecordingContainer

/**
 * A container class to parse the configuration used by {@link grails.plugin.geb.ContainerGebRecordingExtension}
 *
 * @author James Daugherty
 * @since 5.0.0
 */
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
