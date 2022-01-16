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
import com.xuggle.xuggler.IVideoPicture;
import org.chaosdragon.stegovideo.algorithms.DubaiAlgorithm;
import org.chaosdragon.stegovideo.algorithms.EmbeddingAlgorithm;
import org.chaosdragon.stegovideo.algorithms.EmbeddingAlgorithmType;
import org.chaosdragon.stegovideo.algorithms.HaarDWTAlgorithm;
import org.chaosdragon.stegovideo.algorithms.KaurAlgorithm;
import org.chaosdragon.stegovideo.algorithms.KothariAlgorithm;
import org.chaosdragon.stegovideo.algorithms.NullAlgorithm;
import org.chaosdragon.stegovideo.encoders.EncoderFactory;
import org.chaosdragon.stegovideo.encoders.MessageEncoder;
import org.chaosdragon.stegovideo.params.AttackOptions;
import org.chaosdragon.stegovideo.tasks.ProgressCallback;
import org.chaosdragon.stegovideo.tools.AdaptiveBox;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class MasterWriter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MasterWriter.class);

    //Video stream starting index
    private int mVideoStreamIndex = -1;
    //For screen generation
    private final String outputFilePrefix;
    //Input-output files
    private final String inputFilename;
    private final String outputFilename;
    private EmbeddingAlgorithm dtc;
    private ProgressCallback progressCallback;
    private MessageEncoder encoder;
    private final EncoderFactory factory;
    private boolean adaptiveUpgrade = false;
    private AttackOptions attackOptions = new AttackOptions();

    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    //For extraction only
    public MasterWriter(String in, String prefix, EmbeddingAlgorithmType e, int strength, int block, int compress, EncoderFactory factory) throws IOException {
        this("", in, prefix, factory, e, strength, block, compress);
    }

    public MasterWriter(String in, String out, String prefix, EncoderFactory factory,
                        EmbeddingAlgorithmType e, int strength, int block, int compress) {

        inputFilename = in;
        outputFilename = out;
        outputFilePrefix = prefix;
        this.factory = factory;

        dtc = null;

        if (e == EmbeddingAlgorithmType.KAUR_ALGORITHM) {
            dtc = new KaurAlgorithm(block, compress, strength);
        }

        if (e == EmbeddingAlgorithmType.DUBAI_ALGORITHM) {
            dtc = new DubaiAlgorithm(block, compress, strength);
        }

        if (e == EmbeddingAlgorithmType.KOTHARI_ALGORITHM) {
            dtc = new KothariAlgorithm(block, compress, strength);
        }

        if (e == EmbeddingAlgorithmType.NULL_ALGORITHM) {
            dtc = new NullAlgorithm(block, compress);
        }

        if (e == EmbeddingAlgorithmType.HAAR_ALGORITHM) {
            dtc = new HaarDWTAlgorithm(strength);
        }
    }

    public void setAdaptive(boolean upgrade) {
        //If embedder does not support and selected, false
        adaptiveUpgrade = upgrade && dtc.supportsAdaptive();
    }

    public void setAttackSettings(AttackOptions attackOptions) {
        this.attackOptions = attackOptions;
    }

    public void run() {
        try {
            magic();
        } catch (Exception e) {
            log.error("Error writer in process", e);
        }
    }

    public void extract(String targetFilename, boolean e, boolean comp) {
        if (factory != null) {
            encoder = factory.makeEncoder();
        }
        if (encoder != null) {
            dtc.setEncoder(encoder);
        }
        KeyFrameExtractor.generateScreenShots(getOutputFilename(), dtc, targetFilename, outputFilePrefix,
                progressCallback, e, comp, factory);
    }

    private void magic() {
        IContainer container = IContainer.make();

        // we attempt to open up the container
        container.open(getInputFilename(), IContainer.Type.READ, null);

        // find the stream object
        //Find the video stream
        IStream stream = null;
        for (int i = 0; i < container.getNumStreams(); i++) {
            stream = container.getStream(i);
            if (stream.getStreamCoder().getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                break;
            }
        }

        // get the pre-configured decoder that can decode this stream;
        IStreamCoder coder = stream.getStreamCoder();

        log.info("*** Start of Stream Info ***");
        log.info("type: {}; ", coder.getCodecType());
        log.info("codec: {}; ", coder.getCodecID());

        long size = (long) (coder.getFrameRate().multiply(
                IRational.make(container.getDuration() / 1000 / 1000)
        ).getDouble());

        progressCallback.setMaxProgress(size);

        if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
            log.info("width: {}; ", coder.getWidth());
            log.info("height: {}; ", coder.getHeight());
            log.info("format: {}; ", coder.getPixelType());
            log.info("frame-rate: {}; ", String.format("%5.2f", coder.getFrameRate().getDouble()));
        }

        log.info("*** End of Stream Info ***");

        /////////////////////////
        IMediaReader mediaReader = ToolFactory.makeReader(getInputFilename());
        mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

        //Add finding of size here
        int width = coder.getWidth();
        int height = coder.getHeight();

        factory.setMaxEmbeddableSize(dtc.calculateMaxSize(width, height));
        encoder = factory.makeEncoder();

        if (encoder != null) {
            dtc.setEncoder(encoder);
        }

        ImageSnapListener snapper = new ImageSnapListener(coder, dtc, progressCallback, adaptiveUpgrade, attackOptions);
        mediaReader.addListener(snapper);

        while (mediaReader.readPacket() == null) ;
        progressCallback.complete();
        snapper.finish();

        stream.getStreamCoder().close();
        stream.delete();
        container.close();
        container.delete();

        snapper.close();
        mediaReader.close();

        encoder = null;
    }

    /**
     * @return the outputFilePrefix
     */
    public String getOutputFilePrefix() {
        return outputFilePrefix;
    }

    /**
     * @return the inputFilename
     */
    public String getInputFilename() {
        return inputFilename;
    }

    /**
     * @return the outputFilename
     */
    public String getOutputFilename() {
        return outputFilename;
    }

    private class ImageSnapListener extends MediaListenerAdapter {

        private final EmbeddingAlgorithm dtc;
        private final AdvancedWriter writer;
        private ProgressCallback callback;
        private boolean adaptiveUpgrade;
        private AdaptiveBox box;

        public void close() {
            writer.closeStreams();
            box = null;
        }

        public ImageSnapListener(IStreamCoder coder, EmbeddingAlgorithm dtc, ProgressCallback g,
                                 boolean upgrade, AttackOptions settings) {
            this(coder, dtc, settings);
            callback = g;
            adaptiveUpgrade = upgrade;
            box = null;
        }

        public ImageSnapListener(IStreamCoder coder, EmbeddingAlgorithm dtc, AttackOptions settings) {
            this.dtc = dtc;

            // let's make a IMediaWriter to write the file.
            writer = new AdvancedWriter(getOutputFilename(), coder, settings);
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

            IVideoPicture p = event.getPicture();

            BufferedImage img = event.getImage();

            if (!adaptiveUpgrade) {
                dtc.reset();

                try {
                    img = dtc.embed(img);
                } catch (IOException ex) {
                    log.error("An unknown critical error has occurred!", ex);
                    System.exit(1);
                }

                callback.incProgress();
                writer.encodeImage(img, p.getTimeStamp());
            } else {
                adaptiveWrite(false, img, p);
            }
        }

        public void finish() {
            if (adaptiveUpgrade)
                adaptiveWrite(true, null, null);
        }

        private void adaptiveWrite(boolean last, BufferedImage img, IVideoPicture p) {

            if (!last) {
                //Advanced mode
                if (box == null) {
                    // TODO: yCbCr should probably be set based on the image, not always true. Keep as is for now.
                    box = new AdaptiveBox(img, p.getTimeStamp(), true);
                } else {
                    box.add(img, p.getTimeStamp());
                }
            }

            if (box.isFull() || last) {

                int m = 1;
                if (last) {
                    m = AdaptiveBox.FRAME_BUFFER_SIZE - 2;
                }

                for (int i = 0; i < m; i++) {
                    try {
                        dtc.reset();
                        img = box.getNext();
                        long stamp = box.getNextStamp();


                        box.disableChannels();
                        box.enableChannel(0);

                        int[][] n = box.calculateStrengthMatrix(!last);

                        dtc.setStrengthMatrix(n, box);

                        img = dtc.embed(img);
                        callback.incProgress();
                        writer.encodeImage(img, stamp);
                    } catch (Exception ex) {
                        log.error("An unknown critical error has occurred!", ex);
                        System.exit(1);
                    }
                }
            }
        }
    }
}
