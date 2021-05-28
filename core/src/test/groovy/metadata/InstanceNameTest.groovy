/*
 * Copyright 2020 Haulmont.
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

package metadata

import io.jmix.core.InstanceNameProvider
import io.jmix.core.CoreConfiguration
import io.jmix.core.Metadata
import io.jmix.core.security.ClientDetails
import io.jmix.core.security.SystemAuthenticationToken
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ContextConfiguration
import spock.lang.Ignore
import spock.lang.Specification

import test_support.addon1.TestAddon1Configuration
import test_support.app.TestAppConfiguration
import test_support.app.entity.Address
import test_support.app.entity.Owner
import io.jmix.core.security.SystemAuthenticator

import org.springframework.beans.factory.annotation.Autowired
import test_support.app.entity.instance_name.GPSDeviceWithFieldName
import test_support.app.entity.instance_name.GPSDeviceWithMethodName
import test_support.app.entity.instance_name.GPSDeviceWithOverridedMethodName
import test_support.base.TestBaseConfiguration

import java.util.stream.Collectors

@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration, TestAddon1Configuration, TestAppConfiguration])
class InstanceNameTest extends Specification {

    @Autowired
    Metadata metadata

    @Autowired
    InstanceNameProvider instanceNameProvider

    @Autowired
    AuthenticationManager authenticationManager

    @Autowired
    SystemAuthenticator authenticator

    def "instance name method with injected Locale"() {

        def address = metadata.create(Address)
        address.city = "Samara"
        address.zip = "443011"

        expect:

        instanceNameProvider.getInstanceName(address) == "City: Samara, zip: 443011"
    }

    //todo MG
    @Ignore
    def "instance name method with ru Locale"() {

        def address = metadata.create(Address)
        address.city = "Samara"
        address.zip = "443011"

        def token = new SystemAuthenticationToken(null)
        SystemAuthenticationToken authentication = authenticationManager.authenticate(token) as SystemAuthenticationToken
        authentication.details = ClientDetails.builder().locale(new Locale("ru")).build()
        SecurityContextHolder.getContext().setAuthentication(authentication)

        expect:

        instanceNameProvider.getInstanceName(address) == "Город: Samara, индекс: 443011"
        instanceNameProvider.getInstanceNameRelatedProperties(metadata.getClass(Address),true).stream()
                .map{p->p.getName()}
                .collect(Collectors.toSet()) == ["city", "zip"] as Set
    }

    def "instance name property"() {

        def owner = metadata.create(Owner)
        owner.name = "John"
        authenticator.begin()

        expect:

        instanceNameProvider.getInstanceName(owner) == "John"
        instanceNameProvider.getInstanceNameRelatedProperties(metadata.getClass(Owner),true).stream()
                .map{p->p.getName()}
                .collect(Collectors.toSet()) == ["name"] as Set

        cleanup:
        authenticator.end()
    }

    def "instance name method with inheritance"() {
        when: "instance name method with class inheritance"
        def device = metadata.create(GPSDeviceWithMethodName)
        device.name = "gps"
        device.model = "super_model"

        then:
        instanceNameProvider.getInstanceName(device) == "device:gps"

        when: "instance name method with class inheritance and method is overridden"
        def anotherDevice = metadata.create(GPSDeviceWithOverridedMethodName)
        anotherDevice.name = "gps"
        anotherDevice.model = "super_model"

        then:
        instanceNameProvider.getInstanceName(anotherDevice) == "gps:gps"
    }

    def "instance name property with inheritance"() {
        def device = metadata.create(GPSDeviceWithFieldName)
        device.name = "gps"
        device.model = "super_model"

        authenticator.begin()

        expect:
        instanceNameProvider.getInstanceName(device) == "gps"

        cleanup:
        authenticator.end()
    }
}
