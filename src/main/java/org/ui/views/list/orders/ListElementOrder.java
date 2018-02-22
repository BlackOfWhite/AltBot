package org.ui.views.list.orders;

public class ListElementOrder {
    private String coinName, orderType, lastText;
    private double last;

    public ListElementOrder(String coinName, String orderType, double last) {
        this.coinName = coinName;
        this.orderType = orderType;
        setLast(last);
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
        this.lastText = String.format("%.8f", last);
        if (this.lastText.contains(",")) {
            this.lastText = this.lastText.replace(",", ".");
        }
        this.last = last;
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
