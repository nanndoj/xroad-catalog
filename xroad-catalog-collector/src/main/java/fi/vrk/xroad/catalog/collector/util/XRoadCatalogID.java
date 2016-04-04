package fi.vrk.xroad.catalog.collector.util;

import akka.routing.ConsistentHashingRouter;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by sjk on 4.4.2016.
 */
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class XRoadCatalogID implements ConsistentHashingRouter.ConsistentHashable {
    private long parentID;
    private long childID;

    @Override
    public Object consistentHashKey() {
        return parentID;
    }
}
