package fi.vrk.xroad.catalog.collector.actors;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import com.typesafe.config.ConfigException;
import fi.vrk.xroad.catalog.collector.extension.SpringExtension;
import fi.vrk.xroad.catalog.collector.util.ClientTypeUtil;
import fi.vrk.xroad.catalog.collector.util.XRoadClient;
import fi.vrk.xroad.catalog.collector.wsimport.ClientType;
import fi.vrk.xroad.catalog.collector.wsimport.XRoadClientIdentifierType;
import fi.vrk.xroad.catalog.collector.wsimport.XRoadObjectType;
import fi.vrk.xroad.catalog.collector.wsimport.XRoadServiceIdentifierType;
import fi.vrk.xroad.catalog.persistence.CatalogService;
import fi.vrk.xroad.catalog.persistence.entity.Member;
import fi.vrk.xroad.catalog.persistence.entity.Service;
import fi.vrk.xroad.catalog.persistence.entity.Subsystem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.IllegalStateException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Actor which fetches all clients, and delegates listing
 * their methods to ListMethodsActors
 */
@Component
@Scope("prototype")
@Slf4j
public class ListMethodsActor extends UntypedActor {

    private static AtomicInteger INSTANCE_COUNTER = new AtomicInteger(0);
    private int instance;

    private AtomicInteger childCounter;
    private boolean allSent = false;

    public ListMethodsActor() {
        instance = INSTANCE_COUNTER.addAndGet(1);
        childCounter = new AtomicInteger(0);
        log.info("(*) ListMethodsActor instance {} created", instance);
    }


    private static AtomicInteger COUNTER = new AtomicInteger(0);
    // to test fault handling
    private static boolean FORCE_FAILURES = false;

    @Value("${xroad-catalog.xroad-instance}")
    private String xroadInstance;

    @Value("${xroad-catalog.member-code}")
    private String memberCode;

    @Value("${xroad-catalog.member-class}")
    private String memberClass;

    @Value("${xroad-catalog.subsystem-code}")
    private String subsystemCode;

    @Value("${xroad-catalog.webservices-endpoint}")
    private String webservicesEndpoint;

    @Autowired
    private SpringExtension springExtension;

    @Autowired
    protected CatalogService catalogService;

    // supervisor-created pool of list methods actors
    private ActorRef fetchWsdlPoolRef;


    @Override
    public void preStart() throws Exception {
        log.info("preStart {}", this.hashCode());
        fetchWsdlPoolRef = new RelativeActorRefUtil(getContext())
                .resolvePoolRef(Supervisor.FETCH_WSDL_ACTOR_ROUTER);
        super.preStart();
    }

    @Override
    public void postStop() throws Exception {
        log.info("postStop {}", this.hashCode());
        super.postStop();
    }


    int startWorking = 0;

    @Override
    public void onReceive(Object message) throws Exception {

        log.info("(*) ListMethods instance {} onReceive {}", instance, message);

        if (message instanceof WorkDoneMessage) {
            WorkDoneMessage w = (WorkDoneMessage) message;
            childCounter.decrementAndGet();
            log.info("(*) ListMethods instance {} received workdone from FetchWsdl {}, "
                    + "start work came from ListMethods {}, still {} child tasks running",
                    instance, w.getChildId(), w.getParentId(), childCounter.get());

        } else if (message instanceof StartWorkingMessage) {
            startWorking++;
            if (startWorking > 1) {
                log.info("only starting one work per instance for simplicity");
                return;
            }


            log.info("(*) processing start working from {}", ((StartWorkingMessage)message).getParentId());
            for (int i = 0; i < 10; i++) {
                StartWorkingMessage start = new StartWorkingMessage(instance);
                fetchWsdlPoolRef.tell(start, getSelf());
                childCounter.addAndGet(1);
            }
            Thread.sleep(1000);
            allSent = true;

        } else if (message instanceof Terminated) {
            throw new RuntimeException("Terminated: " + message);
        } else {
            log.error("(*) Unable to handle message {}", message);
        }


    }

}
