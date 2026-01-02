package com.group18.greengrocer.util;

import java.util.regex.Pattern;

import com.group18.greengrocer.service.ValidationException;

/**
 * Utility class for common validation logic.
 * Follows the Single Responsibility Principle for validation rules.
 */
public class ValidatorUtil {

    // Regex Patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,13}$");
    // Name Pattern: Allows letters (English + Turkish) and spaces. No numbers or special symbols.
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZçÇğĞıİöÖşŞüÜ\\s]+$");

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
        if (isEmpty(input))
            return false;
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
        if (!isNumeric(input))
            return false;
        return Double.parseDouble(input) > 0;
    }

    /**
     * Checks if password meets strong password requirements.
     * Rule: At least 8 chars, 1 uppercase, 1 lowercase, 1 digit.
     */
    public static boolean isStrongPassword(String password) {
        if (isEmpty(password))
            return false;
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
        if (isEmpty(email))
            return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates phone number format.
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (isEmpty(phone))
            return false;
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validates if the string contains only letters and spaces (for names).
     */
    public static boolean isValidName(String name) {
        if (isEmpty(name))
            return false;
        return NAME_PATTERN.matcher(name).matches();
    }

    public static void validateNotNull(Object object, String message) {
        if (object == null) {
            throw new ValidationException(message);
        }
    }

    /**
     * Validates that a string is not null or empty.
     */
    public static void validateNotEmpty(String input, String message) {
        if (isEmpty(input)) {
            throw new ValidationException(message);
        }
    }

    /**
     * Validates that a condition is true.
     */
    public static void validateTrue(boolean condition, String message) {
        if (!condition) {
            throw new ValidationException(message);
        }
    }

    /**
     * Validates that a number is positive.
     */
    public static void validatePositive(double number, String message) {
        if (number <= 0) {
            throw new ValidationException(message);
        }
    }

    /**
     * Validates that a number is positive.
     */
    public static void validatePositive(int number, String message) {
        if (number <= 0) {
            throw new ValidationException(message);
        }
    }

    /**
     * Validates email format and throws exception if invalid.
     */
    public static void validateEmail(String email, String message) {
        if (!isValidEmail(email)) {
            throw new ValidationException(message);
        }
    }

    /**
     * Validates phone number format and throws exception if invalid.
     */
    public static void validatePhoneNumber(String phone, String message) {
        if (!isValidPhoneNumber(phone)) {
            throw new ValidationException(message);
        }
    }

    /**
     * Validates password strength and throws exception if weak.
     */
    public static void validatePassword(String password, String message) {
        if (!isStrongPassword(password)) {
            throw new ValidationException(message);
        }
    }

    /**
     * Validates that a date is in the future.
     */
    public static void validateFutureDate(java.util.Date date, String message) {
        if (date == null || !date.after(new java.util.Date())) {
            throw new ValidationException(message);
        }
    }

    /**
     * Validates that a name contains only letters and spaces.
     */
    public static void validateName(String name, String message) {
        if (!isValidName(name)) {
            throw new ValidationException(message);
        }
    }
}
