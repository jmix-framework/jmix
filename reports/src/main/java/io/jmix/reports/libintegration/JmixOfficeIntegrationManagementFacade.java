/*
 * Copyright 2021 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.reports.libintegration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@ManagedResource(description = "Manages OpenOffice integration", objectName = "jmix.reports:type=JmixOfficeIntegration")
@Component("report_JmixOfficeIntegrationManagementFacade")
public class JmixOfficeIntegrationManagementFacade {
    @Autowired
    private JmixOfficeIntegration officeIntegration;

    @ManagedAttribute(description = "Timeout (in seconds) to generate doc/docx report document")
    public void setTimeoutInSeconds(Integer timeoutInSeconds) {
        officeIntegration.setTimeoutInSeconds(timeoutInSeconds);
    }

    @ManagedAttribute(description = "Timeout (in seconds) to generate doc/docx report document")
    public Integer getTimeoutInSeconds() {
        return officeIntegration.getTimeoutInSeconds();
    }

    @ManagedAttribute(description = "Has to be false if using OpenOffice reporting formatter on a *nix server without X server running")
    public void setDisplayDeviceAvailable(Boolean displayDeviceAvailable){
        officeIntegration.setDisplayDeviceAvailable(displayDeviceAvailable);
    }
    @ManagedAttribute(description = "Has to be false if using OpenOffice reporting formatter on a *nix server without X server running")
    public Boolean isDisplayDeviceAvailable() {
        return officeIntegration.isDisplayDeviceAvailable();
    }

    @ManagedAttribute(description = "Directory for temporary files")
    public void setTemporaryDirPath(String temporaryDirPath) {
        officeIntegration.setTemporaryDirPath(temporaryDirPath);
    }
    @ManagedAttribute(description = "Directory for temporary files")
    public String getTemporaryDirPath() {
        return officeIntegration.getTemporaryDirPath();
    }

    @ManagedAttribute(description = "The list of ports to start OpenOffice on")
    public String getAvailablePorts() {
        return officeIntegration.getAvailablePorts();
    }

    @ManagedOperation(description = "Closes all connections to OpenOffice")
    public void hardReloadAccessPorts() {
        officeIntegration.hardReloadAccessPorts();
    }
}