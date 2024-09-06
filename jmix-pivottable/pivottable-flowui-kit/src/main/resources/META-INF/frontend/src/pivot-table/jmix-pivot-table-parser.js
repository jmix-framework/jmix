export class PivotTableParser {

    floatFormatAggregationIds = ['sum', 'average', 'minimum', 'maximum', 'sumOverSum', 'upperBound80', 'lowerBound80'];
    integerFormatAggregationIds = ['count', 'countUniqueValues', 'integerSum'];
    boldClassNames = ['pvtTotal rowTotal', 'pvtTotal colTotal', 'pvtGrandTotal'];

    constructor(pivotMessages) {
        this.pivotMessages = pivotMessages;
    }

    getTable(pivotElement) {
        var tableElements = pivotElement.getElementsByClassName('pvtTable');
        if (tableElements.length === 0) {
            return;
        }
        return tableElements[0];
    }

    getColsRowsNumber(table, attributeName) {
        return table.attributes[attributeName] ? table.attributes[attributeName].value : null;
    }

    getRows(tableRows, modelRowStartIndex) {
        var modelRows = [];
        for (var i = 0; i < tableRows.length; i++) {
            var modelRow = this.convertRowToModel(tableRows[i], i + modelRowStartIndex);
            modelRows.push(modelRow);
        }
        return modelRows;
    }

    convertRowToModel(tableRow, rowNumber) {
        var modelRow = {};
        modelRow.tableRowNumber = rowNumber;
        modelRow.cells = [];

        for (var j = 0; j < tableRow.cells.length; j++) {
            var cell = tableRow.cells[j];
            var modelCell = {};

            // check for bold
            if (cell.nodeName === 'TH' || this.boldClassNames.indexOf(cell.className) > -1) {
                modelCell.isBold = true;
            } else {
                modelCell.isBold = false;
            }

            if (cell.className) {
                modelCell.className = cell.className;
            }

            modelCell.colSpan = cell.colSpan;
            modelCell.rowSpan = cell.rowSpan;

            if (cell.outerText) {
                modelCell.value = cell.outerText;
            } else if (cell.innerText) {
                modelCell.value = cell.innerText;
            }

            if (cell.attributes['data-value']) {
                if (cell.attributes['data-value'].value !== 'null'
                        && cell.attributes['data-value'].value !== "") {
                    modelCell.value = cell.attributes['data-value'].value;
                }
            }

            modelCell.type = this.getCellType({value: modelCell.value, isPvtLabel: cell.nodeName === 'TH'});
            modelCell.value = this.parseCellValue(modelCell);

            modelRow.cells.push(modelCell);
        }
        return modelRow;
    }

    getCellType(valueObj) {
        var value = valueObj.value;
        if (!value) {
            return 'STRING';
        }

        let date = new Date(value);
        if (!isNaN(date.getTime())) {
            if (date.getFullYear() === 1970 && date.getMonth() === 0 && date.getDate() === 1) {
                return 'TIME';
            } else if (date.getHours() === 0 && date.getMinutes() === 0 && date.getSeconds() === 0) {
                return 'DATE';
            }
            return 'DATE_TIME';
        }

        if (valueObj.isPvtLabel) {
            // cell is a label check type like a Jmix Datatype

            // trying to convert to decimal number
            var thousandSep = this.pivotMessages.floatFormat.thousandsSep;
            var decimalVal = value.replaceAll(thousandSep, "");

            var decimalSep = this.pivotMessages.floatFormat.decimalSep;
            if (decimalSep !== '.') {
                decimalVal = decimalVal.replace(decimalSep, ".");
            }
            if (decimalVal.includes('.') && !isNaN(decimalVal)) {
                return 'DECIMAL';
            }

            // trying to convert to integer number
            thousandSep = this.pivotMessages.integerFormat.thousandsSep;
            var integerVal = value.replaceAll(thousandSep, "");
            if (!isNaN(integerVal)) {
                return 'INTEGER';
            }

            return 'STRING';
        } else {
        // it is generated cell by aggregation, check cell type by current aggregation

        if (this.floatFormatAggregationIds.indexOf(this.aggregation) > -1) {
            var prefix = this.pivotMessages.floatFormat.prefix;
            var suffix = this.pivotMessages.floatFormat.suffix;
            if ((prefix && prefix.length > 0) || (suffix && suffix.length > 0)
                    || value == Number.POSITIVE_INFINITY || value == Number.NEGATIVE_INFINITY) {
                    return 'STRING';
            }
            return 'DECIMAL';

        } else if (this.integerFormatAggregationIds.indexOf(this.aggregation) > -1) {
            var prefix = this.pivotMessages.integerFormat.prefix;
            var suffix = this.pivotMessages.integerFormat.suffix;
            if ((prefix && prefix.length > 0) || (suffix && suffix.length > 0)) {
                    return 'STRING';
            }
            return 'INTEGER';
        } else if (!isNaN(value)) {
            return value % 1 == 0 ? 'INTEGER' : 'DECIMAL';
        }
        }
        return 'STRING';
    }

    parseCellValue(modelCell) {
        // parse cell value, because numbers in label may have incorrect formatting
        if (modelCell.type == 'STRING') {
            if (modelCell.value === 'Infinity') {
                return "";
            }
        } else if (modelCell.type === 'INTEGER') {
            var thousandSep = this.pivotMessages.integerFormat.thousandsSep;
            return modelCell.value.replaceAll(thousandSep, "");

        } else if (modelCell.type === 'DECIMAL') {
            var thousandSep = this.pivotMessages.floatFormat.thousandsSep;
            var result = modelCell.value.replaceAll(thousandSep, "");

            // replace decimal separator if it is not a dot
            var decimalSep = this.pivotMessages.floatFormat.decimalSep;
            if (decimalSep !== '.') {
                result = result.replace(decimalSep, ".");
            }
            return result;
        }
        return modelCell.value;
    }

    static parseToJson(pivotElement, pivotMessages) {
        let parser = new PivotTableParser(pivotMessages);

        var resultObject = {};

        var table = parser.getTable(pivotElement);
        if (!table) {
            return JSON.stringify(resultObject);
        }

        resultObject.dataNumRows = parser.getColsRowsNumber(table, 'data-numrows');
        resultObject.dataNumCols = parser.getColsRowsNumber(table, 'data-numcols');

        var tableHead = table.tHead;
        resultObject.headRows = parser.getRows(tableHead.rows, 0);

        var tableBody = table.tBodies[0];
        resultObject.bodyRows = parser.getRows(tableBody.rows, resultObject.headRows.length);

        return resultObject;
    }
}