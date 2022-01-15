package org.chaosdragon.stegovideo.writers;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import org.chaosdragon.stegovideo.BWBitmap.BMWAverager;
import org.chaosdragon.stegovideo.BWBitmap.BWBitmap;
import org.chaosdragon.stegovideo.BWBitmap.WatermarkComparator;
import org.chaosdragon.stegovideo.algorithms.EmbeddingAlgorithm;
import org.chaosdragon.stegovideo.encoders.BWBitmapEncoderFactory;
import org.chaosdragon.stegovideo.encoders.EncoderFactory;
import org.chaosdragon.stegovideo.tasks.ProgressCallback;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static java.io.File.separatorChar;

/**
 * @author David Griberman
 */
public class KeyFrameExtractor {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KeyFrameExtractor.class);

    private KeyFrameExtractor() {
        // Utility class
    }

    private static int mVideoStreamIndex = -1;
    private static String outputPath;

    private static BMWAverager averager;
    private static WatermarkComparator watermarkComparator;

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_AREA_AVERAGING);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    public static void generateScreenShots(String inputFilename,
                                           EmbeddingAlgorithm dtc, String targetFile, String individualWatermarkPath,
                                           ProgressCallback callback, boolean extractPerFrameWatermarks,
                                           boolean compareWatermarks, EncoderFactory factory) {
        watermarkComparator = new WatermarkComparator();

        if (factory instanceof BWBitmapEncoderFactory) {
            averager = new BMWAverager();
        }

        if (extractPerFrameWatermarks) {
            File dir = new File(individualWatermarkPath);
            for (File file : dir.listFiles()) {
                file.delete();
            }
            outputPath = individualWatermarkPath.endsWith("" + separatorChar)
                    ? individualWatermarkPath
                    : individualWatermarkPath + separatorChar;
        }

        callback.setMaxProgress(getSize(inputFilename)); // Open the file and calculate frame number

        IMediaReader mediaReader = ToolFactory.makeReader(inputFilename);
        mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

        ImageSnapListener imgListener = new ImageSnapListener(dtc, extractPerFrameWatermarks, compareWatermarks, factory, callback);

        mediaReader.addListener(imgListener);

        while (mediaReader.readPacket() == null) ;

        mediaReader.close();

        if (factory instanceof BWBitmapEncoderFactory) {
            BWBitmap b = averager.getAverage();
            ImageSnapListener.dumpWatermarkToFile(b.toBufferedImage(), targetFile);

            // Finish the progress bar before statistics
            callback.complete();

            if (compareWatermarks) {
                ImageSnapListener.printWatermarkStatistics();
                double finalWatermarkDifference = new WatermarkComparator().compare(
                        ((BWBitmapEncoderFactory) factory).getMark().toBufferedImage(), b.toBufferedImage());
                log.info("Final difference: {}", String.format("%.4f", finalWatermarkDifference));
            } else {
                log.info("Watermark extracted to {}", targetFile);
            }
        }

        mediaReader.close();
    }

    private static long getSize(String inputFilename) {
        IContainer container = IContainer.make();
        container.open(inputFilename, IContainer.Type.READ, null);
        IStream stream = null;
        for (int i = 0; i < container.getNumStreams(); i++) {
            stream = container.getStream(i);
            if (stream.getStreamCoder().getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                break;
            }
        }
        IStreamCoder coder = stream.getStreamCoder();
        long result = (long) (coder.getFrameRate().multiply(
                IRational.make(container.getDuration() / 1000 / 1000)).getDouble());

        coder.close();
        container.close();
        return result;
    }

    private static class ImageSnapListener extends MediaListenerAdapter {

        EmbeddingAlgorithm dtc;
        boolean extract;
        boolean compare;
        EncoderFactory factory;
        ProgressCallback callback;
        boolean sizeSet = false;

        public ImageSnapListener(EmbeddingAlgorithm dtc, boolean extract, boolean compare,
                                 EncoderFactory factory, ProgressCallback callback) {
            this.dtc = dtc;
            this.extract = extract;
            this.factory = factory;
            this.callback = callback;
            this.compare = compare;
        }

        @Override
        public void onVideoPicture(IVideoPictureEvent event) {

            if (event.getStreamIndex() != mVideoStreamIndex) {
                // if the selected video stream id is not yet set, go ahead an
                // select this lucky video stream
                if (mVideoStreamIndex == -1) {
                    mVideoStreamIndex = event.getStreamIndex();
                } // no need to show frames from this video stream
                else {
                    return;
                }
            }

            BufferedImage img = event.getImage();

            //Set only once
            if (!sizeSet) {
                int width = img.getWidth();
                int height = img.getHeight();
                factory.setMaxEmbeddableSize(dtc.calculateMaxSize(width, height));
                sizeSet = true;
            }

            OutputStream stream = factory.makeOutputStream();
            dtc.setOutputStream(stream);

            try {
                dtc.extract(img);
            } catch (IOException ex) {
                log.error("An unknown critical error has occurred!", ex);
                System.exit(1);
            }

            //IF BITMAP ENCODER
            if (factory instanceof BWBitmapEncoderFactory) {
                BWBitmap b = ((BWBitmapEncoderFactory) factory).getComplete();
                if (extract) {
                    dumpPerFrameWatermarkToFile(b.toBufferedImage());
                }

                averager.add(b);

                if (compare) {
                    watermarkComparator.compare(
                            ((BWBitmapEncoderFactory) factory).getMark().toBufferedImage(), b.toBufferedImage());
                }

                callback.incProgress();
            }
        }

        protected static void printWatermarkStatistics() {
            log.info("Average difference: {}", String.format("%.4f", watermarkComparator.getAverageDifference()));
            log.info("Max difference: {}", String.format("%.4f", watermarkComparator.getMaxDifference()));
            log.info("Min difference: {}", String.format("%.4f", watermarkComparator.getMinDifference()));
        }

        private static String dumpWatermarkToFile(BufferedImage image, String outputFilename) {
            try {
                ImageIO.write(image, "png", new File(outputFilename));
                return outputFilename;
            } catch (IOException e) {
                log.error("Error during saving image to file", e);
                return null;
            }
        }

        private static String dumpPerFrameWatermarkToFile(BufferedImage image) {
            String outputFilename = outputPath + System.currentTimeMillis() + ".png";
            return dumpWatermarkToFile(image, outputFilename);
        }
    }
}
