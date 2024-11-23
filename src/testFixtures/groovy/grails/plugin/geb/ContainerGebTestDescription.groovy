package grails.plugin.geb

import org.spockframework.runtime.model.IterationInfo
import org.testcontainers.lifecycle.TestDescription

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ContainerGebTestDescription implements TestDescription {
    String testId
    String filesystemFriendlyName

    ContainerGebTestDescription(IterationInfo testInfo, LocalDateTime runDate) {
        testId = testInfo.displayName

        String safeName = testId.replaceAll("\\W+", "")
        filesystemFriendlyName = "${DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(runDate)}_${safeName}"
    }
}
