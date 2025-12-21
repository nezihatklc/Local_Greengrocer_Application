package com.group18.greengrocer.util;

import java.util.regex.Pattern;

/**
 * Utility class for common validation logic.
 * Follows the Single Responsibility Principle for validation rules.
 */
public class ValidatorUtil {

    // Regex Patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,13}$"); 

    /**
     * Checks if a string is null or empty.
     */
    public static boolean isEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    /**
     * Validates if the input string is a valid double.
     */
    public static boolean isNumeric(String input) {
        if (isEmpty(input)) return false;
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates if the input string is a positive double.
     */
    public static boolean isPositive(String input) {
        if (!isNumeric(input)) return false;
        return Double.parseDouble(input) > 0;
    }

    /**
     * Checks if password meets strong password requirements.
     * Rule: At least 8 chars, 1 uppercase, 1 lowercase, 1 digit.
     */
    public static boolean isStrongPassword(String password) {
        if (isEmpty(password)) return false;
        boolean hasUpper = !password.equals(password.toLowerCase());
        boolean hasLower = !password.equals(password.toUpperCase());
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean isLongEnough = password.length() >= 8;

        return hasUpper && hasLower && hasDigit && isLongEnough;
    }

    /**
     * Validates email format.
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
