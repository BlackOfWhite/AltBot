package org.logic.transactions.model.buysell;

import org.logic.transactions.model.OptionImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "BuySellOption")
@XmlAccessorType(XmlAccessType.FIELD)
public class BuySellOption implements Serializable, OptionImpl {
    private static final long serialVersionUID = 1L;
    private String marketName;

    public BuySellOption() {
    }

    public BuySellOption(String marketName) {
        this.marketName = marketName;
    }

    public String getMarketName() {
        return marketName;
    }

    @Override
    public String toString() {
        return "BuySellOption{" +
                "marketName='" + marketName + "'\'}'";
    }
}