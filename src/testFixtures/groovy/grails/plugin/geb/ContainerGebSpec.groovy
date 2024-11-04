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

import geb.spock.GebSpec
import groovy.transform.PackageScope
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.testcontainers.Testcontainers
import org.testcontainers.containers.BrowserWebDriverContainer
import org.testcontainers.containers.Container
import org.testcontainers.containers.ExecConfig
import org.testcontainers.containers.PortForwardingContainer
import org.testcontainers.images.builder.Transferable
import org.testcontainers.utility.MountableFile
import spock.lang.Shared

import java.nio.charset.Charset
import java.time.Duration

/**
 * A {@link geb.spock.GebSpec GebSpec} that leverages Testcontainers to run the browser inside a container.
 *
 * <p>Prerequisites:
 * <ul>
 *   <li>
 *       The test class must be annotated with {@link grails.testing.mixin.integration.Integration @Integration}.
 *   </li>
 *   <li>
 *       A <a href="https://java.testcontainers.org/supported_docker_environment/">compatible container runtime</a>
 *       (e.g., Docker) must be available for Testcontainers to utilize.
 *   </li>
 * </ul>
 *
 * @author Søren Berg Glasius
 * @author Mattias Reichel
 * @since 5.0.0
 */
class ContainerGebSpec extends GebSpec {

    private static final String DEFAULT_PROTOCOL = 'http'
    private static final String DEFAULT_HOSTNAME = 'host.testcontainers.internal'

    @Shared
    BrowserWebDriverContainer webDriverContainer

    @PackageScope
    void initialize() {
        if (!webDriverContainer) {
            if (!hasProperty('serverPort')) {
                throw new IllegalStateException('Test class must be annotated with @Integration for serverPort to be injected')
            }
            webDriverContainer = new BrowserWebDriverContainer()
            Testcontainers.exposeHostPorts(serverPort)
            webDriverContainer.tap {
                addExposedPort(serverPort)
                withAccessToHost(true)
                start()
            }
            if (hostName != DEFAULT_HOSTNAME) {
                execInContainer('/bin/sh', '-c', "echo '$hostIp\t$hostName' | sudo tee -a /etc/hosts")
            }
            browser.driver = new RemoteWebDriver(webDriverContainer.seleniumAddress, new ChromeOptions())
            browser.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30))
        }
    }

    void setup() {
        initialize()
        baseUrl = "$protocol://$hostName:$serverPort"
    }

    def cleanupSpec() {
        webDriverContainer?.stop()
    }

    /**
     * Returns the protocol that the browser will use to access the server under test.
     * <p>Defaults to {@code http}.
     *
     * @return the protocol for accessing the server under test
     */
    String getProtocol() {
        return DEFAULT_PROTOCOL
    }

    /**
     * Returns the hostname that the browser will use to access the server under test.
     * <p>Defaults to {@code host.testcontainers.internal}.
     *
     * @return the hostname for accessing the server under test
     */
    String getHostName() {
        return DEFAULT_HOSTNAME
    }

    // Convenience methods to interact with the container

    Container.ExecResult execInContainer(String... command) {
        return webDriverContainer.execInContainer(command)
    }

    Container.ExecResult execInContainer(Charset outputCharset, String... command) {
        return webDriverContainer.execInContainer(outputCharset, command)
    }

    Container.ExecResult execInContainer(ExecConfig execConfig) {
        return webDriverContainer.execInContainer(execConfig)
    }

    Container.ExecResult execInContainer(Charset outputCharset, ExecConfig execConfig) {
        return webDriverContainer.execInContainer(outputCharset, execConfig)
    }

    void copyFileToContainer(MountableFile mountableFile, String containerPath) {
        webDriverContainer.copyFileToContainer(mountableFile, containerPath)
    }

    void copyFileToContainer(Transferable transferable, String containerPath) {
        webDriverContainer.copyFileToContainer(transferable, containerPath)
    }

    void copyFileFromContainer(String containerPath, String destinationPath) throws IOException, InterruptedException {
        webDriverContainer.copyFileFromContainer(containerPath, destinationPath)
    }

    String getLogs() {
        return webDriverContainer.getLogs()
    }

    private static String getHostIp() {
        PortForwardingContainer.INSTANCE.network.get().ipAddress
    }
}