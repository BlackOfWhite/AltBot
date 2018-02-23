package org.ui.views.list.orders;

import static org.logic.utils.TextUtils.getDoubleAsText;

public class ListElementOrder {
    private String coinName, orderType, lastText, limitText;
    private double last, limit;

    public ListElementOrder(String coinName, String orderType, double last, double limit) {
        this.coinName = coinName;
        this.orderType = orderType;
        setLast(last);
        setLimit(limit);
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

    public double getLast() {
        return last;
    }

    public void setLast(double last) {
        this.lastText = getDoubleAsText(last);
        this.last = last;
    }

    public String getLimitAsText() {
        return limitText;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limitText = getDoubleAsText(limit);
        this.limit = limit;
    }

    public String getLastAsString() {
        return lastText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListElementOrder that = (ListElementOrder) o;

        if (Double.compare(that.last, last) != 0) return false;
        if (!coinName.equals(that.coinName)) return false;
        if (!orderType.equals(that.orderType)) return false;
        return lastText.equals(that.lastText);
    }

    @Override
    public int hashCode() {
        int result = coinName.hashCode();
        result = 31 * result + orderType.hashCode();
        result = 31 * result + lastText.hashCode();
        return result;
    }
}
