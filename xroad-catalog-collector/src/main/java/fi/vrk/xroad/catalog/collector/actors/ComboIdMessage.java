package fi.vrk.xroad.catalog.collector.actors;

import akka.actor.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * Created by janne on 5.4.2016.
 */
@Getter
@Setter
public class ComboIdMessage implements Serializable {
    private int listClientsBatch;
    private int listMethodsBatch;
    private int fetchWsdlBatch;

    private int listClientsInstance;
    private int listMethodsInstance;
    private int fetchWsdlInstance;

    private ActorRef listClientsActor;
    private ActorRef listMethodsActor;

    public ComboIdMessage() {
    }

    public void copyFieldsFrom(ComboIdMessage other) {
        BeanUtils.copyProperties(other, this);
    }
}
