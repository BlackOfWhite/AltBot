package org.logic.transactions.model.stoploss;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "StopLossOptions")
public class StopLossOptions {

    public StopLossOptions() {
    }

    @XmlElement(name = "StopLossOption", type = StopLossOption.class)
    private ArrayList<StopLossOption> collection;

    public StopLossOptions(ArrayList<StopLossOption> collection) {
        this.collection = collection;
    }

    public ArrayList<StopLossOption> getCollection() {
        return collection;
    }
}
