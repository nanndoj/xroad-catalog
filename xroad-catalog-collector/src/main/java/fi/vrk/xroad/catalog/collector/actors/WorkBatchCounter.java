package fi.vrk.xroad.catalog.collector.actors;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Counts work items in progress for different batches
 */
@Slf4j
public class WorkBatchCounter {

    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    private class BatchKey {
        private Integer listClientsBatch;
        private Integer listMethodsBatch;
        private Integer fetchWsdlBatch;
    }

    // bit silly currently, tracking each work item by putting Integer "1" into a map...
    private Map<BatchKey, Integer> counters = new HashMap<>();

    public synchronized void startTracking(StartWorkingMessage startMessage) {
        BatchKey key = createMapKey(startMessage);
        Integer old = counters.get(key);
        if (old != null) {
            throw new IllegalStateException("already tracking " + startMessage);
        }
        counters.put(key, new Integer(1));
    }

    /**
     * keys are listclientsbatch.listmethodsbatch.fetchwsdlbatch
     * @param message
     * @return
     */
    private BatchKey createMapKey(ComboIdMessage message) {
        assert(message.getListClientsBatch() != null);
        BatchKey key = new BatchKey();
        key.setListClientsBatch(message.getListClientsBatch());
        key.setListMethodsBatch(message.getListMethodsBatch());
        key.setFetchWsdlBatch(message.getFetchWsdlBatch());
        return key;
//        StringBuffer key = new StringBuffer();
//        key.append(message.getListClientsBatch());
//        if (message.getListMethodsBatch() != -1) {
//            key.append(".");
//            key.append(message.getListMethodsBatch());
//            if (message.getFetchWsdlBatch() != -1) {
//                key.append(".");
//                key.append(message.getFetchWsdlBatch());
//            }
//        }
    }

    public synchronized void markWorkDone(WorkDoneMessage doneMessage) {
        BatchKey key = createMapKey(doneMessage);
        Integer workers = counters.get(key);
        if (workers == null) {
            throw new IllegalStateException("not working on " + doneMessage);
        }
        counters.remove(key);
    }

    public synchronized void logStatus() {
        log.info("working on items: " + counters);
    }

    public synchronized boolean allDone() {
        return counters.isEmpty();
    }

    public boolean allDone(Integer listClientsBatch) {
        for (BatchKey key: counters.keySet()) {
            if (listClientsBatch.equals(key.getListClientsBatch())) {
                return false;
            }
        }
        return true;
    }

//    public boolean allDone(int listClientsBatch, int listMethodsBatch) {
//        for (BatchKey key: counters.keySet()) {
//            if (key.getListClientsBatch() == listClientsBatch
//                    && key.getListMethodsBatch() == listMethodsBatch) {
//                return false;
//            }
//        }
//        return true;
//    }

}
