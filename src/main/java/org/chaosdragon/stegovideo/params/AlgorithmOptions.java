package org.chaosdragon.stegovideo.params;

import org.chaosdragon.stegovideo.algorithms.EmbeddingAlgorithmType;

public class AlgorithmOptions {
    private String key = "qwerty";
    private EmbeddingAlgorithmType embeddingAlgorithmType = EmbeddingAlgorithmType.KAUR_ALGORITHM;
    private int strength = 8;
    private int compression = 0;
    private int blockSize = 8;
    private boolean fillWithNoise = true;
    private boolean multipleCopies = true;
    private boolean adaptiveEmbedding = false;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public EmbeddingAlgorithmType getEmbeddingAlgorithmType() {
        return embeddingAlgorithmType;
    }

    public void setEmbeddingAlgorithmType(EmbeddingAlgorithmType embeddingAlgorithmType) {
        this.embeddingAlgorithmType = embeddingAlgorithmType;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getCompression() {
        return compression;
    }

    public void setCompression(int compression) {
        this.compression = compression;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public boolean isFillWithNoise() {
        return fillWithNoise;
    }

    public void setFillWithNoise(boolean fillWithNoise) {
        this.fillWithNoise = fillWithNoise;
    }

    public boolean isMultipleCopies() {
        return multipleCopies;
    }

    public void setMultipleCopies(boolean multipleCopies) {
        this.multipleCopies = multipleCopies;
    }

    public boolean isAdaptiveEmbedding() {
        return adaptiveEmbedding;
    }

    public void setAdaptiveEmbedding(boolean adaptiveEmbedding) {
        this.adaptiveEmbedding = adaptiveEmbedding;
    }
}
