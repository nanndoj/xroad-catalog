package fi.vrk.xroad.catalog.collector.actors;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import fi.vrk.xroad.catalog.collector.extension.SpringExtension;
import fi.vrk.xroad.catalog.persistence.CatalogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Actor which fetches all clients, and delegates listing
 * their methods to ListMethodsActors
 */
@Component
@Scope("prototype")
@Slf4j
public class ListClientsActor extends UntypedActor {

    private boolean allSent = false;
    public static final String START_COLLECTING = "StartCollecting";
    private static AtomicInteger INSTANCE_COUNTER = new AtomicInteger(0);
    private int instance;

    public ListClientsActor() {
        instance = INSTANCE_COUNTER.addAndGet(1);
        log.info("ListClientsActor instance {} created", instance);
    }

    private static AtomicInteger COUNTER = new AtomicInteger(0);
    // to test fault handling
    private static boolean FORCE_FAILURES = false;

    private WorkBatchCounter workBatchCounter = new WorkBatchCounter();

    @Autowired
    @Qualifier("listClientsRestOperations")
    private RestOperations restOperations;

    @Autowired
    private SpringExtension springExtension;

    @Autowired
    protected CatalogService catalogService;


    @Value("${xroad-catalog.list-clients-host}")
    private String host;


    // supervisor-created pool of list methods actors
    protected ActorRef listMethodsPoolRef;


    @Override
    public void preStart() throws Exception {
        log.info("preStart {}", this.hashCode());
        listMethodsPoolRef = new RelativeActorRefUtil(getContext())
                .resolvePoolRef(Supervisor.LIST_METHODS_ACTOR_ROUTER);
        super.preStart();
    }

    @Override
    public void postStop() throws Exception {
        log.info("postStop {}", this.hashCode());
        super.postStop();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("ListClientsActor instance {} onReceive {}", instance, message);

        if (START_COLLECTING.equals(message)) {

            for (int i = 0; i < 10; i++) {
                NewStartWorkingMessage start = new NewStartWorkingMessage();
                start.setListClientsActor(getSelf());
                start.setListClientsBatch(i);
                start.setListClientsInstance(instance);
                workBatchCounter.startTracking(start);
                listMethodsPoolRef.tell(start, getSelf());
            }

            allSent = true;
            log.info("all messages sent to actor");
        } else if (message instanceof NewWorkDoneMessage) {
            NewWorkDoneMessage done = (NewWorkDoneMessage) message;
            if (done.getListClientsInstance() != instance) {
                IllegalStateException e = new IllegalStateException("wrong instance id: " + done.getListClientsInstance() + ", should be: " + instance);
                log.error(e.toString());
                throw e;
            }

            log.info("ListClientsActor instance {} received message {}", instance, message);
            workBatchCounter.markWorkDone(done);
            workBatchCounter.logStatus();
            if (allSent && workBatchCounter.allDone()) {
                // all work is done, success
                throw new IllegalStateException("READY!");
            }


        } else if (message instanceof Terminated) {
            throw new RuntimeException("Terminated: " + message);
        } else {
            log.error("Unable to handle message {}", message);
        }
    }
}
