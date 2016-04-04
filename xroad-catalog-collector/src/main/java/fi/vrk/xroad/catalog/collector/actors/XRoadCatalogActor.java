package fi.vrk.xroad.catalog.collector.actors;

import akka.actor.Terminated;
import akka.actor.UntypedActor;
import fi.vrk.xroad.catalog.collector.util.XRoadCatalogID;
import fi.vrk.xroad.catalog.collector.util.XRoadCatalogMessage;
import fi.vrk.xroad.catalog.collector.util.XRoadCatalogTransaction;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sjk on 4.4.2016.
 */
@Slf4j
public abstract class XRoadCatalogActor extends UntypedActor {
    protected static AtomicInteger COUNTER = new AtomicInteger(0);
    protected Map<Long, XRoadCatalogTransaction> transactionMap;
    protected Long myId;
    protected XRoadCatalogTransaction currentTransaction;
    private long currentChildId = 0;

    protected long generateChildId() {
        return ++currentChildId;
    }

    protected void handleEndOfChildren() {
        log.info("{} {} No more chilren", COUNTER,
                this
                        .getClass());

    }

    protected Object handleXRoadCatalogMessage(Object message) {

        log.info("{} onReceive {}", COUNTER.addAndGet(1), this.hashCode());
        if (message instanceof XRoadCatalogMessage) {
            log.info("{} TransactionMap {}", COUNTER, transactionMap);

            XRoadCatalogMessage m = (XRoadCatalogMessage) message;
            if (transactionMap == null) {
                transactionMap = new HashMap<>();
            }
            myId = m.getId().getChildID();
            currentTransaction = transactionMap.get(m.getId().getChildID());
            if (currentTransaction == null) {
                currentTransaction = new XRoadCatalogTransaction(getSender(), m.getId().getParentID());
            }
            transactionMap.put(m.getId().getChildID(), currentTransaction);
            return m.getPayload();
        } else if (message instanceof XRoadCatalogID) {
            XRoadCatalogID id = (XRoadCatalogID) message;
            log.info("{} {} - Child sent done message {}. TransactionMap {}", this.hashCode(), this.getClass(), id,
                    transactionMap);
            XRoadCatalogTransaction transaction = transactionMap.get(id.getParentID());
            log.info("{} {} - Child done. Message {}. Transaction {}", this.hashCode(), this.getClass(), id,
                    transaction);
            if (transaction.removeChild(id.getChildID())) {
                log.info("{} {} - Last child removed, sending message to sender", this.hashCode(), this.getClass());
                transaction.getOriginalSender().tell(new XRoadCatalogID(transaction.getOriginalParentId(), id
                        .getParentID()), getSelf());
                handleEndOfChildren();
            }
        } else if (message instanceof Terminated) {
            throw new RuntimeException("Terminated: " + message);
        } else {
            log.error("Unable to handle message {}", message);
            throw new RuntimeException("Unable to handle message");
        }
        return null;
    }

    protected XRoadCatalogID createXRoadCatalogIDForChild() {
        long myChildId = generateChildId();
        log.info("{} {} - Creating child id {} for {} ", this.hashCode(), this.getClass(), myChildId, myId);
        currentTransaction.addChildId(myChildId);
        return new XRoadCatalogID(myId, myChildId);
    }
}
