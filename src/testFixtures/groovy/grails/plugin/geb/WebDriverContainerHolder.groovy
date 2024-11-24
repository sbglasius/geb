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

import geb.Browser
import groovy.transform.EqualsAndHashCode
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.SpecInfo
import org.testcontainers.Testcontainers
import org.testcontainers.containers.BrowserWebDriverContainer

import java.time.Duration

/**
 * Responsible for initializing a {@link org.testcontainers.containers.BrowserWebDriverContainer BrowserWebDriverContainer}
 * per the Spec's {@link grails.plugin.geb.ContainerGebConfiguration ContainerGebConfiguration}.  This class will try to
 * reuse the same container if the configuration matches the current container.
 *
 * @author James Daugherty
 * @since 5.0
 */
class WebDriverContainerHolder {
    RecordingSettings recordingSettings
    WebDriverContainerConfiguration configuration
    BrowserWebDriverContainer current

    WebDriverContainerHolder(RecordingSettings recordingSettings) {
        this.recordingSettings = recordingSettings
    }

    boolean isInitialized() {
        current != null
    }

    boolean stop() {
        if (!current) {
            return false
        }
        current.stop()
        current = null
        configuration = null
        return true
    }

    boolean matchesCurrentContainerConfiguration(WebDriverContainerConfiguration specConfiguration) {
        specConfiguration == configuration
    }

    private static int getPort(IMethodInvocation invocation) {
        try {
            return (int) invocation.instance.metaClass.getProperty(invocation.instance, 'serverPort')
        } catch (Exception ignore) {
            throw new IllegalStateException('Test class must be annotated with @Integration for serverPort to be injected')
        }
    }

    boolean reinitialize(IMethodInvocation invocation) {
        WebDriverContainerConfiguration specConfiguration = new WebDriverContainerConfiguration(getPort(invocation), invocation.getSpec())
        if (matchesCurrentContainerConfiguration(specConfiguration)) {
            return false
        }

        if (isInitialized()) {
            stop()
        }

        configuration = specConfiguration
        current = new BrowserWebDriverContainer()
        Testcontainers.exposeHostPorts(configuration.port)
        current.tap {
            addExposedPort(configuration.port)
            withAccessToHost(true)
            start()
        }
        if (!isDefaultHostname()) {
            current.execInContainer('/bin/sh', '-c', "echo '$hostIp\t${configuration.hostName}' | sudo tee -a /etc/hosts")
        }

        if (recordingSettings.recordingMode != BrowserWebDriverContainer.VncRecordingMode.SKIP) {
            current = current.withRecordingMode(
                    recordingSettings.recordingMode,
                    recordingSettings.recordingDirectory,
                    recordingSettings.recordingFormat
            )
        }

        WebDriver driver = new RemoteWebDriver(current.seleniumAddress, new ChromeOptions())
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30))

        // Update the browser to use this container
        Browser browser = (invocation.sharedInstance as ContainerGebSpec).browser
        browser.driver = driver
        browser.baseUrl = "${configuration.protocol}://${configuration.hostName}:${configuration.port}"

        return true
    }

    private boolean isDefaultHostname() {
        return configuration.hostName == ContainerGebConfiguration.DEFAULT_HOSTNAME_FROM_CONTAINER
    }

    private String getHostIp() {
        current.getContainerInfo().getNetworkSettings().getNetworks().entrySet().first().value.ipAddress
    }

    @EqualsAndHashCode
    private static class WebDriverContainerConfiguration {
        String protocol
        String hostName
        int port

        WebDriverContainerConfiguration(int port, SpecInfo spec) {
            ContainerGebConfiguration configuration = spec.annotations.find { it.annotationType() == ContainerGebConfiguration } as ContainerGebConfiguration

            protocol = configuration?.protocol() ?: ContainerGebConfiguration.DEFAULT_PROTOCOL
            hostName = configuration?.hostName() ?: ContainerGebConfiguration.DEFAULT_HOSTNAME_FROM_CONTAINER
            this.port = port
        }
    }
}

