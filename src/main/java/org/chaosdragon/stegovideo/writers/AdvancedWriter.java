package org.chaosdragon.stegovideo.writers;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;
import org.chaosdragon.stegovideo.exceptions.EncodingException;
import org.chaosdragon.stegovideo.params.AttackOptions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class AdvancedWriter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdvancedWriter.class);

    private final IContainer outContainer;
    private final IStream outStream;
    private final IStreamCoder outStreamCoder;
    private final IRational frameRate;
    private AttackOptions settings;

    private static final IPixelFormat.Type OUTPUT_PIXEL_TYPE = IPixelFormat.Type.YUV420P;

    public AdvancedWriter(String outFile, IStreamCoder coder, AttackOptions settings) {
        this.settings = settings;
        frameRate = coder.getFrameRate();
        outContainer = IContainer.make();

        int retval = outContainer.open(outFile, IContainer.Type.WRITE, null);
        if (retval < 0) {
            throw new EncodingException("could not open output file");
        }

        ICodec codec = ICodec.findEncodingCodec(ICodec.ID.CODEC_ID_H264);

        outStream = outContainer.addNewStream(codec);
        outStreamCoder = outStream.getStreamCoder();
        outStreamCoder.setNumPicturesInGroupOfPictures(coder.getNumPicturesInGroupOfPictures());

        double compress = 2.0; //Default        
        if (settings.isCompression()) {
            compress = 1;
        }
        if (settings.isCompression2()) {
            compress = 0.5;
        }
        if (settings.isCompression() && settings.isCompression2()) {
            compress = 0.1;
        }

        outStreamCoder.setBitRate((int) (coder.getBitRate() * compress));
        outStreamCoder.setBitRateTolerance(coder.getBitRateTolerance());
        outStreamCoder.setAutomaticallyStampPacketsForStream(true);
        outStreamCoder.setPixelType(OUTPUT_PIXEL_TYPE);

        int hh = coder.getHeight();
        int ww = coder.getWidth();

        if (settings.getResize() != 1) {
            ww = (int) (ww * settings.getResize());
            hh = (int) (hh * settings.getResize());
        }

        outStreamCoder.setHeight(hh);
        outStreamCoder.setWidth(ww);
        outStreamCoder.setFlags(coder.getFlags());
        outStreamCoder.setGlobalQuality(coder.getGlobalQuality());

        outStreamCoder.setFrameRate(frameRate);

        outStreamCoder.setTimeBase(coder.getTimeBase());

        coder.close();

        retval = outStreamCoder.open(null, null);
        if (retval < 0) {
            throw new EncodingException("could not open input decoder");
        }
        retval = outContainer.writeHeader();
        if (retval < 0) {
            throw new EncodingException("could not write file header");
        }
    }

    /**
     * Encode the given image to the file and increment our time stamp.
     *
     * @param originalImage an image of the screen.
     */
    public void encodeImage(BufferedImage originalImage, long timeStamp) {
        encodeImage(originalImage, timeStamp, false);
    }

    public void encodeImage(BufferedImage originalImage, long timeStamp, boolean nil) throws UnsupportedOperationException {

        if (nil) {
            //Finalization to lessen xuggler bug with last frames
            for (int i = 0; i < 100; i++) {
                IPacket packet = IPacket.make();
                outStreamCoder.encodeVideo(packet, null, 0);
                if (packet.isComplete()) {
                    outContainer.writePacket(packet);
                }
            }

            return;
        }

        //Returns a new one, does not appe
        if (settings.isFlip()) {
            AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
            tx.translate(-originalImage.getWidth(null), 0);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            originalImage = op.filter(originalImage, null);
        }

        if (settings.isCrop()) {
            //2:39/1
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            int newHeight = (int) (width / 2.4);

            int heightChange = originalImage.getHeight() - newHeight;

            Graphics g = originalImage.getGraphics();
            g.setColor(Color.black);
            g.fillRect(0, 0, width, heightChange);
            g.fillRect(0, height - heightChange, width, height);

        }

        if (settings.getResize() != 1) {
            int ww = originalImage.getWidth();
            int hh = originalImage.getHeight();
            //Resize image
            ww = (int) (ww * settings.getResize());
            hh = (int) (hh * settings.getResize());

            originalImage = KeyFrameExtractor.resize(originalImage, ww, hh);
        }

        //Overlay RTU logo
        if (settings.isOverlay()) {
            try {
                BufferedImage overlay = ImageIO.read(getClass().getResourceAsStream("/rtu_overlay.png"));

                originalImage.getGraphics().drawImage(
                        overlay, originalImage.getWidth() / 2
                                - overlay.getWidth() / 2, originalImage.getHeight() / 2 - overlay.getHeight() / 2,
                        null);

            } catch (Exception e) {
                log.error("Could not draw overlay watermark image", e);
            }

        }

        BufferedImage worksWithXugglerBufferedImage = convertToType(originalImage,
                BufferedImage.TYPE_3BYTE_BGR);
        IPacket packet = IPacket.make();

        IConverter converter = ConverterFactory.createConverter(
                worksWithXugglerBufferedImage, OUTPUT_PIXEL_TYPE);

        IVideoPicture outFrame = converter.toPicture(worksWithXugglerBufferedImage,
                timeStamp);
        outFrame.setQuality(0);
        int retval = outStreamCoder.encodeVideo(packet, outFrame, 0);
        if (retval < 0) {
            throw new EncodingException("could not encode video");
        }
        if (packet.isComplete()) {
            retval = outContainer.writePacket(packet);
            if (retval < 0) {
                throw new EncodingException("could not save packet to container");
            }
        }
    }

    /**
     * Close out the file we're currently working on.
     */
    public void closeStreams() {
        encodeImage(null, 0, true);
        int retval = outContainer.writeTrailer();
        if (retval < 0) {
            throw new EncodingException("Could not write trailer to output file");
        }

        outStreamCoder.close();
        outContainer.close();
    }

    /**
     * Convert a {@link BufferedImage} of any type, to {@link BufferedImage} of
     * a specified type. If the source image is the same type as the target
     * type, then original image is returned, otherwise new image of the correct
     * type is created and the content of the source image is copied into the
     * new image.
     *
     * @param sourceImage the image to be converted
     * @param targetType  the desired BufferedImage type
     * @return a BufferedImage of the specifed target type.
     * @see BufferedImage
     */
    public static BufferedImage convertToType(BufferedImage sourceImage,
                                              int targetType) {
        BufferedImage image;
        // if the source image is already the target type, return the source image
        if (sourceImage.getType() == targetType) {
            image = sourceImage;
        } else {
            // otherwise create a new image of the target type and draw the new image
            image = new BufferedImage(sourceImage.getWidth(),
                    sourceImage.getHeight(), targetType);
            image.getGraphics().drawImage(sourceImage, 0, 0, null);
        }
        return image;
    }
}
