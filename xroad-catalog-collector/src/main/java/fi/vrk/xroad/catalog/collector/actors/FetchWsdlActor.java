package fi.vrk.xroad.catalog.collector.actors;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import fi.vrk.xroad.catalog.collector.wsimport.XRoadServiceIdentifierType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Actor which fetches one wsdl
 */
@Component
@Scope("prototype")
@Slf4j
public class FetchWsdlActor extends UntypedActor {

    private static AtomicInteger WORK_DONE_COUNTER = new AtomicInteger(1);
    private static AtomicInteger INSTANCE_COUNTER = new AtomicInteger(0);
    private int instance;

    public FetchWsdlActor() {
        instance = INSTANCE_COUNTER.addAndGet(1);
        log.info("(---) FetchWsdlActor instance {} created", instance);
    }


    @Override
    public void onReceive(Object message) throws Exception {
        log.info("(---) FetchWsdlActor instance {} onReceive {}", instance, message);

        if (message instanceof StartWorkingMessage) {

            StartWorkingMessage myStartCommand = (StartWorkingMessage) message;
            log.info("(*) processing start working from {}", myStartCommand.getListClientsInstance());
            Thread.sleep(1000);
            // reply with "work done"
            ActorRef listMethodsActor = myStartCommand.getListMethodsActor();
            WorkDoneMessage doneMessageFromListMethods = new WorkDoneMessage();
            doneMessageFromListMethods.copyFieldsFrom(myStartCommand);
            doneMessageFromListMethods.setFetchWsdlInstance(instance);

            log.info("WSDLfetching number {} finished, sending 'done' signal upstream", WORK_DONE_COUNTER.getAndAdd(1));

            listMethodsActor.tell(doneMessageFromListMethods, getSelf());


        } else if (message instanceof XRoadServiceIdentifierType) {
        } else if (message instanceof Terminated) {
            throw new RuntimeException("Terminated: " + message);
        } else {
            log.error("Unable to handle message {}", message);
        }
    }

}
