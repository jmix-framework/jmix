/*
 * Copyright 2024 Haulmont.
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

package io.jmix.reports.yarg.formatters.impl.xlsx;

import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.xssf.usermodel.XSSFDrawing;

import java.awt.*;

public class XlsxImage {
    protected Picture picture;
    protected byte[] pictureData;
    protected Integer dx1;
    protected Integer dx2;
    protected Integer dy1;
    protected Integer dy2;
    protected Dimension size;
    protected XSSFDrawing xssfDrawing;
    protected Integer row;
    protected Integer col;

    public XlsxImage(Picture picture, Integer dx1, Integer dx2, Integer dy1, Integer dy2, Dimension size, XSSFDrawing xssfDrawing, Integer row, Integer col) {
        this.picture = picture;
        this.pictureData = picture.getPictureData().getData();
        this.dx1 = dx1;
        this.dx2 = dx2;
        this.dy1 = dy1;
        this.dy2 = dy2;
        this.size = size;
        this.xssfDrawing = xssfDrawing;
        this.row = row;
        this.col = col;
    }

    public Picture getPicture() {
        return picture;
    }

    public void setPicture(Picture picture) {
        this.picture = picture;
    }

    public byte[] getPictureData() {
        return pictureData;
    }

    public void setPictureData(byte[] pictureData) {
        this.pictureData = pictureData;
    }

    public Dimension getSize() {
        return size;
    }

    public void setSize(Dimension size) {
        this.size = size;
    }

    public XSSFDrawing getXssfDrawing() {
        return xssfDrawing;
    }

    public void setXssfDrawing(XSSFDrawing xssfDrawing) {
        this.xssfDrawing = xssfDrawing;
    }

    public Integer getRow() {
        return row;
    }

    public void setRow(Integer row) {
        this.row = row;
    }

    public Integer getCol() {
        return col;
    }

    public void setCol(Integer col) {
        this.col = col;
    }

    public Integer getDx1() {
        return dx1;
    }

    public void setDx1(Integer dx1) {
        this.dx1 = dx1;
    }

    public Integer getDx2() {
        return dx2;
    }

    public void setDx2(Integer dx2) {
        this.dx2 = dx2;
    }

    public Integer getDy1() {
        return dy1;
    }

    public void setDy1(Integer dy1) {
        this.dy1 = dy1;
    }

    public Integer getDy2() {
        return dy2;
    }

    public void setDy2(Integer dy2) {
        this.dy2 = dy2;
    }
}