package com.urlshortener.util;

import org.springframework.stereotype.Component;

/**
 * Encodes/decodes long numbers to Base62 strings for compact short URL codes.
 * Uses characters [0-9 A-Z a-z] (62 chars), producing 7-char codes for numbers
 * up to 62^7 = ~3.5 trillion unique short URLs.
 */
@Component
public class Base62Encoder {

    private static final String ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;
    private static final int CODE_LENGTH = 7;

    /**
     * Encodes a positive long number to a zero-padded Base62 string of CODE_LENGTH chars.
     *
     * @param number positive number to encode
     * @return Base62 string of length CODE_LENGTH
     */
    public String encode(long number) {
        if (number < 0) {
            number = Math.abs(number);
        }
        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            sb.insert(0, ALPHABET.charAt((int) (number % BASE)));
            number /= BASE;
        }
        // Left-pad with '0' to ensure consistent length
        while (sb.length() < CODE_LENGTH) {
            sb.insert(0, '0');
        }
        // Trim to CODE_LENGTH if somehow longer
        if (sb.length() > CODE_LENGTH) {
            sb = new StringBuilder(sb.substring(sb.length() - CODE_LENGTH));
        }
        return sb.toString();
    }

    /**
     * Decodes a Base62 string to a long number.
     *
     * @param code Base62 encoded string
     * @return decoded long value
     */
    public long decode(String code) {
        long result = 0;
        for (char c : code.toCharArray()) {
            result = result * BASE + ALPHABET.indexOf(c);
        }
        return result;
    }

    /**
     * Validates that a code contains only valid Base62 characters.
     *
     * @param code string to validate
     * @return true if valid Base62
     */
    public boolean isValidCode(String code) {
        if (code == null || code.isEmpty()) return false;
        return code.chars().allMatch(c -> ALPHABET.indexOf(c) >= 0);
    }
}
