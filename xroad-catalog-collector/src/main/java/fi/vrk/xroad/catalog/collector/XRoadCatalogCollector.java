package fi.vrk.xroad.catalog.collector;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.TypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import fi.vrk.xroad.catalog.collector.actors.Supervisor;
import fi.vrk.xroad.catalog.collector.extension.SpringExtension;
import fi.vrk.xroad.catalog.collector.util.XRoadCatalogID;
import fi.vrk.xroad.catalog.collector.util.XRoadCatalogMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Main collector application.
 * Initiates a supervisor
 */
@Slf4j
@Configuration
@EnableAutoConfiguration
@ComponentScan("fi.vrk.xroad.catalog.collector.configuration")
public class XRoadCatalogCollector  {
    public static final String DONE = "Done";

    public static void main(String[] args) throws Exception {

        ApplicationContext context =
            SpringApplication.run(XRoadCatalogCollector.class, args);

        ActorSystem system = context.getBean(ActorSystem.class);

        final LoggingAdapter log = Logging.getLogger(system, "Application");

        log.info("Starting up");


        SpringExtension ext = context.getBean(SpringExtension.class);

        // Use the Spring Extension to create props for a named actor bean
        ActorRef supervisor = system.actorOf(
                ext.props("supervisor").withMailbox("akka.priority-mailbox"));


        final boolean START_COLLECTING = true;
        if (START_COLLECTING) {
            supervisor.tell(new XRoadCatalogMessage(new XRoadCatalogID(1, 1), Supervisor.START_COLLECTING), null);
        }



        log.info("End of main");
    }

}
