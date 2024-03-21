/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.screen;

import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.Facet;
import io.jmix.ui.component.Fragment;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.Window;
import io.jmix.ui.model.ScreenData;
import org.springframework.context.ApplicationListener;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Internal methods used in Screens and Fragments implementations.
 */
@ParametersAreNonnullByDefault
public final class UiControllerUtils {

    public static void setWindowId(FrameOwner screen, String id) {
        if (screen instanceof Screen) {
            ((Screen) screen).setId(id);
        } else if (screen instanceof ScreenFragment) {
            ((ScreenFragment) screen).setId(id);
        }
    }

    public static void setFrame(FrameOwner screen, Frame window) {
        if (screen instanceof Screen) {
            ((Screen) screen).setWindow((Window) window);
        } else if (screen instanceof ScreenFragment) {
            ((ScreenFragment) screen).setFragment((Fragment) window);
        }
    }

    public static <E> void fireEvent(FrameOwner screen, Class<E> eventType, E event) {
        if (screen instanceof Screen) {
            ((Screen) screen).fireEvent(eventType, event);
        } else if (screen instanceof ScreenFragment) {
            ((ScreenFragment) screen).fireEvent(eventType, event);
        }
    }

    public static EventHub getEventHub(FrameOwner frameOwner) {
        if (frameOwner instanceof Screen) {
            return ((Screen) frameOwner).getEventHub();
        }
        return ((ScreenFragment) frameOwner).getEventHub();
    }

    public static void setScreenContext(FrameOwner screen, ScreenContext screenContext) {
        if (screen instanceof Screen) {
            ((Screen) screen).setScreenContext(screenContext);
        } else if (screen instanceof ScreenFragment) {
            ((ScreenFragment) screen).setScreenContext(screenContext);
        }
    }

    public static ScreenContext getScreenContext(FrameOwner frameOwner) {
        if (frameOwner instanceof Screen) {
            return ((Screen) frameOwner).getScreenContext();
        }
        return ((ScreenFragment) frameOwner).getScreenContext();
    }

    public static ScreenData getScreenData(FrameOwner frameOwner) {
        if (frameOwner instanceof Screen) {
            return ((Screen) frameOwner).getScreenData();
        }
        return ((ScreenFragment) frameOwner).getScreenData();
    }

    public static void setScreenData(FrameOwner screen, ScreenData screenData) {
        if (screen instanceof Screen) {
            screenData.setScreenId(((Screen) screen).getId());
            ((Screen) screen).setScreenData(screenData);
        } else if (screen instanceof ScreenFragment) {
            screenData.setScreenId(((ScreenFragment) screen).getId());
            ((ScreenFragment) screen).setScreenData(screenData);
        }
    }

    public static Frame getFrame(FrameOwner frameOwner) {
        if (frameOwner instanceof Screen) {
            return ((Screen) frameOwner).getWindow();
        }
        return ((ScreenFragment) frameOwner).getFragment();
    }

    public static void setUiEventListeners(FrameOwner frameOwner, List<ApplicationListener> listeners) {
        if (frameOwner instanceof Screen) {
            ((Screen) frameOwner).setUiEventListeners(listeners);
        } else if (frameOwner instanceof ScreenFragment) {
            ((ScreenFragment) frameOwner).setUiEventListeners(listeners);
        }
    }

    @Nullable
    public static List<ApplicationListener> getUiEventListeners(FrameOwner frameOwner) {
        if (frameOwner instanceof Screen) {
            return ((Screen) frameOwner).getUiEventListeners();
        } else if (frameOwner instanceof ScreenFragment) {
            return ((ScreenFragment) frameOwner).getUiEventListeners();
        }
        return Collections.emptyList();
    }

    public static String getPackage(Class controllerClass) {
        Package javaPackage = controllerClass.getPackage();
        if (javaPackage != null) {
            return javaPackage.getName();
        }

        // infer from FQN, hot-deployed classes do not have package
        // see JDK-8189231

        String canonicalName = controllerClass.getCanonicalName();
        int dotIndex = canonicalName.lastIndexOf('.');

        if (dotIndex >= 0) {
            return canonicalName.substring(0, dotIndex);
        }

        return "";
    }

    public static void setHostController(ScreenFragment fragment, FrameOwner hostController) {
        fragment.setHostController(hostController);
    }

    public static Subscription addAfterDetachListener(Screen screen, Consumer<Screen.AfterDetachEvent> listener) {
        return screen.addAfterDetachListener(listener);
    }

    public static Subscription addDetachListener(ScreenFragment screen, Consumer<ScreenFragment.DetachEvent> listener) {
        return screen.addDetachEventListener(listener);
    }

    public static Screen getHostScreen(ScreenFragment fragment) {
        return fragment.getHostScreen();
    }

    public static Screen getScreen(FrameOwner screen) {
        Screen hostScreen;
        if (screen instanceof Screen) {
            hostScreen = (Screen) screen;
        } else {
            hostScreen = UiControllerUtils.getHostScreen((ScreenFragment) screen);
        }
        return hostScreen;
    }

    public static boolean isMultipleOpen(Screen screen) {
        return screen.isMultipleOpen();
    }

    public static boolean isAlreadyOpened(Screen newScreen, Screen openedScreen) {
        return newScreen.isSameScreen(openedScreen);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends Facet> T getFacet(Frame frame, Class<T> facetClass) {
        return (T) frame.getFacets()
                .filter(facet -> facetClass.isAssignableFrom(facet.getClass()))
                .findFirst()
                .orElse(null);
    }
}
