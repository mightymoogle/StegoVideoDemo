package org.chaosdragon.stegovideo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.chaosdragon.stegovideo.BWBitmap.BWBitmap;
import org.chaosdragon.stegovideo.encoders.BWBitmapEncoderFactory;
import org.chaosdragon.stegovideo.encoders.EncoderFactory;
import org.chaosdragon.stegovideo.params.AlgorithmOptions;
import org.chaosdragon.stegovideo.params.AttackOptions;
import org.chaosdragon.stegovideo.params.InputOutputOptions;
import org.chaosdragon.stegovideo.tasks.EmbeddingTask;
import org.chaosdragon.stegovideo.tasks.ExtractingTask;
import org.chaosdragon.stegovideo.tasks.PsnrTask;
import org.chaosdragon.stegovideo.writers.MasterWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.function.Consumer;

import static org.chaosdragon.stegovideo.params.CLIOptions.prepareAlgorithmOptions;
import static org.chaosdragon.stegovideo.params.CLIOptions.prepareAttackOptions;
import static org.chaosdragon.stegovideo.params.CLIOptions.prepareEmbeddingOptions;
import static org.chaosdragon.stegovideo.params.CLIOptions.prepareEmbeddingIOOptions;
import static org.chaosdragon.stegovideo.params.CLIOptions.prepareExtractionIOOptions;
import static org.chaosdragon.stegovideo.params.CLIOptions.prepareExtractionOptions;
import static org.chaosdragon.stegovideo.params.CLIOptions.preparePsnrIOOptions;
import static org.chaosdragon.stegovideo.params.CLIOptions.preparePsnrOptions;

