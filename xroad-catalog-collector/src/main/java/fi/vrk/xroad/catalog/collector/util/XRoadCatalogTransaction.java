package fi.vrk.xroad.catalog.collector.util;

import akka.actor.ActorRef;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sjk on 4.4.2016.
 */
@Getter
@ToString
public class XRoadCatalogTransaction {
    private ActorRef originalSender;
    private Long originalParentId;

    private Set<Long> childIds = new HashSet<>();

    public void addChildId(Long id) {
        childIds.add(id);
    }

    /**
     *
     * @param id
     * @return true, if the last child was removed
     */
    public boolean removeChild(Long id) {
        childIds.remove(id);
        return childIds.isEmpty();
    }

    public XRoadCatalogTransaction(ActorRef originalSender, Long originalParentId) {
        this.originalSender = originalSender;
        this.originalParentId = originalParentId;
    }
}
