package fi.vrk.xroad.catalog.collector.actors;

import akka.actor.ActorRef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * Created by janne on 5.4.2016.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class ComboIdMessage implements Serializable {
    private Integer listClientsBatch;
    private Integer listMethodsBatch;
    private Integer fetchWsdlBatch;

    private Integer listClientsInstance;
    private Integer listMethodsInstance;
    private Integer fetchWsdlInstance;

    private ActorRef listClientsActor;
    private ActorRef listMethodsActor;

    public ComboIdMessage() {
    }

    public void copyFieldsFrom(ComboIdMessage other) {
        BeanUtils.copyProperties(other, this);
    }
}
