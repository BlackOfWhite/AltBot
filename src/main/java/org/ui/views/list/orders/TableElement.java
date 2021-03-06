package org.ui.views.list.orders;

import java.util.Comparator;

import static org.logic.utils.TextUtils.getDoubleAsText;

public class TableElement {
    private String coinName, orderType, lastText, maxText, minText;
    private double last, min, max;
    private String minLabel = "", maxLabel = "";
    private String uuid; // used only for real open orders

    // Minimum is 0.0 by default
    public TableElement(String coinName, String orderType, double last, double max) {
        this.coinName = coinName;
        this.orderType = orderType;
        setLast(last);
        setMax(max);
        setMin(0.0d);
    }

    public static Comparator getComparator() {
        // Sort
        Comparator comparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                TableElement l1 = (TableElement) o1;
                TableElement l2 = (TableElement) o2;
                int cmp = l1.getCoinName().compareTo(l2.getCoinName());
                if (cmp == 0) {
                    cmp = l1.getOrderType().compareTo(l2.getOrderType());
                }
                if (cmp == 0) {
                    if (l1.getLast() < l2.getLast()) {
                        return -1;
                    } else if (l1.getLast() > l2.getLast()) {
                        return 1;
                    }
                    return 0;
                }
                return cmp;
            }
        };
        return comparator;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
        this.minText = getDoubleAsText(min);
    }

    public String getMinText() {
        return minText;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
        this.maxText = getDoubleAsText(max);
    }

    public String getMaxText() {
        return maxText;
    }

    public double getLast() {
        return last;
    }

    public void setLast(double last) {
        this.lastText = getDoubleAsText(last);
        this.last = last;
    }

    public String getLastAsString() {
        return lastText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableElement that = (TableElement) o;
        if (!coinName.equals(that.coinName)) return false;
        return orderType.equals(that.orderType);
    }

    @Override
    public int hashCode() {
        int result = coinName.hashCode();
        result = 31 * result + orderType.hashCode();
        return result;
    }

    public String getMinLabel() {
        return minLabel;
    }

    public void setMinLabel(String s) {
        this.minLabel = s;
    }

    public String getMaxLabel() {
        return maxLabel;
    }

    public void setMaxLabel(String s) {
        this.maxLabel = s;
    }

    @Override
    public String toString() {
        return "TableElement{" +
                "coinName='" + coinName + '\'' +
                ", orderType='" + orderType + '\'' +
                ", lastText='" + lastText + '\'' +
                ", maxText='" + maxText + '\'' +
                ", minText='" + minText + '\'' +
                ", defaultColor=" + last +
                ", min=" + min +
                ", max=" + max +
                '}';

    }

    public void doCancelClick() {
        System.out.print(">>>" + toString());
    }
}
