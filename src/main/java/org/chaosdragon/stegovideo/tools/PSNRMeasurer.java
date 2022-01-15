package org.chaosdragon.stegovideo.tools;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import org.chaosdragon.stegovideo.tasks.ProgressCallback;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Class for measuring PSNR values
 *
 * @author David Griberman
 */
public class PSNRMeasurer {

    private int mVideoStreamIndex = -1;
    private BufferedImage a;
    private BufferedImage b;

    public double getPSNR(String original, String edited, ProgressCallback progressCallback) {

        ArrayList<Double> psnrs = new ArrayList<>();
        IMediaReader mediaReader1 = ToolFactory.makeReader(original);
        IMediaReader mediaReader2 = ToolFactory.makeReader(edited);
        mediaReader1.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
        mediaReader2.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

        ImageSnapListener imgListener1 = new ImageSnapListener(progressCallback, 0);
        ImageSnapListener imgListener2 = new ImageSnapListener(progressCallback, 1);
        mediaReader1.addListener(imgListener1);
        mediaReader2.addListener(imgListener2);

        while (mediaReader1.readPacket() == null) {

            if (a != null) {
                while (b == null && mediaReader2.readPacket() == null);

                if (b != null) {
                    double psnr = PSNR.measurePSNR(a, b);

                    if (!Double.isNaN(psnr) && psnr != 0) {
                        psnrs.add(psnr);
                    }
                    a = null;
                    b = null;
                }
            }
        }

        mediaReader1.close();
        mediaReader2.close();

        //Can probably overflow with huge sizes, need running sum?
        double sum = 0;
        for (double p : psnrs) {
            sum += p;
        }

        if (psnrs.isEmpty()) {
            return 0; //If same
        }
        return sum / psnrs.size();
    }

    private class ImageSnapListener extends MediaListenerAdapter {

        int num;
        ProgressCallback callback;

        public ImageSnapListener(ProgressCallback callback, int num) {
            this.num = num;
            this.callback = callback;
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

            if (num == 0) {
                a = img;
            }
            if (num == 1) {
                b = img;
            }
        }
    }
}
