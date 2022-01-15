package org.chaosdragon.stegovideo.params;

/**
 * Class for setting AdvancedWriter settings
 *
 * @author David Griberman 
 *
 */
public class AttackOptions {

    private boolean compression = false;
    private boolean compression2 = false;
    private boolean overlay = false; //Need bufferedimage for final
    private double resize = 1;
    private boolean flip = false;
    private boolean crop = false;

    /**
     * @return the compression
     */
    public boolean isCompression() {
        return compression;
    }

    /**
     * @return the compression2
     */
    public boolean isCompression2() {
        return compression2;
    }

    /**
     * @return the resize
     */
    public double getResize() {
        return resize;
    }

    /**
     * @return the flip
     */
    public boolean isFlip() {
        return flip;
    }

    /**
     * @return the crop
     */
    public boolean isCrop() {
        return crop;
    }

    /**
     * @param crop the crop to set
     */
    public void setCrop(boolean crop) {
        this.crop = crop;
    }

    /**
     * @param compression the compression to set
     */
    public void setCompression(boolean compression) {
        this.compression = compression;
    }

    /**
     * @param compression2 the compression2 to set
     */
    public void setCompression2(boolean compression2) {
        this.compression2 = compression2;
    }

    /**
     * @param resize the resize to set
     */
    public void setResize(double resize) {
        this.resize = resize;
    }

    /**
     * @param flip the flip to set
     */
    public void setFlip(boolean flip) {
        this.flip = flip;
    }

    /**
     * @return the overlay
     */
    public boolean isOverlay() {
        return overlay;
    }

    /**
     * @param overlay the overlay to set
     */
    public void setOverlay(boolean overlay) {
        this.overlay = overlay;
    }

}
