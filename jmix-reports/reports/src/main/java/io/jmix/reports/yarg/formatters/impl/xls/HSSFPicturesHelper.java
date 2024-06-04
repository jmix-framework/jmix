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
package io.jmix.reports.yarg.formatters.impl.xls;

import org.apache.poi.ddf.EscherClientAnchorRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class HSSFPicturesHelper {
    private HSSFPicturesHelper() {
    }

    public static List<HSSFClientAnchor> getAllAnchors(EscherAggregate escherAggregate) {
        List<HSSFClientAnchor> pictures = new ArrayList<HSSFClientAnchor>();
        if (escherAggregate == null) return Collections.emptyList();
        List<EscherRecord> escherRecords = escherAggregate.getEscherRecords();
        searchForAnchors(escherRecords, pictures);
        return pictures;
    }

    public static void searchForAnchors(List escherRecords, List<HSSFClientAnchor> pictures) {
        Iterator recordIter = escherRecords.iterator();
        HSSFClientAnchor anchor = null;
        while (recordIter.hasNext()) {
            Object obj = recordIter.next();
            if (obj instanceof EscherRecord) {
                EscherRecord escherRecord = (EscherRecord) obj;
                if (escherRecord instanceof EscherClientAnchorRecord) {
                    EscherClientAnchorRecord anchorRecord = (EscherClientAnchorRecord) escherRecord;
                    if (anchor == null) anchor = new HSSFClientAnchor();
                    anchor.setDx1(anchorRecord.getDx1());
                    anchor.setDx2(anchorRecord.getDx2());
                    anchor.setDy1(anchorRecord.getDy1());
                    anchor.setDy2(anchorRecord.getDy2());
                    anchor.setRow1(anchorRecord.getRow1());
                    anchor.setRow2(anchorRecord.getRow2());
                    anchor.setCol1(anchorRecord.getCol1());
                    anchor.setCol2(anchorRecord.getCol2());
                }
                // Recursive call.
                searchForAnchors(escherRecord.getChildRecords(), pictures);
            }
        }
        if (anchor != null)
            pictures.add(anchor);
    }
}
