package org.chaosdragon.stegovideo.embedders;

import org.chaosdragon.stegovideo.encoders.MessageEncoder;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An interface describing an embedder used in an embedding algorithm
 *
 * @author David Griberman
 */
public interface DTCEmbedder {

    /**
     * Embed information inside of the matrix
     *
     * @param input matrix to embed into
     * @param channel chanel to embed into
     * @return matrix with embedded data
     */
    int[][] embed(int[][] input, int channel);

    /**
     * Extracts information from a matrix
     *
     * @param n the input matrix
     * @param channel the channel to read
     * @return information extracted
     * @throws IOException
     */
    void decodeBlock(int[][] n, int channel) throws IOException;

    /**
     * Determine if the channel is used for embedding
     *
     * @param channel channel where embedding is to be done (0-2, RGB or YCbCr)
     * @return possibility of embedding in the channel
     */
    boolean willHide(int channel);

    /**
     * Sets the encoder for embedding
     *
     * @param encoder
     */
    void setEncoder(MessageEncoder encoder);

    /**
     * Sets the OutputStream for extracting
     *
     * @param decoder
     */
    void setOutputStream(OutputStream decoder);

    /**
     * Resets the random number generator
     */
    void reset();
}
