package fi.vrk.xroad.catalog.collector.actors;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import com.google.common.base.Strings;
import fi.vrk.xroad.catalog.collector.wsimport.ClientType;
import fi.vrk.xroad.catalog.collector.wsimport.XRoadServiceIdentifierType;
import fi.vrk.xroad.catalog.persistence.CatalogService;
import fi.vrk.xroad.catalog.persistence.entity.ServiceId;
import fi.vrk.xroad.catalog.persistence.entity.SubsystemId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Actor which fetches one wsdl
 */
@Component
@Scope("prototype")
@Slf4j
public class FetchWsdlActor extends UntypedActor {

    private static AtomicInteger INSTANCE_COUNTER = new AtomicInteger(0);
    private int instance;

    public FetchWsdlActor() {
        instance = INSTANCE_COUNTER.addAndGet(1);
        log.info("(---) FetchWsdlActor instance {} created", instance);
    }


    @Override
    public void onReceive(Object message) throws Exception {
        log.info("(---) FetchWsdlActor instance {} onReceive {}", instance, message);

        if (message instanceof NewStartWorkingMessage) {

            NewStartWorkingMessage myStartCommand = (NewStartWorkingMessage) message;
            log.info("(*) processing start working from {}", myStartCommand.getListClientsInstance());
            Thread.sleep(1000);
            // reply with "work done"
            ActorRef listMethodsActor = myStartCommand.getListMethodsActor();
            NewWorkDoneMessage doneMessageFromListMethods = new NewWorkDoneMessage();
            doneMessageFromListMethods.copyFieldsFrom(myStartCommand);
            doneMessageFromListMethods.setFetchWsdlInstance(instance);
            listMethodsActor.tell(doneMessageFromListMethods, getSelf());


        } else if (message instanceof XRoadServiceIdentifierType) {
        } else if (message instanceof Terminated) {
            throw new RuntimeException("Terminated: " + message);
        } else {
            log.error("Unable to handle message {}", message);
        }
    }

}
