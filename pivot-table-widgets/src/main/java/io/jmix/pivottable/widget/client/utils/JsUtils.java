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

package io.jmix.pivottable.widget.client.utils;

import com.google.gwt.core.client.JavaScriptObject;

public class JsUtils {

    public static native void merge(JavaScriptObject dst, JavaScriptObject src) /*-{
        for (var property in src) {
            if (src.hasOwnProperty(property)) {
                if (src[property] && typeof src[property] === "object") {
                    if (!dst[property]) {
                        dst[property] = src[property];
                    } else {
                        arguments.callee(dst[property], src[property]);
                    }
                } else {
                    dst[property] = src[property];
                }
            }
        }
    }-*/;

    public static native void applyCustomJson(JavaScriptObject config, String manualOptions) /*-{
        var cfg = $wnd.eval("(" + manualOptions + ")");
        @io.jmix.pivottable.widget.client.utils.JsUtils::merge(*)(config, cfg);
    }-*/;

    public static native void activateFunctions(JavaScriptObject config, boolean removeSuffix) /*-{
        // function property names ends with 'Function'
        var reFunction = /Function$/;
        var active = function (obj) {
            for (var prop in obj) {
                if (obj.hasOwnProperty(prop)) {
                    if (prop.match(reFunction)) {
                        var func = $wnd.eval("(" + obj[prop] + ")");
                        if (removeSuffix) {
                            obj[prop.replace(reFunction, "")] = func;
                            delete obj[prop];
                        } else {
                            obj[prop] = func;
                        }
                    } else if (typeof obj[prop] === "object") {
                        arguments.callee(obj[prop]);
                    }
                }
            }
        };
        active(config);
    }-*/;

    public static native void replaceProperty(JavaScriptObject config, String origin, String replacer) /*-{
        if (config[origin]) {
            config[replacer] = config[origin];
            delete config[origin];
        }
    }-*/;

    public static native void getKeyByValue(JavaScriptObject object, String value) /*-{
        for (var prop in object) {
            if (object.hasOwnProperty(prop) && object[prop] === value) {
                return prop;
            }
        }
    }-*/;

    public static native Object getValueByKey(JavaScriptObject object, String key) /*-{
        if (object.hasOwnProperty(key)) {
            return object[key];
        } else {
            return null;
        }
    }-*/;
}
