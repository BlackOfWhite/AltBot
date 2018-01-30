package org.logic.transactions.model.bots;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "BotAvgOptions")
public class BotAvgOptions {

    public BotAvgOptions() {
    }

    @XmlElement(name = "BotAvgOption", type = BotAvgOption.class)
    private ArrayList<BotAvgOption> collection;

    public BotAvgOptions(ArrayList<BotAvgOption> collection) {
        this.collection = collection;
    }

    public ArrayList<BotAvgOption> getCollection() {
        return collection;
    }
}
