package grails.plugin.geb

import groovy.transform.TailRecursive
import groovy.util.logging.Slf4j
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.SpecInfo

import java.time.LocalDateTime

@Slf4j
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
