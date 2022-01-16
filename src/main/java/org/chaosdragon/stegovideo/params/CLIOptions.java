package org.chaosdragon.stegovideo.params;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.chaosdragon.stegovideo.algorithms.EmbeddingAlgorithmType;
import org.chaosdragon.stegovideo.exceptions.EncodingException;

import java.io.File;

public class CLIOptions {

    private CLIOptions() {
        // Utility class
    }

    public static Options prepareEmbeddingOptions() {
        Options options = new Options();
        options.addOption(Option.builder("c")
                .longOpt("container")
                .argName("file")
                .hasArg()
                .desc("video file to embed into (required)")
                .required()
                .build());
        options.addOption(Option.builder("o")
                .longOpt("output")
                .argName("file")
                .hasArg()
                .desc("output video file to embed into (required)")
                .required()
                .build());
        options.addOption(Option.builder("w")
                .longOpt("watermark")
                .argName("file")
                .hasArg()
                .desc("BMP watermark image to embed (required)")
                .required()
                .build());
        addAlgoritmOptions(options, true);
        addAttackOptions(options);
        return options;
    }

    private static void addAlgoritmOptions(Options options, boolean embedding) {
        options.addOption(Option.builder("a")
                .longOpt("algorithm")
                .argName("id")
                .hasArg()
                .desc("algorithm to use for embedding (0-4): 0. Null 1. Kaur 2. Dubai 3. Kothari 4. Haar")
                .required()
                .build());
        options.addOption(Option.builder("k")
                .longOpt("key")
                .argName("password")
                .hasArg()
                .desc("password used for embedding")
                .build());
        options.addOption(Option.builder("str")
                .argName("strength")
                .hasArg()
                .desc("embedding strength (1-64). Defaults to 8. Recommendations: Kaur 8-64; Dubai 16-64; Kothari 16-64; Haar 2-4")
                .build());
        options.addOption(Option.builder("com")
                .argName("level")
                .hasArg()
                .desc("the quality of the image during DCT transformation (0 best - 25 worst), defaults to 0")
                .build());
        options.addOption(Option.builder("block")
                .argName("size")
                .hasArg()
                .desc("DCT block size. Defaults to 8")
                .build());
        options.addOption(Option.builder()
                .longOpt("no-noise")
                .desc("do not fill unused embeddable data with noise")
                .build());
        options.addOption(Option.builder()
                .longOpt("single-copy")
                .desc("embed only one watermark copy per frame")
                .build());

        if (embedding) {
            options.addOption(Option.builder()
                    .longOpt("adaptive")
                    .desc("use adaptive embedding (see documentation). Supports only Kaur and Kothari algorithms")
                    .build());
        }
    }

    private static void addAttackOptions(Options options) {
        options.addOption(Option.builder()
                .longOpt("attack-compress1")
                .desc("compression attack (1/0.1)")
                .build());
        options.addOption(Option.builder()
                .longOpt("attack-compress2")
                .desc("compression attack (0.5/0.1)")
                .build());
        options.addOption(Option.builder()
                .longOpt("attack-overlay")
                .desc("overlay attack (overlay logo)")
                .build());
        options.addOption(Option.builder()
                .longOpt("attack-resize")
                .desc("resize attack (size / 2)")
                .build());
        options.addOption(Option.builder()
                .longOpt("attack-flip")
                .desc("flip attack (horizontal)")
                .build());
        options.addOption(Option.builder()
                .longOpt("attack-crop")
                .desc("crp[ attack (16:9 to 2.39:1)")
                .build());
    }

    public static Options prepareExtractionOptions() {
        Options options = new Options();
        options.addOption(Option.builder("s")
                .longOpt("stegocontainer")
                .argName("file")
                .hasArg()
                .desc("video file to extract from (required)")
                .required()
                .build());
        options.addOption(Option.builder("o")
                .longOpt("output")
                .argName("file")
                .hasArg()
                .desc("bmp file to extract to")
                .required()
                .build());
        options.addOption(Option.builder("w")
                .longOpt("watermark")
                .argName("file")
                .hasArg()
                .desc("original BMP watermark image for comparison. It's size overwrites width & height params.")
                .build());
        options.addOption(Option.builder()
                .longOpt("watermark-path")
                .argName("path")
                .hasArg()
                .desc("path where to extract per-frame watermarks. WARNING! All files will be deleted!")
                .build());
        options.addOption(Option.builder("width")
                .argName("pixels")
                .hasArg()
                .desc("width of the watermark to extract")
                .build());
        options.addOption(Option.builder("height")
                .argName("pixels")
                .hasArg()
                .desc("height of the watermark to extract")
                .build());
        addAlgoritmOptions(options, false);
        return options;
    }

    public static Options preparePsnrOptions() {
        Options options = new Options();
        options.addOption(Option.builder("c")
                .longOpt("container")
                .argName("file")
                .hasArg()
                .desc("video file without watermark, " +
                        "should be processed the same way as stegocontainer with NULL algorithm (required)")
                .required()
                .build());
        options.addOption(Option.builder("s")
                .longOpt("stegocontainer")
                .argName("file or path")
                .hasArg()
                .desc("video file or folder with embeded watermark (required)")
                .required()
                .build());
        return options;
    }

