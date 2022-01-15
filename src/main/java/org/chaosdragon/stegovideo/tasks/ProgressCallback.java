package org.chaosdragon.stegovideo.tasks;

public interface ProgressCallback {

    void setMaxProgress(long max);

    void incProgress();

    void setMessage(String s);

    void complete();
}
