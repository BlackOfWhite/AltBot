package org.logic.transactions.model.buysell;

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
    private double buyBelowRatio = 0.975d; // 2.5% below avg
    private double totalGainRatio = 1.035d; // 3.5% above bought price
    private double sellAndResetRatio = 0.94d; // 6% will auto sell also below this, below bought price
    private double btc = 0.017;
    private double boughtAt = -1;

    public BotAvgOption() {
    }

    public String getMarketName() {
        return marketName;
    }

    public BotAvgOption(String marketName, double buyBelowRatio, double totalGainRatio, double sellAndResetRatio, double btc) {
        this.marketName = marketName;
        this.buyBelowRatio = buyBelowRatio;
        this.totalGainRatio = totalGainRatio;
        this.sellAndResetRatio = sellAndResetRatio;
        this.btc = btc;
    }

    public BotAvgOption(String marketName) {
        this(marketName, 0.975d, 1.035d, 0.094d, 0.017d);
    }


    public double getBuyBelowRatio() {
        return buyBelowRatio;
    }

    public double getTotalGainRatio() {
        return totalGainRatio;
    }

    public double getSellAndResetRatio() {
        return sellAndResetRatio;
    }

    public double getBtc() {
        return btc;
    }

    public void setBoughtAt(double boughtAt) {
        this.boughtAt = boughtAt;
    }

    public double getBoughtAt() {
        return boughtAt;
    }

    @Override
    public String toString() {
        return "BotAvgOption{" +
                "marketName='" + marketName + '\'' +
                ", buyBelowRatio=" + buyBelowRatio +
                ", totalGainRatio=" + totalGainRatio +
                ", sellAndResetRatio=" + sellAndResetRatio +
                ", btc=" + btc +
                ", boughtAt=" + boughtAt +
                '}';
    }
}