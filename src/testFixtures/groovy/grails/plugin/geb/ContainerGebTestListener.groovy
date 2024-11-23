package grails.plugin.geb

import org.spockframework.runtime.AbstractRunListener
import org.spockframework.runtime.model.ErrorInfo
import org.spockframework.runtime.model.IterationInfo
import org.spockframework.runtime.model.SpecInfo
import org.testcontainers.containers.BrowserWebDriverContainer

import java.time.LocalDateTime

class ContainerGebTestListener extends AbstractRunListener {
    BrowserWebDriverContainer webDriverContainer
	ErrorInfo errorInfo
	SpecInfo spec
	LocalDateTime runDate

    ContainerGebTestListener(SpecInfo spec, LocalDateTime runDate) {
        this.spec = spec
        this.runDate = runDate
    }

    @Override
    void afterIteration(IterationInfo iteration) {
        webDriverContainer.afterTest(new ContainerGebTestDescription(iteration, runDate), Optional.of(errorInfo?.exception))
        errorInfo = null
    }

    @Override
    void error(ErrorInfo error) {
        errorInfo = error
    }
}