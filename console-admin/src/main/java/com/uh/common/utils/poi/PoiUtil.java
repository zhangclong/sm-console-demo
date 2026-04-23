package com.uh.common.utils.poi;

import com.uh.common.core.text.Convert;
import com.uh.common.utils.AssertUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

public class PoiUtil {

    /**
     * 将POI转成JAVA的类型允许为空若为空返回null
     *
     * @param cell
     * @param t
     * @param <T>
     * @return
     */
    public static <T> T getPoiToJType(Cell cell, Class<?> t) {
        return getPoiToJType(cell, t, true);
    }

    /**
     * 将POI单元格数据转换为Java类型
     *
     * @param cell
     * @param t
     * @param allowNull 是否允许为空，为false时，空值会抛出异常。
     * @param <T>
     * @return
     */
    public static <T> T getPoiToJType(Cell cell, Class<?> t, boolean allowNull) {
        if (cell == null) {
            if (!allowNull) throw new RuntimeException("单元格内容为空！");
            return null;
        }
        CellType cellType = cell.getCellType();

        Object value = null;

        if (cellType == CellType.NUMERIC) {
            value = cell.getNumericCellValue();
        } else if (cellType == CellType.STRING) {
            value = cell.getStringCellValue();
        } else if (cellType == CellType.BOOLEAN) {
            value = cell.getBooleanCellValue();
        } else {
            if (!allowNull)
                throw new RuntimeException("无法处理的类型！");
            else return null;
        }


        if (t == String.class) {
            if (value instanceof String) return (T) value;

            if (value instanceof Double) {
                double num = (double) value;
                if (num == Math.floor(num) && !Double.isInfinite(num)) {
                    return (T) Convert.toStr(Convert.toInt(value));
                }
            }

            return (T) Convert.toStr(value);
        } else if (t == Integer.class) {
            return (T) Convert.toInt(value);
        } else if (t == Long.class) {
            return (T) Convert.toLong(value);
        } else if (t == Boolean.class) {
            if (value instanceof Boolean) return (T) value;
            return (T) Convert.toBool(value);
        } else if (t == Double.class) {
            if (value instanceof Double) return (T) value;
            return (T) Convert.toDouble(value);
        } else {
            if (!allowNull)
                throw new RuntimeException("单元格内容(" + cell.getAddress() + ")为空！");
            else return null;
        }
    }

    /**
     * 将POI单元格数据转换为Java类型，若为空则抛出异常
     *
     * @param cell     单元格
     * @param t        转到的JAVA类型如：String.class
     * @param errorMsg 抛出RuntimeException的错误信息
     * @param <T>
     * @return
     */
    public static <T> T getPoiToJType(Cell cell, Class<?> t, String errorMsg) {

        Object v = getPoiToJType(cell, t);
        AssertUtils.ObjectIsNull(v, errorMsg);

        return (T) v;
    }
}
