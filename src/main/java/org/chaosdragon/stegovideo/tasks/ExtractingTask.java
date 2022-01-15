package org.chaosdragon.stegovideo.tasks;

import org.chaosdragon.stegovideo.writers.MasterWriter;

/**
 * @author David Griberman
 */
public class ExtractingTask implements Runnable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExtractingTask.class);

    private final MasterWriter m;
    private final String targetFilename;
    private final boolean extractPerFrameWatermarks;
    private final boolean compareWatermarks;

    public ExtractingTask(MasterWriter m, String targetFilename, boolean extractPerFrameWatermarks,
                          boolean compareWatermarks, String message) {
        this.m = m;
        this.targetFilename = targetFilename;
        this.extractPerFrameWatermarks = extractPerFrameWatermarks;
        this.compareWatermarks = compareWatermarks;
        m.setProgressCallback(new ProgressBarCallback(message));
    }

    @Override
    public void run() {
        try {
            m.extract(targetFilename, extractPerFrameWatermarks, compareWatermarks);
        } catch (Exception e) {
            log.error("An error has occurred during extraction!", e);
        }
    }
}
