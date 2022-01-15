package org.chaosdragon.stegovideo.params;

public class InputOutputOptions {

    private String containerPath;
    private String stegoconainerPath;
    private String payloadPath;
    private String originalPayloadPath;
    private String outputPath;
    private int watermarkWidth;
    private int watermarkHeight;

    public String getContainerPath() {
        return containerPath;
    }

    public void setContainerPath(String containerPath) {
        this.containerPath = containerPath;
    }

    public String getStegoconainerPath() {
        return stegoconainerPath;
    }

    public void setStegoconainerPath(String stegoconainerPath) {
        this.stegoconainerPath = stegoconainerPath;
    }

    public String getPayloadPath() {
        return payloadPath;
    }

    public void setPayloadPath(String payloadPath) {
        this.payloadPath = payloadPath;
    }

    public String getOriginalPayloadPath() {
        return originalPayloadPath;
    }

    public void setOriginalPayloadPath(String originalPayloadPath) {
        this.originalPayloadPath = originalPayloadPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public int getWatermarkWidth() {
        return watermarkWidth;
    }

    public void setWatermarkWidth(int watermarkWidth) {
        this.watermarkWidth = watermarkWidth;
    }

    public int getWatermarkHeight() {
        return watermarkHeight;
    }

    public void setWatermarkHeight(int watermarkHeight) {
        this.watermarkHeight = watermarkHeight;
    }
}
