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

package restconfig;


import io.jmix.rest.RestConfiguration;
import io.jmix.rest.impl.config.RestServicesConfiguration;
import io.jmix.samples.rest.service.RestTestServiceWithoutInterfaces;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.AbstractRestControllerFT;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.*;

public class RestConfigurationFT extends AbstractRestControllerFT {
    @Autowired
    RestServicesConfiguration restServicesConfiguration;

    @Test
    public void serviceWithoutInterfacesMethods() {
        RestServicesConfiguration.RestServiceInfo serviceInfo =
                restServicesConfiguration.getServiceInfo(RestTestServiceWithoutInterfaces.NAME);
        assertNotNull(serviceInfo);
        Set<Method> methods = new HashSet<>(Arrays.asList(RestTestServiceWithoutInterfaces.class.getMethods()));
        List<RestServicesConfiguration.RestMethodInfo> methodInfos = serviceInfo.getMethods();
        for (RestServicesConfiguration.RestMethodInfo methodInfo : methodInfos) {
            assertTrue(methods.contains(methodInfo.getMethod()));
        }
    }
}
