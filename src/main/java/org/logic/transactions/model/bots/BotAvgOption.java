package org.logic.transactions.model.bots;

import org.logic.transactions.model.OptionImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "BotAvgOption")
@XmlAccessorType(XmlAccessType.FIELD)
public class BotAvgOption implements Serializable, OptionImpl {
    private static final long serialVersionUID = 1L;
    private String marketName;
    private double btc = 0.017;
    private double sellAbove = -1;
    private double stopLoss = -1;

    public BotAvgOption() {
    }

    public BotAvgOption(String marketName, double btc) {
        this.marketName = marketName;
        this.btc = btc;
    }

    public String getMarketName() {
        return marketName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BotAvgOption that = (BotAvgOption) o;

        return marketName.equals(that.marketName);
    }

    @Override
    public int hashCode() {
        return marketName.hashCode();
    }

    public double getBtc() {
        return btc;
    }

    public double getSellAbove() {
        return sellAbove;
    }

    public void setSellAbove(double sellAbove) {
        this.sellAbove = sellAbove;
    }

    public double getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(double stopLoss) {
        this.stopLoss = stopLoss;
    }

    @Override
    public String toString() {
        return "BotAvgOption{" +
                "marketName='" + marketName + '\'' +
                ", btc=" + btc +
                ", sellAbove=" + sellAbove +
                ", stopLoss=" + stopLoss +
                '}';
    }
}