    public static InputOutputOptions prepareEmbeddingIOOptions(CommandLine line) {
        InputOutputOptions options = new InputOutputOptions();
        options.setContainerPath(line.getOptionValue("c"));
        checkFileExistence(options.getContainerPath());
        options.setStegoconainerPath(line.getOptionValue("o"));
        options.setPayloadPath(line.getOptionValue("w"));
        checkFileExistence(options.getPayloadPath());
        return options;
    }

    public static InputOutputOptions prepareExtractionIOOptions(CommandLine line) {
        InputOutputOptions options = new InputOutputOptions();
        options.setStegoconainerPath(line.getOptionValue("s"));
        checkFileExistence(options.getStegoconainerPath());
        options.setPayloadPath(line.getOptionValue("o"));

        if (line.hasOption("watermark-path")) {
            options.setOutputPath(line.getOptionValue("watermark-path"));
            checkPathExistence(options.getOutputPath());
        }

        if (line.hasOption("w")) {
            options.setOriginalPayloadPath(line.getOptionValue("w"));
            checkFileExistence(options.getOriginalPayloadPath());
        } else if (line.hasOption("width") && line.hasOption("height")) {
            options.setWatermarkWidth(parseNumber(line.getOptionValue("width")));
            options.setWatermarkHeight(parseNumber(line.getOptionValue("height")));
        } else {
            throw new IllegalArgumentException(
                    "Please specify either original watermark or width & height of the extractable one");
        }
        return options;
    }

    public static InputOutputOptions preparePsnrIOOptions(CommandLine line) {
        InputOutputOptions options = new InputOutputOptions();
        options.setContainerPath(line.getOptionValue("c"));
        options.setStegoconainerPath(line.getOptionValue("s"));

        checkFileExistence(options.getContainerPath());
        checkFileExistence(options.getStegoconainerPath());
        return options;
    }

    public static AlgorithmOptions prepareAlgorithmOptions(CommandLine line, boolean embed) {
        AlgorithmOptions options = new AlgorithmOptions();
        int algorithmId = parseNumber(line.getOptionValue("a"));
        options.setEmbeddingAlgorithmType(getAlgorithmType(algorithmId));

        if (line.hasOption("k")) {
            options.setKey(line.getOptionValue("k"));
        }
        if (line.hasOption("str")) {
            options.setStrength(parseNumber(line.getOptionValue("str")));
        }
        if (line.hasOption("com")) {
            options.setCompression(parseNumber(line.getOptionValue("com")));
        }
        if (line.hasOption("block")) {
            options.setBlockSize(parseNumber(line.getOptionValue("block")));
        }
        
        // Rarely set parameters
        if (line.hasOption("no-noise")) {
            options.setFillWithNoise(false);
        }

        if (line.hasOption("single-copy")) {
            options.setMultipleCopies(false);
        }

        // Only for embedding!
        if (line.hasOption("adaptive") && embed) {
            if (options.getEmbeddingAlgorithmType() == EmbeddingAlgorithmType.KAUR_ALGORITHM
                    || options.getEmbeddingAlgorithmType() == EmbeddingAlgorithmType.KOTHARI_ALGORITHM) {
                options.setAdaptiveEmbedding(true);
            } else {
                throw new IllegalArgumentException("Adaptive embedding supported only for KAUR and KOTHARI algorithms!");
            }
        }

        return options;
    }

    private static EmbeddingAlgorithmType getAlgorithmType(int algorithmId) {
        switch (algorithmId) {
            case 0: return EmbeddingAlgorithmType.NULL_ALGORITHM;
            case 1: return EmbeddingAlgorithmType.KAUR_ALGORITHM;
            case 2: return EmbeddingAlgorithmType.DUBAI_ALGORITHM;
            case 3: return EmbeddingAlgorithmType.KOTHARI_ALGORITHM;
            case 4: return EmbeddingAlgorithmType.HAAR_ALGORITHM;
            default: throw new IllegalArgumentException("Unknown algorithm id " + algorithmId);
        }
    }

    public static AttackOptions prepareAttackOptions(CommandLine line) {
        AttackOptions options = new AttackOptions();
        if (line.hasOption("attack-compress1")) {
            options.setCompression(true);
        }
        if (line.hasOption("attack-compress2")) {
            options.setCompression2(true);
        }
        if (line.hasOption("attack-overlay")) {
            options.setOverlay(true);
        }
        if (line.hasOption("attack-resize")) {
            options.setResize(0.5);
        }
        if (line.hasOption("attack-flip")) {
            options.setFlip(true);
        }
        if (line.hasOption("attack-crop")) {
            options.setCrop(true);
        }

        return options;
    }

    private static void checkFileExistence(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new EncodingException("Unable to access given file: " + filePath);
        }
    }

    private static void checkPathExistence(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            throw new EncodingException("Unable to access directory: " + path);
        }
    }

    private static int parseNumber(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number " + input);
        }
    }
}
