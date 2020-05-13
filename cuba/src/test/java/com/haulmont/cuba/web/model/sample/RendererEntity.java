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

package com.haulmont.cuba.web.model.sample;

import io.jmix.core.metamodel.annotations.ModelObject;
import io.jmix.core.metamodel.annotations.ModelProperty;
import io.jmix.data.entity.BaseUuidEntity;

import java.util.Date;

@ModelObject(name = "test_RendererEntity")
public class RendererEntity extends BaseUuidEntity {

    @ModelProperty
    protected String button;

    @ModelProperty
    protected String component;

    @ModelProperty
    protected Boolean checkBox;

    @ModelProperty
    protected String clickableText;

    @ModelProperty
    protected Date date;

    @ModelProperty
    protected String icon;

    @ModelProperty
    protected String image;

    @ModelProperty
    protected String html;

    @ModelProperty
    protected String localDate;

    @ModelProperty
    protected String localDateTime;

    @ModelProperty
    protected Double number;

    @ModelProperty
    protected Double progressBar;

    @ModelProperty
    protected String text;

    public String getButton() {
        return button;
    }

    public void setButton(String button) {
        this.button = button;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public Boolean getCheckBox() {
        return checkBox;
    }

    public void setCheckBox(Boolean checkBox) {
        this.checkBox = checkBox;
    }

    public String getClickableText() {
        return clickableText;
    }

    public void setClickableText(String clickableText) {
        this.clickableText = clickableText;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getLocalDate() {
        return localDate;
    }

    public void setLocalDate(String localDate) {
        this.localDate = localDate;
    }

    public String getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(String localDateTime) {
        this.localDateTime = localDateTime;
    }

    public Double getNumber() {
        return number;
    }

    public void setNumber(Double number) {
        this.number = number;
    }

    public Double getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(Double progressBar) {
        this.progressBar = progressBar;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
