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

package jmix_modules

import io.jmix.core.JmixModuleDescriptor
import io.jmix.core.impl.JmixModulesSorter
import spock.lang.Specification

class JmixModulesSorterTest extends Specification {

    def "test"() {
        def core = new JmixModuleDescriptor("io.jmix.core")
        def data = new JmixModuleDescriptor("io.jmix.data")
        def security = new JmixModuleDescriptor("io.jmix.security")
        def ui = new JmixModuleDescriptor("io.jmix.ui")
        def uidata = new JmixModuleDescriptor("io.jmix.uidata")
        def addon = new JmixModuleDescriptor("com.company.addon")
        def app = new JmixModuleDescriptor("com.company.app")

        data.addDependency(core)

        security.addDependency(core)

        ui.addDependency(core)

        uidata.addDependency(ui)
        uidata.addDependency(data)

        addon.addDependency(data)
        addon.addDependency(ui)

        app.addDependency(core)
        app.addDependency(data)
        app.addDependency(security)
        app.addDependency(ui)
        app.addDependency(uidata)
        app.addDependency(addon)

        when:
        def sorted = JmixModulesSorter.sort([app, addon, ui, uidata, security, data, core])

        then:
        sorted.size() == 7
        sorted.indexOf(core) == 0
        sorted.indexOf(data) > sorted.indexOf(core)
        sorted.indexOf(ui) > sorted.indexOf(core)
        sorted.indexOf(addon) > sorted.indexOf(core)
        sorted.indexOf(addon) > sorted.indexOf(data)
        sorted.indexOf(addon) > sorted.indexOf(security)
        sorted.indexOf(addon) > sorted.indexOf(ui)
        sorted.indexOf(app) > sorted.indexOf(addon)
    }
}
