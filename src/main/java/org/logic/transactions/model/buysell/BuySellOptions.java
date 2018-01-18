package org.logic.transactions.model.buysell;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Options")
public class BuySellOptions {

    public BuySellOptions() {
    }

    @XmlElement(name = "BuySellOption", type = BuySellOption.class)
    private ArrayList<BuySellOption> collection;

    public BuySellOptions(ArrayList<BuySellOption> collection) {
        this.collection = collection;
    }

    public ArrayList<BuySellOption> getCollection() {
        return collection;
    }
}
