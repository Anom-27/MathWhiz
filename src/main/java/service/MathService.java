package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

/** All mathematical logic of the game. Results are always CALCULATED here, never stored. */
public final class MathService {

    public static final String DIFFICULTY = "NORMAL";
    /** Trigonometric answers are accepted if they differ from the calculated value by at most this much. */
    public static final double TOLERANCE = 0.01;
    public static final int MARKS_PRIME = 2, MARKS_MOD = 2, MARKS_TRIG = 3;

    private MathService() { }

    // ---------- TASK 03: prime ----------
    /** Returns 1 if number is prime, otherwise 0. Numbers below 2 are not prime. */
    public static int isPrime(int number) {
        if (number < 2) {
            return 0;
        }
        for (int i = 2; (long) i * i <= number; i++) {   // only need to test up to sqrt(number)
            if (number % i == 0) {
                return 0;
            }
        }
        return 1;
    }

    // ---------- TASK 04: modulus ----------
    /** Returns a % b. A zero divisor is rejected with IllegalArgumentException (validation error). */
    public static int calculateMod(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("Divisor must not be zero.");
        }
        return a % b;
    }

    // ---------- TASK 05: trigonometry (degrees in, radians used internally) ----------
    public static double calculateSin(double degree) {
        return clean(Math.sin(Math.toRadians(degree % 360)));
    }

    public static double calculateCos(double degree) {
        return clean(Math.cos(Math.toRadians(degree % 360)));
    }

    /** tan is undefined where cos = 0 (90, 270, ...): returns Double.NaN for those angles. */
    public static double calculateTan(double degree) {
        double radians = Math.toRadians(degree % 360);
        if (Math.abs(Math.cos(radians)) < 1e-10) {
            return Double.NaN;
        }
        return clean(Math.tan(radians));
    }

    public static double calculateTrig(String function, double degree) {
        switch (function) {
            case "sin": return calculateSin(degree);
            case "cos": return calculateCos(degree);
            case "tan": return calculateTan(degree);
            default: throw new IllegalArgumentException("Unknown function: " + function);
        }
    }

    /** Floating-point noise such as sin(180) = 1.2e-16 is shown as an exact 0. */
    private static double clean(double v) {
        return Math.abs(v) < 1e-12 ? 0.0 : v;
    }

    // ---------- TASK 06: checking the player's answers + scoring ----------
    public static boolean checkPrime(int number, int answer) {
        return isPrime(number) == answer;
    }

    public static boolean checkMod(int a, int b, int answer) {
        return calculateMod(a, b) == answer;
    }

    public static boolean checkTrig(String function, double degree, double answer) {
        double expected = calculateTrig(function, degree);
        return !Double.isNaN(expected) && Math.abs(expected - answer) <= TOLERANCE + 1e-9;
    }

    public static int calculateScore(boolean primeCorrect, boolean modCorrect, boolean trig1Correct, boolean trig2Correct) {
        int totalScore = 0;
        if (primeCorrect) totalScore += MARKS_PRIME;
        if (modCorrect)   totalScore += MARKS_MOD;
        if (trig1Correct) totalScore += MARKS_TRIG;
        if (trig2Correct) totalScore += MARKS_TRIG;
        return totalScore;                                 // maximum 10
    }

    // ---------- seed generator ----------
    /** Random number from 1 to 10, randomly added (+) or subtracted (-): returns -10..-1 or +1..+10. */
    public static int randomSeed(Random rnd) {
        int seed = 1 + rnd.nextInt(10);
        return rnd.nextBoolean() ? seed : -seed;
    }

    // ---------- trigonometry with an integer coefficient, e.g. 5 sin 30 ----------
    public static double calculateTrigTerm(int coefficient, String function, double degree) {
        return clean(coefficient * calculateTrig(function, degree));
    }

    public static boolean checkTrigTerm(int coefficient, String function, double degree, double answer) {
        double expected = calculateTrigTerm(coefficient, function, degree);
        return !Double.isNaN(expected) && Math.abs(expected - answer) <= TOLERANCE + 1e-9;
    }

    /** Two decimal places, never "-0.00". */
    public static String formatTrig(double v) {
        String s = String.format(Locale.ROOT, "%.2f", v);
        return s.equals("-0.00") ? "0.00" : s;
    }

    // ---------- answer options (the correct answer + wrong ones, shuffled) ----------
    public static List<Integer> modOptions(int correct, Random rnd) {
        Set<Integer> set = new LinkedHashSet<>();
        set.add(correct);
        while (set.size() < 3) {
            int offset = 1 + rnd.nextInt(5);
            set.add(rnd.nextBoolean() ? correct + offset : correct - offset);
        }
        List<Integer> list = new ArrayList<>(set);
        Collections.shuffle(list, rnd);
        return list;
    }

    public static List<String> trigOptions(double correct, Random rnd) {
        double[] offsets = {-3, -2, -1, -0.5, 0.5, 1, 2, 3};      // all far outside the 0.01 tolerance
        List<String> list = new ArrayList<>();
        list.add(formatTrig(correct));
        while (list.size() < 3) {
            String wrong = formatTrig(correct + offsets[rnd.nextInt(offsets.length)]);
            if (!list.contains(wrong)) {
                list.add(wrong);
            }
        }
        Collections.shuffle(list, rnd);
        return list;
    }
}
