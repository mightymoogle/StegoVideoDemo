package org.chaosdragon.stegovideo.tasks;

import me.tongfei.progressbar.ProgressBar;
import org.chaosdragon.stegovideo.params.InputOutputOptions;
import org.chaosdragon.stegovideo.tools.PSNRMeasurer;

/**
 * A class that does benchmarking tasks
 * @author David Griberman
 */
public class PsnrTask implements Runnable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PsnrTask.class);

    private final InputOutputOptions io;

    public PsnrTask(InputOutputOptions io) {
        this.io = io;
    }

    @Override
    public void run() {
        log.info("Started PSNR calclulation {} -> {}",  io.getStegoconainerPath(), io.getContainerPath());
        Double psnr = calculatePsnr(io);
        System.out.println(String.format("PSNR %.2f", psnr));
    }

    private static double calculatePsnr(InputOutputOptions io) {
        try (ProgressBar progressBar = new ProgressBar(
                "PSNR", 100)) {
            PSNRMeasurer ps = new PSNRMeasurer();
            progressBar.step();
            double psnr = ps.getPSNR(io.getContainerPath(), io.getStegoconainerPath(), null);
            progressBar.stepTo(progressBar.getMax());
            return psnr;
        } catch (Exception ex) {
            log.error("Error during PSNR callucation", ex);
            return -999.99;
        }
    }
}
