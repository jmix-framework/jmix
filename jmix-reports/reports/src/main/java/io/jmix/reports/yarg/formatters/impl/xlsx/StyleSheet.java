/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.reports.yarg.formatters.impl.xlsx;

import org.xlsx4j.sml.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StyleSheet {

    protected CTStylesheet ctStylesheet;
    protected Map<String, CellStyle> namedStyles = new HashMap<String, CellStyle>();
    protected List<CellXfs> cellXfsIndex = new ArrayList<CellXfs>();
    protected List<CellXfs> newCellXfs = new ArrayList<CellXfs>();

    public StyleSheet(CTStylesheet ctStylesheet) {
        this.ctStylesheet = ctStylesheet;
        CTCellStyles ctCellStyles = ctStylesheet.getCellStyles();
        CTCellStyleXfs ctCellStyleXfs = ctStylesheet.getCellStyleXfs();
        CTCellXfs ctCellXfs = ctStylesheet.getCellXfs();
        if (ctCellStyles != null && ctCellStyleXfs != null) {
            for (CTCellStyle ctCellStyle : ctCellStyles.getCellStyle()) {
                try {
                    CTXf xf = ctCellStyleXfs.getXf().get((int) ctCellStyle.getXfId());
                    CellStyle cellStyle = new CellStyle(ctCellStyle.getXfId(),
                            ctCellStyle.getName(),
                            xf.getNumFmtId(),
                            xf.getFontId(),
                            xf.getFillId(),
                            xf.getBorderId());
                    namedStyles.put(cellStyle.getName(), cellStyle);
                } catch (IndexOutOfBoundsException e) {
                    //Do nothing
                }
            }
        }
        if (ctCellXfs != null) {
            for (CTXf xf : ctCellXfs.getXf()) {
                CellXfs cellXfs = new CellXfs(xf.getXfId(),
                        xf.getNumFmtId(),
                        xf.getFontId(),
                        xf.getFillId(),
                        xf.getBorderId());
                cellXfsIndex.add(cellXfs);
            }
        }
    }

    public CellStyle getNamedStyle(String styleName) {
        return namedStyles.get(styleName);
    }

    public CellXfs getCellStyle(long index) {
        return cellXfsIndex.get((int) index);
    }

    public long getCellXfsIndex(CellXfs cellXfs) {
        return cellXfsIndex.indexOf(cellXfs);
    }

    public void addCellXfs(CellXfs cellXfs) {
        cellXfsIndex.add(cellXfs);
        newCellXfs.add(cellXfs);
    }

    public void saveStyle() {
        if (!newCellXfs.isEmpty()) {
            CTCellXfs ctCellXfs = ctStylesheet.getCellXfs();
            if (ctCellXfs == null) {
                ctCellXfs = new CTCellXfs();
                ctStylesheet.setCellXfs(ctCellXfs);
            }
            ctCellXfs.setCount((long) cellXfsIndex.size());
            List<CTXf> xfs = ctCellXfs.getXf();
            for (CellXfs cellXfs : newCellXfs) {
                CTXf ctXf = new CTXf();
                ctXf.setNumFmtId(cellXfs.getNumFmtId());
                ctXf.setFontId(cellXfs.getFontId());
                ctXf.setFillId(cellXfs.getFillId());
                ctXf.setBorderId(cellXfs.getBorderId());
                ctXf.setXfId(cellXfs.getXfId());
                ctXf.setParent(ctCellXfs);
                xfs.add(ctXf);
            }
        }
    }

    public static class CellStyleXfs {
        protected final Long numFmtId;
        protected final Long fontId;
        protected final Long fillId;
        protected final Long borderId;

        public CellStyleXfs(Long numFmtId, Long fontId, Long fillId, Long borderId) {
            this.numFmtId = numFmtId;
            this.fontId = fontId;
            this.fillId = fillId;
            this.borderId = borderId;
        }

        public Long getNumFmtId() {
            return numFmtId;
        }

        public Long getFontId() {
            return fontId;
        }

        public Long getFillId() {
            return fillId;
        }

        public Long getBorderId() {
            return borderId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CellStyleXfs)) return false;

            CellStyleXfs that = (CellStyleXfs) o;

            if (numFmtId != null ? !numFmtId.equals(that.numFmtId) : that.numFmtId != null) return false;
            if (fontId != null ? !fontId.equals(that.fontId) : that.fontId != null) return false;
            if (fillId != null ? !fillId.equals(that.fillId) : that.fillId != null) return false;
            return borderId != null ? borderId.equals(that.borderId) : that.borderId == null;
        }

        @Override
        public int hashCode() {
            int result = numFmtId != null ? numFmtId.hashCode() : 0;
            result = 31 * result + (fontId != null ? fontId.hashCode() : 0);
            result = 31 * result + (fillId != null ? fillId.hashCode() : 0);
            result = 31 * result + (borderId != null ? borderId.hashCode() : 0);
            return result;
        }
    }

    public static class CellStyle extends CellStyleXfs {
        protected final Long xfId;
        protected final String name;

        public CellStyle(Long xfId, String name, Long numFmtId, Long fontId, Long fillId, Long borderId) {
            super(numFmtId, fontId, fillId, borderId);
            this.xfId = xfId;
            this.name = name;
        }

        public Long getXfId() {
            return xfId;
        }

        public String getName() {
            return name;
        }
    }

    public static class CellXfs extends CellStyleXfs {
        protected Long xfId;

        public CellXfs(Long xfId, Long numFmtId, Long fontId, Long fillId, Long borderId) {
            super(numFmtId, fontId, fillId, borderId);
            this.xfId = xfId;
        }

        public Long getXfId() {
            return xfId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CellXfs)) return false;
            if (!super.equals(o)) return false;

            CellXfs cellXfs = (CellXfs) o;

            return xfId != null ? xfId.equals(cellXfs.xfId) : cellXfs.xfId == null;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (xfId != null ? xfId.hashCode() : 0);
            return result;
        }
    }
}
