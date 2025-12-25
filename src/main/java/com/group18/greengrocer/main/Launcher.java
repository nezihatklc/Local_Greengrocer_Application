package com.group18.greengrocer.main;

/**
 * Launcher class to bypass JavaFX module checks.
 * This is necessary when running the application as a non-modular project.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
