package fi.vrk.xroad.catalog.collector.actors;

import lombok.ToString;

import java.io.Serializable;

/**
 * Created by janne on 5.4.2016.
 */
@ToString(callSuper = true)
public class NewStartWorkingMessage extends ComboIdMessage implements Serializable {
    public NewStartWorkingMessage() {
    }
}
