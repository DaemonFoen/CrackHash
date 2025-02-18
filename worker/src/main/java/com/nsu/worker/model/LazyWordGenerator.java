package com.nsu.worker.model;

import java.util.List;

import java.util.stream.LongStream;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LazyWordGenerator {
    private final List<Character> alphabet;
    private final int maxLength;
    private final long startIndex;
    private final long endIndex;
    private final long[] wordsPerLength;

    public LazyWordGenerator(List<Character> alphabet, int maxLength, int partCount, int partNumber) {
        this.alphabet = alphabet;
        this.maxLength = maxLength;

        long totalWords = 1;
        wordsPerLength = new long[maxLength + 1];
        wordsPerLength[0] = 1;

        for (int i = 1; i <= maxLength; i++) {
            wordsPerLength[i] = (long) Math.pow(alphabet.size(), i);
            totalWords += wordsPerLength[i];
        }

        long chunkSize = (long) Math.ceil((double) totalWords / partCount);
        this.startIndex = partNumber * chunkSize;
        log.info("startIndex:" + startIndex);
        this.endIndex = Math.min(startIndex + chunkSize, totalWords);
        log.info("endIndex:" + endIndex);
    }

    private String getWordByIndex(long index) {
        if (index == 0) return "";

        long offset = 1;

        for (int length = 1; length <= maxLength; length++) {
            long numWords = wordsPerLength[length];
            if (index < offset + numWords) {
                return generateWord(index - offset, length);
            }
            offset += numWords;
        }
        throw new IllegalStateException("Index out of bounds");
    }

    private String generateWord(long index, int length) {
        StringBuilder word = new StringBuilder();
        int base = alphabet.size();

        for (int i = 0; i < length; i++) {
            word.insert(0, alphabet.get((int) (index % base)));
            index /= base;
        }
        return word.toString();
    }

    public Stream<String> generateWords() {
        return LongStream.range(startIndex, endIndex).peek(e -> {
                    if (e % 1000000 == 0){
                        log.info("idx {}", e);
                    }
                }).mapToObj(this::getWordByIndex);
    }

}
