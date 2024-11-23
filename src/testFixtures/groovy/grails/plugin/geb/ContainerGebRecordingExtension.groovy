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
import groovy.transform.TailRecursive
import groovy.util.logging.Slf4j
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.SpecInfo

import java.time.LocalDateTime

/**
 * A Spock Extension that manages the Testcontainers lifecycle for a {@link grails.plugin.geb.ContainerGebSpec}
 *
 * @author James Daugherty
 * @since 5.0.0
 */
@Slf4j
@CompileStatic
class ContainerGebRecordingExtension implements IGlobalExtension {
    ContainerGebConfiguration configuration

    @Override
    void start() {
        configuration = new ContainerGebConfiguration()
    }

    @Override
    void visitSpec(SpecInfo spec) {
        if (isContainerizedGebSpec(spec)) {
            ContainerGebTestListener listener = new ContainerGebTestListener(spec, LocalDateTime.now())
            // TODO: We should initialize the web driver container once for all geb tests so we don't have to spin it up & down.
            spec.addSetupInterceptor {
                ContainerGebSpec gebSpec = it.instance as ContainerGebSpec
                gebSpec.initialize()
                if(configuration.recording) {
                    listener.webDriverContainer = gebSpec.webDriverContainer.withRecordingMode(configuration.recordingMode, configuration.recordingDirectory, configuration.recordingFormat)
                }
            }

            spec.addListener(listener)
        }
    }

    @TailRecursive
    boolean isContainerizedGebSpec(SpecInfo spec) {
        if(spec != null) {
            if(spec.filename.startsWith('ContainerGebSpec.')) {
                return true
            }

            return isContainerizedGebSpec(spec.superSpec)
        }
        return false
    }
}
