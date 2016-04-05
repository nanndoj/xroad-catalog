package fi.vrk.xroad.catalog.collector.actors;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by janne on 5.4.2016.
 */
public class WorkDoneMessage extends ParentIdentifyingMessage implements Serializable {
    @Getter
    @Setter
    private String payload;

    @Getter
    @Setter
    private int childId;

    public WorkDoneMessage(int parentId, int childId, String payload) {
        super(parentId);
        this.payload = payload;
        this.childId = childId;
    }
}
