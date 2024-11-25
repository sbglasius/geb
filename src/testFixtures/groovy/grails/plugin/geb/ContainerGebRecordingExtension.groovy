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

import grails.testing.mixin.integration.Integration
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
 * @since 5.0
 */
@Slf4j
@CompileStatic
class ContainerGebRecordingExtension implements IGlobalExtension {

    WebDriverContainerHolder holder

    @Override
    void start() {
        holder = new WebDriverContainerHolder(new RecordingSettings())
        addShutdownHook {
            holder.stop()
        }
    }

    @Override
    void stop() {
        holder.stop()
    }

    @Override
    void visitSpec(SpecInfo spec) {
        if (isContainerGebSpec(spec) && validateContainerGebSpec(spec)) {
            ContainerGebTestListener listener = new ContainerGebTestListener(holder, spec, LocalDateTime.now())
            spec.addSetupInterceptor {
                holder.reinitialize(it)
                (it.sharedInstance as ContainerGebSpec).webDriverContainer = holder.currentContainer
            }

            spec.addListener(listener)
        }
    }

    @TailRecursive
    private boolean isContainerGebSpec(SpecInfo spec) {
        if (spec != null) {
            if (spec.filename.startsWith('ContainerGebSpec.')) {
                return true
            }
            return isContainerGebSpec(spec.superSpec)
        }
        return false
    }

    private static boolean validateContainerGebSpec(SpecInfo specInfo) {
        if (!specInfo.annotations.find { it.annotationType() == Integration }) {
            throw new IllegalArgumentException('ContainerGebSpec classes must be annotated with @Integration')
        }

        return true
    }
}

