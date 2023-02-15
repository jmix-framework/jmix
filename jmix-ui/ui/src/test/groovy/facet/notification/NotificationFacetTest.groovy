/*
 * Copyright (c) 2020 Haulmont.
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

package facet.notification

import com.vaadin.server.Extension
import com.vaadin.ui.Notification
import facet.notification.screen.NotificationFacetTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.GuiDevelopmentException
import io.jmix.ui.Notifications
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.ContentMode
import io.jmix.ui.component.impl.ButtonImpl
import io.jmix.ui.component.impl.NotificationFacetImpl
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class NotificationFacetTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(['facet.notification'])
    }

    def 'Notification attributes are applied'() {
        showTestMainScreen()

        def screen = screens.create(NotificationFacetTestScreen)

        when: 'Notification is configured in XML'
        screen.show()

        def notification = screen.testNotification

        then: 'Attribute values are propagated to notification facet'

        notification.id == 'testNotification'
        notification.type == Notifications.NotificationType.HUMANIZED
        notification.caption == 'Notification Facet Test'
        notification.description == 'Description from XML'
        notification.contentMode == ContentMode.HTML
        notification.delay == 3000
        notification.position == Notifications.Position.TOP_CENTER
        notification.styleName == 'notification-facet-style'
    }

    def 'Notification Install and Subscribe handlers'() {
        showTestMainScreen()

        def screen = screens.create(NotificationFacetTestScreen)
        screen.show()

        def notification = screen.testNotification

        when: 'Notification is shown'

        notification.show()

        then: 'Caption and description providers are triggered'

        screen.testNotification.captionProvider != null
        screen.testNotification.descriptionProvider != null

        screen.captionProvided
        screen.descriptionProvided

        when: 'All notifications are closed'

        closeAllNotifications()

        then: 'CloseEvent is fired'

        screen.closeEvtFired
    }

    def 'Declarative Notification Action subscription'() {
        showTestMainScreen()

        def screen = screens.create(NotificationFacetTestScreen)

        when: 'Notification target action is performed'

        screen.notificationAction.actionPerform(screen.notificationButton)

        then: 'Notification is shown'

        vaadinUi.getExtensions().find { ext ->
            ext instanceof Notification &&
                    ((Notification) ext).caption == 'Notification Action subscription'
        }
    }

    def 'Declarative Notification Button subscription'() {
        showTestMainScreen()

        def screen = screens.create(NotificationFacetTestScreen)

        when: 'Notification target button is clicked'

        ((ButtonImpl) screen.notificationButton)
                .buttonClicked(null)

        then: 'Notification is shown'

        vaadinUi.getExtensions().find { ext ->
            ext instanceof Notification &&
                    ((Notification) ext).caption == 'Notification Button subscription'
        }

    }

    def 'Notification should be bound to frame'() {
        def notification = new NotificationFacetImpl()

        when: 'Trying to show Notification not bound to frame'

        notification.show()

        then: 'Exception is thrown'

        thrown IllegalStateException

        when: 'Trying to setup declarative subscription without frame'

        notification.subscribe()

        then: 'Exception is thrown'

        thrown IllegalStateException
    }

    def 'Notification should have single subscription'() {
        showTestMainScreen()

        def screen = screens.create(NotificationFacetTestScreen)

        def notification = new NotificationFacetImpl()

        notification.setOwner(screen.getWindow())
        notification.setActionTarget('actionId')
        notification.setButtonTarget('buttonId')

        when: 'Both action and button are set as Notification subscription target'

        notification.subscribe()

        then: 'Exception is thrown'

        thrown GuiDevelopmentException
    }

    def 'Notification target should not be missing'() {
        showTestMainScreen()

        def screen = screens.create(NotificationFacetTestScreen)

        def notification = new NotificationFacetImpl()

        notification.setOwner(screen.getWindow())
        notification.setActionTarget('missingAction')

        when: 'Notification is bound to missing action'

        notification.subscribe()

        then: 'Exception is thrown'

        thrown GuiDevelopmentException

        when: 'Notification is bound to missing button'

        notification.setActionTarget(null)
        notification.setButtonTarget('missingButton')
        notification.subscribe()

        then: 'Exception is thrown'

        thrown GuiDevelopmentException
    }

    protected void closeAllNotifications() {
        def notifications = []
        for (Extension ext : vaadinUi.getExtensions()) {
            if (ext instanceof Notification) {
                notifications.push(ext as Notification)
            }
        }
        for (Notification ntf : notifications) {
            ntf.close()
        }
    }
}
