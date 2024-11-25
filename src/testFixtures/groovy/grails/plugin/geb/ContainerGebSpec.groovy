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

import geb.test.GebTestManager
import geb.test.ManagedGebTest
import geb.transform.DynamicallyDispatchesToBrowser
import org.testcontainers.containers.BrowserWebDriverContainer
import spock.lang.Shared
import spock.lang.Specification

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
 * @see grails.plugin.geb.ContainerGebConfiguration for how to customize the container's connection information
 *
 * @author Søren Berg Glasius
 * @author Mattias Reichel
 * @author James Daugherty
 * @since 5.0
 */
@DynamicallyDispatchesToBrowser
abstract class ContainerGebSpec extends Specification implements ManagedGebTest, ContainerAwareDownloadSupport {

    private static final String DEFAULT_HOSTNAME_FROM_HOST = 'localhost'
    boolean reportingEnabled = false

    @Override
    @Delegate(includes = ['getBrowser', 'report'])
    GebTestManager getTestManager() {
        return isReportingEnabled() ?
                GebTestManagerProvider.getReportingInstance() :
                GebTestManagerProvider.getInstance()
    }

    @Shared
    BrowserWebDriverContainer webDriverContainer

    /**
     * Get access to container running the web-driver, for convenience to execInContainer, copyFileToContainer etc.
     *
     * @see org.testcontainers.containers.ContainerState#execInContainer(java.lang.String ...)
     * @see org.testcontainers.containers.ContainerState#copyFileToContainer(org.testcontainers.utility.MountableFile, java.lang.String)
     * @see org.testcontainers.containers.ContainerState#copyFileFromContainer(java.lang.String, java.lang.String)
     * @see org.testcontainers.containers.ContainerState
     */
    BrowserWebDriverContainer getContainer() {
        return webDriverContainer
    }

    /**
     * Returns the hostname that the server under test is available on from the host.
     * <p>This is useful when using any of the {@code download*()} methods as they will connect from the host,
     * and not from within the container.
     * <p>Defaults to {@code localhost}. If the value returned by {@code webDriverContainer.getHost()}
     * is different from the default, this method will return the same value same as {@code webDriverContainer.getHost()}.
     *
     * @return the hostname for accessing the server under test from the host
     */
    @Override
    String getHostNameFromHost() {
        return hostNameChanged ? webDriverContainer.host : DEFAULT_HOSTNAME_FROM_HOST
    }

    private boolean isHostNameChanged() {
        return webDriverContainer.host != ContainerGebConfiguration.DEFAULT_HOSTNAME_FROM_CONTAINER
    }
}