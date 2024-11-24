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
import org.spockframework.runtime.model.IterationInfo
import org.testcontainers.lifecycle.TestDescription

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Implements {@link org.testcontainers.lifecycle.TestDescription} to customize recording names.
 *
 * @see org.testcontainers.lifecycle.TestDescription
 * @author James Daugherty
 * @since 5.0
 */
@CompileStatic
class ContainerGebTestDescription implements TestDescription {

    String testId
    String filesystemFriendlyName

    ContainerGebTestDescription(IterationInfo testInfo, LocalDateTime runDate) {
        testId = testInfo.displayName
        String safeName = testId.replaceAll('\\W+', '_')
        filesystemFriendlyName = "${DateTimeFormatter.ofPattern('yyyyMMdd_HHmmss').format(runDate)}_${safeName}"
    }
}
