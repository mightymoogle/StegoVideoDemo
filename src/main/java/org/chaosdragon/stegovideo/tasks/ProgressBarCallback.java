package org.chaosdragon.stegovideo.tasks;

import me.tongfei.progressbar.ProgressBar;

/**
 *
 * @author David Griberman
 */
public class ProgressBarCallback implements ProgressCallback {

    private final ProgressBar progressBar;

    public ProgressBarCallback(String name) {
        progressBar = new ProgressBar(name, 100);
    }
    
    @Override
    public void setMaxProgress(long max) {
        progressBar.maxHint(max);
    }
    
    @Override
    public void incProgress() {
        progressBar.step();
    }
    
    @Override
    public void setMessage(String s) {
        progressBar.setExtraMessage(s);
    }

    @Override
    public void complete() {
        progressBar.stepTo(progressBar.getMax());
        progressBar.close();
    }
}
