package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** One trigonometry question such as "8 sin 30" (integer coefficient + function + angle in degrees). */
public class TrigQuestion implements Serializable {
    private final int coefInput;       // integer typed by the player, e.g. 5
    private final int coefSeed;        // seed generator result, -10..-1 or +1..+10
    private final String function;     // sin | cos | tan
    private final double angle;        // degrees
    private final List<String> options;

    public TrigQuestion(int coefInput, int coefSeed, String function, double angle, List<String> options) {
        this.coefInput = coefInput;
        this.coefSeed = coefSeed;
        this.function = function;
        this.angle = angle;
        this.options = options;
    }

    public int getCoefInput() { return coefInput; }
    public int getCoefSeed() { return coefSeed; }
    public int getCoefFinal() { return coefInput + coefSeed; }
    public String getSeedText() { return Round.signed(coefSeed); }
    public String getFunction() { return function; }
    public double getAngle() { return angle; }
    public String getAngleText() { return BigDecimal.valueOf(angle).stripTrailingZeros().toPlainString(); }
    public List<String> getOptions() { return options; }
}
