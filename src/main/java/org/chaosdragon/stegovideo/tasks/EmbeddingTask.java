package org.chaosdragon.stegovideo.tasks;

import org.chaosdragon.stegovideo.writers.MasterWriter;

/**
 * @author David Griberman
 */
public class EmbeddingTask implements Runnable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmbeddingTask.class);

    private final MasterWriter m;

    public EmbeddingTask(MasterWriter m, String message) {
        this.m = m;
        m.setProgressCallback(new ProgressBarCallback(message));
    }

    @Override
    public void run() {
        try {
            m.run();
        } catch (Exception e) {
            log.error("An error has occurred during embedding!", e);
        }
    }
}
