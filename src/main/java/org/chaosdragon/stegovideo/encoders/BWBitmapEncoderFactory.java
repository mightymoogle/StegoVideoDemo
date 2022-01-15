package org.chaosdragon.stegovideo.encoders;

import org.chaosdragon.stegovideo.BWBitmap.BWBItmapStream;
import org.chaosdragon.stegovideo.BWBitmap.BWBitmap;
import org.chaosdragon.stegovideo.BWBitmap.BWBitmapEncoder;
import org.chaosdragon.stegovideo.BWBitmap.BWBitmapStreamWriter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A class with settings for BWBitmap embedding and extraction process
 * @author David Griberman
 */
public class BWBitmapEncoderFactory implements EncoderFactory {

    private final BWBitmap mark; //Input watermark
    private BWBitmap out;  //Output watermark
    private long key = -1; //The key for scrambling

    //WATERMARK SIZE
    private int sizeX;
    private int sizeY;

    //MAXIMUM SIZE TO EMBED (NEED only 1)
    private int maxX;
    private int maxY;

    private int blockSize; //DTC block size
    private boolean fillNoise = true; //Fill empty with noise
    private boolean copies = true; //Embed multiple copies

    /**
     * 
     * @return the input watermark
     */
    public BWBitmap getMark() {
        return mark;
    }

    /**
     * Creates the factory with a watermark provided
     * @param a  the input watermark
     */
    public BWBitmapEncoderFactory(BWBitmap a) {
        mark = a;
    }

    /**
     * Creates the factory with a watermark from a file path
     * @param s path to the input watermark
     * @throws IOException 
     */
    public BWBitmapEncoderFactory(String s) throws IOException {
        mark = new BWBitmap(ImageIO.read(new File(s)));
    }

    /**
     * Sets the key
     * @param k key
     * @return
     */
    @Override
    public BWBitmapEncoderFactory setKey(long k) {
        key = k;
        return this;
    }

    /**
     * Sets the watermark size
     * @param x width
     * @param y height
     * @return 
     */
    @Override
    public EncoderFactory setWatermarkSize(int x, int y) {
        sizeX = x;
        sizeY = y;
        return this;
    }

    /**
     * Sets the max embeddable size
     * @param size
     */
    @Override
    public void setMaxEmbeddableSize(int size) {
        this.maxX = size;
        this.maxY = 1;
    }

    /**
     * Enables or disables embedding of multiple copies of the watermark
     * @param t
     * @return 
     */
    @Override
    public EncoderFactory setMultipleCopies(boolean t) {
        copies = t;
        return this;
    }
    
    /**
     * Creates the encoder based on settings specified in the object
     * @return a new encoder
     */    
    @Override
    public MessageEncoder makeEncoder() {

        MessageEncoder encoder = new BWBitmapEncoder(
                new BWBItmapStream(
                        mark
                ));

        if (copies) {
            encoder = new Duplicator(encoder, (long) maxX * maxY);
        }
        
        encoder = new Obscurificator(encoder, (long) maxX * maxY, fillNoise);
        encoder = new Scrambler(encoder, key);
        return encoder;
    }

    /**
     * Creates an outputstream from settings provided
     * @return a BWBitmap outputstream
     */
    @Override
    public OutputStream makeOutputStream() {
        out = new BWBitmap(sizeX, sizeY);
        return makeOutputStream(out);
    }

    /**
     * Creates an outputstream from settings provided
     * @param b bitmap to make stream from
     * @return 
     */
    protected OutputStream makeOutputStream(BWBitmap b) {
        OutputStream stream = new BWBitmapStreamWriter(b);
        
        int obsSize = maxX * maxY; //???
        
        if (copies) {
        stream = new DeDuplicator(stream, maxX * maxY, sizeX * sizeY);
        obsSize = DeDuplicator.getSize(maxX * maxY, sizeX * sizeY);
        }
        
        stream = new Deobscurificator(stream, obsSize);
        stream = new Descrambler(stream, key, maxX * maxY); //MAX SIZE HERE
        
        
        return stream;
    }

    /**
     * 
     * @return output watermark
     */
    public BWBitmap getComplete() {
        return out;
    }

    /**
     *
     * @return  size of DTC block
     */
    @Override
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * 
     * @param size DTC block size
     * @return 
     */
    @Override
    public EncoderFactory setBlockSize(int size) {
        blockSize = size;
        return this;
    }

    /**
     * Specifies whether to fill empty spots with noise
     * @param a 
     * @return 
     */
    @Override
    public EncoderFactory setFillNoise(boolean a) {
        fillNoise = a;
        return this;
    }

}
