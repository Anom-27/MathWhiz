package service;

/** Run this class's main(): it prints PASS/FAIL for every test case of TASK 09. */
public class MathServiceTest {
    private static int passed = 0, failed = 0;

    private static void check(String name, boolean ok) {
        if (ok) { passed++; System.out.println("PASS  " + name); }
        else    { failed++; System.out.println("FAIL  " + name); }
    }
    private static boolean near(double a, double b) { return Math.abs(a - b) < 1e-9; }

    public static void main(String[] args) {
        // Prime
        check("isPrime(7) = 1", MathService.isPrime(7) == 1);
        check("isPrime(8) = 0", MathService.isPrime(8) == 0);
        check("isPrime(1) = 0", MathService.isPrime(1) == 0);
        check("isPrime(0) = 0", MathService.isPrime(0) == 0);
        check("isPrime(-7) = 0", MathService.isPrime(-7) == 0);
        check("isPrime(2) = 1", MathService.isPrime(2) == 1);
        check("isPrime(97) = 1", MathService.isPrime(97) == 1);
        check("isPrime(100) = 0", MathService.isPrime(100) == 0);

        // Modulus
        check("17 % 5 = 2", MathService.calculateMod(17, 5) == 2);
        check("-17 % 5 = -2", MathService.calculateMod(-17, 5) == -2);
        check("17 % -5 = 2", MathService.calculateMod(17, -5) == 2);
        check("0 % 5 = 0", MathService.calculateMod(0, 5) == 0);
        boolean threw = false;
        try { MathService.calculateMod(5, 0); } catch (IllegalArgumentException e) { threw = true; }
        check("divisor 0 -> validation error", threw);

        // Trigonometry
        check("sin 30 = 0.5", near(MathService.calculateSin(30), 0.5));
        check("cos 60 = 0.5", near(MathService.calculateCos(60), 0.5));
        check("tan 45 = 1.0", near(MathService.calculateTan(45), 1.0));
        check("sin 180 = 0 (no float noise)", MathService.calculateSin(180) == 0.0);
        check("cos 90 = 0 (no float noise)", MathService.calculateCos(90) == 0.0);
        check("tan 90 undefined (NaN)", Double.isNaN(MathService.calculateTan(90)));
        check("tan 270 undefined (NaN)", Double.isNaN(MathService.calculateTan(270)));
        check("sin^2 + cos^2 = 1 at 37 deg", near(Math.pow(MathService.calculateSin(37), 2) + Math.pow(MathService.calculateCos(37), 2), 1.0));

        // Answer checking with tolerance
        check("sin 45 accepts 0.71", MathService.checkTrig("sin", 45, 0.71));
        check("sin 45 accepts 0.7071", MathService.checkTrig("sin", 45, 0.7071));
        check("sin 45 rejects 0.8", !MathService.checkTrig("sin", 45, 0.8));
        check("tan 60 accepts 1.73", MathService.checkTrig("tan", 60, 1.73));
        check("tan 90 never correct", !MathService.checkTrig("tan", 90, 0));
        check("checkPrime(7, 1)", MathService.checkPrime(7, 1));
        check("checkPrime(8, 1) wrong", !MathService.checkPrime(8, 1));
        check("checkMod(17, 5, 2)", MathService.checkMod(17, 5, 2));

        // Scoring
        check("all correct = 10", MathService.calculateScore(true, true, true, true) == 10);
        check("all wrong = 0", MathService.calculateScore(false, false, false, false) == 0);
        check("only prime = 2", MathService.calculateScore(true, false, false, false) == 2);
        check("prime+mod = 4", MathService.calculateScore(true, true, false, false) == 4);
        check("both trig = 6", MathService.calculateScore(false, false, true, true) == 6);
        check("prime+trig1 = 5", MathService.calculateScore(true, false, true, false) == 5);

        // Big / negative angles are reduced exactly (no floating-point noise)
        check("sin 3600 = 0", MathService.calculateSin(3600) == 0.0);
        check("sin 390 = 0.5", near(MathService.calculateSin(390), 0.5));
        check("cos -60 = 0.5", near(MathService.calculateCos(-60), 0.5));
        check("sin 1000000 = sin 280", near(MathService.calculateSin(1000000), MathService.calculateSin(280)));
        check("tan 450 undefined", Double.isNaN(MathService.calculateTan(450)));
        check("sin 30.5 works for decimals", near(MathService.calculateSin(30.5), Math.sin(Math.toRadians(30.5))));

        // Seed generator: always 1..10, added or subtracted, both signs occur
        java.util.Random rnd = new java.util.Random(7);
        boolean inRange = true, sawPlus = false, sawMinus = false;
        for (int i = 0; i < 2000; i++) {
            int seed = MathService.randomSeed(rnd);
            if (Math.abs(seed) < 1 || Math.abs(seed) > 10) inRange = false;
            if (seed > 0) sawPlus = true; else sawMinus = true;
        }
        check("seed is always 1..10 (never 0, never >10)", inRange);
        check("seed is sometimes added and sometimes subtracted", sawPlus && sawMinus);
        check("12 + seed(+7) = 19 is prime", MathService.checkPrime(12 + 7, 1));

        // Trig with an integer in front, e.g. 5 sin 30
        check("5 sin 30 = 2.5", near(MathService.calculateTrigTerm(5, "sin", 30), 2.5));
        check("8 sin 30 = 4.0", near(MathService.calculateTrigTerm(8, "sin", 30), 4.0));
        check("-3 cos 60 = -1.5", near(MathService.calculateTrigTerm(-3, "cos", 60), -1.5));
        check("0 tan 45 = 0", MathService.calculateTrigTerm(0, "tan", 45) == 0.0);
        check("checkTrigTerm accepts option 4.00 for 8 sin 30", MathService.checkTrigTerm(8, "sin", 30, 4.00));
        check("checkTrigTerm rejects 4.50 for 8 sin 30", !MathService.checkTrigTerm(8, "sin", 30, 4.50));
        check("formatTrig never gives -0.00", MathService.formatTrig(-0.0).equals("0.00") && MathService.formatTrig(-0.001).equals("0.00"));

        // Options: 3 different values, the right one is always among them
        boolean modOk = true, trigOk = true;
        for (int i = 0; i < 2000 && modOk && trigOk; i++) {
            int correct = MathService.calculateMod(20 + rnd.nextInt(80), 2 + rnd.nextInt(10));
            java.util.List<Integer> m = MathService.modOptions(correct, rnd);
            modOk = m.size() == 3 && new java.util.HashSet<>(m).size() == 3 && m.contains(correct);

            double v = MathService.calculateTrigTerm(rnd.nextInt(41) - 20, "sin", rnd.nextInt(361));
            java.util.List<String> t = MathService.trigOptions(v, rnd);
            long close = t.stream().filter(x -> Math.abs(Double.parseDouble(x) - v) <= 0.01).count();
            trigOk = t.size() == 3 && new java.util.HashSet<>(t).size() == 3 && close == 1;
        }
        check("2000 x modulus options: 3 distinct, contain the right remainder", modOk);
        check("2000 x trig options: 3 distinct, exactly one right answer", trigOk);

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
    }
}
