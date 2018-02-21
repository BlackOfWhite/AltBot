package org.ui.views.list.orders;

public class ListElementOrder {
    private String coinName, orderType;

    public ListElementOrder(String coinName, String orderType) {
        this.coinName = coinName;
        this.orderType = orderType;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListElementOrder that = (ListElementOrder) o;

        if (!coinName.equals(that.coinName)) return false;
        return orderType.equals(that.orderType);
    }

    @Override
    public int hashCode() {
        int result = coinName.hashCode();
        result = 31 * result + orderType.hashCode();
        return result;
    }
}
