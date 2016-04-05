package fi.vrk.xroad.catalog.collector.actors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by janne on 5.4.2016.
 */
@AllArgsConstructor
public class ParentIdentifyingMessage implements Serializable {
    @Getter
    @Setter
    private int parentId;
}
