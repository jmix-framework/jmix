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

package io.jmix.dashboardsui.screen.dashboard.editor.responsive;

import io.jmix.core.Messages;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Slider;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Map;

import static io.jmix.ui.component.ResponsiveGridLayout.Breakpoint.*;
import static io.jmix.ui.component.Window.CLOSE_ACTION_ID;
import static io.jmix.ui.component.Window.COMMIT_ACTION_ID;

@UiController("dshbrd_ResponsiveCreation.dialog")
@UiDescriptor("responsive-creation-dialog.xml")
public class ResponsiveCreationDialog extends Screen {
    public static final String SCREEN_NAME = "dshbrd_ResponsiveCreation.dialog";
    @Autowired
    protected Messages messages;
    @Autowired
    protected Slider<Integer> xs;
    @Autowired
    protected Slider<Integer> sm;
    @Autowired
    protected Slider<Integer> md;
    @Autowired
    protected Slider<Integer> lg;

    @Subscribe
    public void onInit(InitEvent e) {
        ScreenOptions options = e.getOptions();
        Map<String, Object> params = Collections.emptyMap();
        if (e.getOptions() instanceof MapScreenOptions) {
            params = ((MapScreenOptions) options).getParams();
        }
        init(params);
    }

    public void init(Map<String, Object> params) {
        int xsd = params.get(XS.name()) == null ? 12 : (int) params.get(XS.name());
        int smd = params.get(SM.name()) == null ? 6 : (int) params.get(SM.name());
        int mdd = params.get(MD.name()) == null ? 6 : (int) params.get(MD.name());
        int lgd = params.get(LG.name()) == null ? 3 : (int) params.get(LG.name());

        initXsSlider(xsd);
        initSmSlider(smd);
        initMdSlider(mdd);
        initLgSlider(lgd);
    }

    protected void initXsSlider(int xsd) {
        xs.setCaptionAsHtml(true);
        xs.setCaption(getCaption("dashboard.responsive.xs", xsd));
        xs.setValue(xsd);
        xs.setWidthFull();
        xs.addValueChangeListener(event -> xs.setCaption(getCaption("dashboard.responsive.xs", xs.getValue())));
    }

    protected void initSmSlider(int smd) {
        sm.setCaptionAsHtml(true);
        sm.setCaption(getCaption("dashboard.responsive.sm", smd));
        sm.setValue(smd);
        sm.setWidthFull();
        sm.addValueChangeListener(event -> sm.setCaption(getCaption("dashboard.responsive.sm", sm.getValue())));
    }

    protected void initMdSlider(int mdd) {
        md.setCaptionAsHtml(true);
        md.setCaption(getCaption("dashboard.responsive.md", mdd));
        md.setValue(mdd);
        md.setWidthFull();
        md.addValueChangeListener(event -> md.setCaption(getCaption("dashboard.responsive.md", md.getValue())));
    }

    protected void initLgSlider(int lgd) {
        lg.setCaptionAsHtml(true);
        lg.setCaption(getCaption("dashboard.responsive.lg", lgd));
        lg.setValue(lgd);
        lg.setWidthFull();
        lg.addValueChangeListener(event -> lg.setCaption(getCaption("dashboard.responsive.lg", lg.getValue())));
    }

    protected String getCaption(String message, Integer value) {
        return messages.formatMessage(ResponsiveCreationDialog.class, message, value);
    }

    @Subscribe("okBtn")
    public void apply(Button.ClickEvent event) {
        this.close(new StandardCloseAction(COMMIT_ACTION_ID));
    }

    @Subscribe("cancelBtn")
    public void cancel(Button.ClickEvent event) {
        this.close(new StandardCloseAction(CLOSE_ACTION_ID));
    }

    public Integer getXs() {
        return xs.getValue();
    }

    public Integer getSm() {
        return sm.getValue();
    }

    public Integer getMd() {
        return md.getValue();
    }

    public Integer getLg() {
        return lg.getValue();
    }
}