package fi.vrk.xroad.catalog.collector.util;

import akka.actor.ActorRef;
import akka.routing.ConsistentHashingRouter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by sjk on 4.4.2016.
 */
@Getter
@ToString
@AllArgsConstructor
public class XRoadCatalogMessage implements ConsistentHashingRouter.ConsistentHashable {
    private XRoadCatalogID id;
    private Object payload;

    @Override
    public Object consistentHashKey() {
        return id.getParentID();
    }
}
