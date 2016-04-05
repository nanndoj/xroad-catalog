package fi.vrk.xroad.catalog.collector.actors;

import java.io.Serializable;

/**
 * Created by janne on 5.4.2016.
 */
public class StartWorkingMessage extends ParentIdentifyingMessage implements Serializable {
    public StartWorkingMessage(int parentId) {
        super(parentId);
    }
}
