package org.ui.views.list;

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
}
