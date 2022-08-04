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

package io.jmix.ui.deviceinfo;

import com.google.common.base.Strings;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.WebBrowser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.TimeZone;

@Component("ui_DeviceInfoProvider")
public class DeviceInfoProviderImpl implements DeviceInfoProvider {

    private static final String ATTR_NAME = "ui_DeviceInfoProvider";

    @Nullable
    @Override
    public DeviceInfo getDeviceInfo() {
        // per request cache
        HttpServletRequest currentServletRequest = VaadinServletService.getCurrentServletRequest();
        if (currentServletRequest == null) {
            return null;
        }

        DeviceInfo deviceInfo = (DeviceInfo) currentServletRequest.getAttribute(ATTR_NAME);
        if (deviceInfo != null) {
            return deviceInfo;
        }

        Page page = Page.getCurrent();

        if (page == null) {
            return null;
        }

        WebBrowser webBrowser = page.getWebBrowser();

        DeviceInfo di = new DeviceInfo();

        di.setAddress(webBrowser.getAddress());
        di.setBrowserApplication(webBrowser.getBrowserApplication());
        di.setBrowserMajorVersion(webBrowser.getBrowserMajorVersion());
        di.setBrowserMinorVersion(webBrowser.getBrowserMinorVersion());

        di.setChrome(webBrowser.isChrome());
        di.setChromeFrame(webBrowser.isChromeFrame());
        di.setChromeFrameCapable(webBrowser.isChromeFrameCapable());
        di.setEdge(webBrowser.isEdge());
        di.setFirefox(webBrowser.isFirefox());
        di.setOpera(webBrowser.isOpera());
        di.setIE(webBrowser.isIE());
        di.setSafari(webBrowser.isSafari());

        if (webBrowser.isWindows()) {
            di.setOperatingSystem(DeviceInfo.OperatingSystem.WINDOWS);
        } else if (webBrowser.isAndroid()) {
            di.setOperatingSystem(DeviceInfo.OperatingSystem.ANDROID);
        } else if (webBrowser.isIOS()) {
            di.setOperatingSystem(DeviceInfo.OperatingSystem.IOS);
        } else if (webBrowser.isMacOSX()) {
            di.setOperatingSystem(DeviceInfo.OperatingSystem.MACOSX);
        } else if (webBrowser.isLinux()) {
            di.setOperatingSystem(DeviceInfo.OperatingSystem.LINUX);
        }

        di.setTouchDevice(webBrowser.isTouchDevice());
        di.setIPad(webBrowser.isIPad());
        di.setIPhone(webBrowser.isIPhone());
        di.setWindowsPhone(webBrowser.isWindowsPhone());

        di.setSecureConnection(webBrowser.isSecureConnection());
        di.setLocale(webBrowser.getLocale());

        di.setScreenHeight(webBrowser.getScreenHeight());
        di.setScreenWidth(webBrowser.getScreenWidth());

        di.setTimeZone(detectTimeZone(webBrowser));

        currentServletRequest.setAttribute(ATTR_NAME, di);

        return di;
    }

    protected TimeZone detectTimeZone(WebBrowser webBrowser) {
        String timeZoneId = webBrowser.getTimeZoneId();
        if (!Strings.isNullOrEmpty(timeZoneId)) {
            return TimeZone.getTimeZone(timeZoneId);
        } else {
            int offset = webBrowser.getTimezoneOffset() / 1000 / 60;
            char sign = offset >= 0 ? '+' : '-';
            int absOffset = Math.abs(offset);

            String hours = StringUtils.leftPad(String.valueOf(absOffset / 60), 2, '0');
            String minutes = StringUtils.leftPad(String.valueOf(absOffset % 60), 2, '0');

            return TimeZone.getTimeZone("GMT" + sign + hours + minutes);
        }
    }
}
