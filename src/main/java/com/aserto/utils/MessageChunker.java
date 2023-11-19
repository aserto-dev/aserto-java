package com.aserto.utils;

/* Chunk messages into smaller pieces. */
public class MessageChunker {
    private int maxChunkSize;
    private byte[] message;

    private int chunkStart = 0;

    public MessageChunker(int maxChunkSize, byte[] message) {
        this.maxChunkSize = maxChunkSize;
        this.message = message;
    }

    public byte[] nextChunk() {
        int chunkEnd = Math.min(chunkStart + maxChunkSize, message.length);

        byte[] chunk = new byte[chunkEnd - chunkStart];
        System.arraycopy(message, chunkStart, chunk, 0, chunk.length);
        chunkStart = chunkEnd;
        return chunk;
    }

    public boolean hasNextChunk() {
        return chunkStart < message.length;
    }

}
