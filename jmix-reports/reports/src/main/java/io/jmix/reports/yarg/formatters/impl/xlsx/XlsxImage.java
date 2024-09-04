package io.jmix.reports.yarg.formatters.impl.xlsx;

import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.xssf.usermodel.XSSFDrawing;

import java.awt.*;

public class XlsxImage {
    protected Picture picture;
    protected Dimension size;
    protected XSSFDrawing xssfDrawing;
    protected Integer row;
    protected Integer col;

    public XlsxImage(Picture picture, Dimension size, XSSFDrawing xssfDrawing, Integer row, Integer col) {
        this.picture = picture;
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

}