package model;

import java.io.Serializable;
import java.util.List;

/**
 * Everything about one game after the seed generator has run for each question: the player's inputs,
 * the seeds, the final integers and the answer options. The correct answers are NOT stored; they are
 * calculated again by MathService when the player submits.
 * Each question is filled separately (the player runs the seed generator question by question).
 */
public class Round implements Serializable {
    private boolean primeReady, modReady;
    private int primeInput, primeSeed;
    private int dividendInput, dividendSeed, divisor;
    private List<Integer> modOptions;
    private TrigQuestion trig1, trig2;

    public static String signed(int v) { return (v > 0 ? "+" : "") + v; }

    public synchronized boolean isComplete() { return primeReady && modReady && trig1 != null && trig2 != null; }

    public synchronized int getPrimeInput() { return primeInput; }
    public synchronized int getPrimeSeed() { return primeSeed; }
    public synchronized String getPrimeSeedText() { return signed(primeSeed); }
    public synchronized int getPrimeFinal() { return primeInput + primeSeed; }
    public synchronized void setPrime(int input, int seed) { this.primeInput = input; this.primeSeed = seed; this.primeReady = true; }

    public synchronized int getDividendInput() { return dividendInput; }
    public synchronized int getDividendSeed() { return dividendSeed; }
    public synchronized String getDividendSeedText() { return signed(dividendSeed); }
    public synchronized int getDividendFinal() { return dividendInput + dividendSeed; }
    public synchronized int getDivisor() { return divisor; }
    public synchronized List<Integer> getModOptions() { return modOptions; }
    public synchronized void setModulus(int dividend, int seed, int divisor, List<Integer> options) {
        this.dividendInput = dividend; this.dividendSeed = seed; this.divisor = divisor; this.modOptions = options; this.modReady = true;
    }

    public synchronized TrigQuestion getTrig1() { return trig1; }
    public synchronized TrigQuestion getTrig2() { return trig2; }
    public synchronized void setTrig1(TrigQuestion t) { this.trig1 = t; }
    public synchronized void setTrig2(TrigQuestion t) { this.trig2 = t; }
}