public class StegoVideoApp {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StegoVideoApp.class);

    public static void main(String[] args) {
        try {
            System.out.println("*** Video Steganography / Watermarking Demo Tool ***");
            System.out.println("(C) David Griberman 2014-2022");
            System.out.println();
            verifyRunningOn32BitJava();
            run(args);
        } catch (Exception e) {
            log.error("Critical application error", e);
        }
    }

    private static void run(String[] args) {
        if (args.length > 0) {
            String command = args[0];
            if (command.equalsIgnoreCase("e") || command.equalsIgnoreCase("embed")) {
                Options options = prepareEmbeddingOptions();
                parseAndExecute("embed", args, options,
                        line -> embed(prepareEmbeddingIOOptions(line), prepareAlgorithmOptions(line, true),
                                prepareAttackOptions(line)));
                return;
            }

            if (command.equalsIgnoreCase("x") || command.equalsIgnoreCase("extract")) {
                Options options = prepareExtractionOptions();
                parseAndExecute("extract", args, options,
                        line -> extract(prepareExtractionIOOptions(line), prepareAlgorithmOptions(line, false)));
                return;
            }

            if (command.equalsIgnoreCase("p") || command.equalsIgnoreCase("psnr")) {
                Options options = preparePsnrOptions();
                parseAndExecute("psnr", args, options,
                        line -> calculatePsnr(preparePsnrIOOptions(line)));
                return;
            }

            log.error("Unknown command: {}", command);
        } else {
            System.out.println("Missing command, try one of the following to see more information:");
            System.out.println("  embed    OR e - to embed a BMP watermark into a video file.");
            System.out.println("  extract  OR x - to extract a BMP watermark from a video file.");
            System.out.println("  psnr     OR p - to calculate the PSNR of an file with an embeded watermark.");
            System.exit(1);
        }
    }

    private static void parseAndExecute(String commandName, String[] args, Options options,
                                        Consumer<CommandLine> consumer) {
        if (args.length == 1) {
            printHelp(commandName, options);
            return;
        }

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);
            consumer.accept(line);
        } catch (ParseException exp) {
            log.error("Parsing failed.  Reason: {}", exp.getMessage());
        }
    }

    private static void printHelp(String commandName, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("StegoVideoApp.jar " + commandName, options);
    }

    private static void embed(InputOutputOptions io,
                              AlgorithmOptions algorithmOptions, AttackOptions attackOptions) {
        try {
            EncoderFactory factory = new BWBitmapEncoderFactory(io.getPayloadPath())
                    .setKey(getKey(algorithmOptions.getKey().toCharArray()))
                    .setBlockSize(algorithmOptions.getBlockSize())
                    .setFillNoise(algorithmOptions.isFillWithNoise())
                    .setMultipleCopies(algorithmOptions.isMultipleCopies());

            MasterWriter videoEmbedding = new MasterWriter(io.getContainerPath(),
                    io.getStegoconainerPath(), null,
                    factory,
                    algorithmOptions.getEmbeddingAlgorithmType(),
                    algorithmOptions.getStrength(),
                    algorithmOptions.getBlockSize(),
                    algorithmOptions.getCompression());
            videoEmbedding.setAdaptive(algorithmOptions.isAdaptiveEmbedding());
            videoEmbedding.setAttackSettings(attackOptions);

            log.info("Started embedding {} -> {}", io.getContainerPath(), io.getStegoconainerPath());
            Thread t = new Thread(new EmbeddingTask(videoEmbedding, "Embed"));
            t.start();
            t.join();
        } catch (Exception e) {
            log.error("Error during embedding!", e);
        }
    }

    private static void extract(InputOutputOptions io,
                                AlgorithmOptions algorithmOptions) {
        try {
            boolean checkMark = io.getOriginalPayloadPath() != null; // Check with original watermark
            boolean extractEachToFile = io.getOutputPath() != null; // Extract per-frame watermarks separately

            int width = io.getWatermarkWidth();
            int height = io.getWatermarkHeight();

            if (checkMark) {
                BufferedImage originalWatermark = ImageIO.read(new File(io.getOriginalPayloadPath()));
                width = originalWatermark.getWidth();
                height = originalWatermark.getHeight();
            }

            EncoderFactory factory = checkMark
                    ? new BWBitmapEncoderFactory(io.getOriginalPayloadPath())
                    : new BWBitmapEncoderFactory(new BWBitmap(width, height));

            long key = getKey(algorithmOptions.getKey().toCharArray());

            factory.setKey(key)
                    .setWatermarkSize(width, height)
                    .setBlockSize(algorithmOptions.getBlockSize())
                    .setFillNoise(algorithmOptions.isFillWithNoise())
                    .setMultipleCopies(algorithmOptions.isMultipleCopies());

            MasterWriter videoExtracting = new MasterWriter(io.getStegoconainerPath(), io.getOutputPath(),
                    algorithmOptions.getEmbeddingAlgorithmType(), algorithmOptions.getStrength(),
                    algorithmOptions.getBlockSize(),
                    algorithmOptions.getCompression(), factory);

            log.info("Started extracting {} -> {}", io.getStegoconainerPath(), io.getPayloadPath());
            Thread t = new Thread(new ExtractingTask(videoExtracting, io.getPayloadPath(),
                    extractEachToFile, checkMark, "Extract"));

            t.setName("Extract: " + io.getStegoconainerPath());
            t.start();
            t.join();
        } catch (Exception e) {
            log.error("Error during extraction!", e);
        }
    }

    private static long getKey(char[] string) {
        if (string.length == 0) {
            return -1;
        }
        return Arrays.hashCode(string); //Not the best, but works
    }

    private static void calculatePsnr(InputOutputOptions io) {
        try {
            Thread t = new Thread(new PsnrTask(io));
            t.start();
            t.join();
        } catch (Exception e) {
            log.error("Error during PSNR calculation!", e);
        }
    }

    private static void verifyRunningOn32BitJava() {
        String model = System.getProperty("sun.arch.data.model");
        if (!model.equals("32")) {
            log.warn("You are most likely not running using 32 bit Java. Detected: {}", model);
            log.warn("Xuggler library has known issues with 64 bit Java (crashing on Windows).");
            log.warn("Please consider using the 32 bit version of Java 8 SDK or continue on your own risk!");
        }
    }
}
