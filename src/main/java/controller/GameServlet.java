package controller;

import dao.ScoreDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.GameScore;
import model.Round;
import model.TrigQuestion;
import service.MathService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Flow:
 *  index.jsp --start--> game.jsp. On that page every question has its own "Run Seed Generator" button
 *  (POST action=seed&q=prime|mod|trig1|trig2, answered with JSON), which shows the final integer and the options.
 *  When all four questions are ready: --submit--> evaluate + save to DB --> result.jsp
 * GET ?page=play | result | leaderboard shows the matching page.
 */
@WebServlet("/game")
public class GameServlet extends HttpServlet {

    private static final int LIMIT = 1_000_000_000;      // keeps (input +- seed) inside the int range

    private final ScoreDAO dao = new ScoreDAO();
    private final Random random = new Random();
    private final Object lock = new Object();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String page = req.getParameter("page");
        HttpSession session = req.getSession();

        if ("play".equals(page) && session.getAttribute("playerName") != null) {
            req.getRequestDispatcher("/game.jsp").forward(req, resp);
        } else if ("result".equals(page) && session.getAttribute("result") != null) {
            req.getRequestDispatcher("/result.jsp").forward(req, resp);
        } else if ("leaderboard".equals(page)) {
            showLeaderboard(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        if ("start".equals(action)) {
            startGame(req, resp);
        } else if ("seed".equals(action)) {
            runSeed(req, resp);
        } else if ("submit".equals(action)) {
            submitAnswers(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
        }
    }

    // ---- Screen 1 -> Screen 2 (inputs) ----
    private void startGame(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("playerName");
        name = (name == null) ? "" : name.trim();
        if (name.isEmpty() || name.length() > 100) {
            req.setAttribute("error", "Please enter your name (1 to 100 characters).");
            req.getRequestDispatcher("/index.jsp").forward(req, resp);
            return;
        }
        HttpSession session = req.getSession();
        session.removeAttribute("result");
        session.removeAttribute("round");
        session.setAttribute("playerName", name);
        resp.sendRedirect(req.getContextPath() + "/game?page=play");
    }

    // ---- "Run Seed Generator" button of ONE question (called with fetch, answers with JSON) ----
    private void runSeed(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String json;
        try {
            json = seed(req);
        } catch (NumberFormatException | NullPointerException e) {
            json = error("Please fill every field of this question with valid numbers "
                    + "(integers must be between -1,000,000,000 and 1,000,000,000).");
        }
        resp.getWriter().write(json);
    }

    private String seed(HttpServletRequest req) {
        HttpSession session = req.getSession();
        if (session.getAttribute("playerName") == null) {
            return error("Your session has expired. Please go back to the home page and start again.");
        }
        String q = req.getParameter("q");
        if (q == null) {
            return error("Unknown question.");
        }
        Round round = currentRound(session);
        switch (q) {
            case "prime": return seedPrime(req, round);
            case "mod":   return seedModulus(req, round);
            case "trig1": return seedTrig(req, round, 1);
            case "trig2": return seedTrig(req, round, 2);
            default:      return error("Unknown question.");
        }
    }

    private Round currentRound(HttpSession session) {
        synchronized (lock) {
            Round r = (Round) session.getAttribute("round");
            if (r == null) {
                r = new Round();
                session.setAttribute("round", r);
            }
            return r;
        }
    }

    /** Q1: seed generator changes the player's integer; the player then chooses 1 (prime) or 0 (not prime). */
    private String seedPrime(HttpServletRequest req, Round round) {
        int input = parseInt(req.getParameter("primeInput"));
        int seed = MathService.randomSeed(random);
        round.setPrime(input, seed);
        int fin = round.getPrimeFinal();
        return ok("Your integer " + input + ", seed " + Round.signed(seed) + " \u2192 final integer " + fin,
                "Is " + fin + " a prime number?",
                option("1", "1 - Prime"), option("0", "0 - Not Prime"));
    }

    /** Q2: seed generator changes the dividend; options = the right remainder + 2 wrong ones. */
    private String seedModulus(HttpServletRequest req, Round round) {
        int dividend = parseInt(req.getParameter("dividend"));
        int divisor = parseInt(req.getParameter("divisor"));
        if (divisor == 0) {
            return error("Q2: the divisor must not be zero.");
        }
        int seed = MathService.randomSeed(random);
        int fin = dividend + seed;
        List<Integer> options = MathService.modOptions(MathService.calculateMod(fin, divisor), random);
        round.setModulus(dividend, seed, divisor, options);

        String[][] opts = new String[options.size()][];
        for (int i = 0; i < opts.length; i++) {
            opts[i] = option(String.valueOf(options.get(i)), String.valueOf(options.get(i)));
        }
        return ok("Dividend " + dividend + ", seed " + Round.signed(seed) + " \u2192 final dividend " + fin + ", divisor " + divisor,
                "What is " + fin + " % " + divisor + " ? (Java keeps the sign of the dividend.)", opts);
    }

    /** Q3 / Q4: seed generator changes the integer in front of sin/cos/tan. */
    private String seedTrig(HttpServletRequest req, Round round, int which) {
        String prefix = "trig" + which;
        int coef = parseInt(req.getParameter(prefix + "Coef"));
        String fn = parseFunction(req.getParameter(prefix + "Fn"));
        double angle = parseDecimal(req.getParameter(prefix + "Angle"));
        if (Double.isNaN(MathService.calculateTrig(fn, angle))) {
            return error("Q" + (which + 2) + ": " + fn + "(" + num(angle) + " degrees) is undefined. Please choose another angle.");
        }
        int seed = MathService.randomSeed(random);
        int fin = coef + seed;
        double correct = MathService.calculateTrigTerm(fin, fn, angle);
        List<String> options = MathService.trigOptions(correct, random);
        TrigQuestion tq = new TrigQuestion(coef, seed, fn, angle, options);
        if (which == 1) {
            round.setTrig1(tq);
        } else {
            round.setTrig2(tq);
        }

        String[][] opts = new String[options.size()][];
        for (int i = 0; i < opts.length; i++) {
            opts[i] = option(options.get(i), options.get(i));
        }
        return ok("Integer " + coef + ", seed " + Round.signed(seed) + " \u2192 final integer " + fin,
                "Find " + fin + " \u00d7 " + fn + "(" + tq.getAngleText() + "\u00b0)  (2 decimal places)", opts);
    }

    // ---- tiny JSON writer (our own text only; the player's text is never echoed back) ----
    private static String[] option(String value, String label) {
        return new String[] {value, label};
    }

    private static String ok(String calc, String question, String[]... options) {
        StringBuilder sb = new StringBuilder("{\"ok\":true,\"calc\":\"").append(esc(calc))
                .append("\",\"question\":\"").append(esc(question)).append("\",\"options\":[");
        for (int i = 0; i < options.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"value\":\"").append(esc(options[i][0])).append("\",\"label\":\"").append(esc(options[i][1])).append("\"}");
        }
        return sb.append("]}").toString();
    }

    private static String error(String message) {
        return "{\"ok\":false,\"error\":\"" + esc(message) + "\"}";
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // ---- Submit Answers -> result ----
    private void submitAnswers(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        Round round = (Round) session.getAttribute("round");
        String player = (String) session.getAttribute("playerName");
        if (player == null) {                                 // e.g. the form was submitted twice
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }
        if (round == null || !round.isComplete()) {
            backWithError(req, resp, "/game.jsp", "Please run the seed generator for all four questions and choose an answer for each.");
            return;
        }

        int primeAns, modAns;
        double trig1Ans, trig2Ans;
        try {
            primeAns = Integer.parseInt(req.getParameter("primeAnswer").trim());
            modAns = Integer.parseInt(req.getParameter("modAnswer").trim());
            trig1Ans = parseDecimal(req.getParameter("trig1Answer"));
            trig2Ans = parseDecimal(req.getParameter("trig2Answer"));
        } catch (NumberFormatException | NullPointerException e) {
            backWithError(req, resp, "/game.jsp", "Please run the seed generator and select one option for every question.");
            return;
        }

        TrigQuestion t1 = round.getTrig1();
        TrigQuestion t2 = round.getTrig2();

        // the correct answers are calculated NOW by the Java methods (they are not stored anywhere)
        int primeResult = MathService.isPrime(round.getPrimeFinal());
        int remainder = MathService.calculateMod(round.getDividendFinal(), round.getDivisor());
        double trig1 = MathService.calculateTrigTerm(t1.getCoefFinal(), t1.getFunction(), t1.getAngle());
        double trig2 = MathService.calculateTrigTerm(t2.getCoefFinal(), t2.getFunction(), t2.getAngle());

        boolean primeOk = MathService.checkPrime(round.getPrimeFinal(), primeAns);
        boolean modOk = MathService.checkMod(round.getDividendFinal(), round.getDivisor(), modAns);
        boolean trig1Ok = MathService.checkTrigTerm(t1.getCoefFinal(), t1.getFunction(), t1.getAngle(), trig1Ans);
        boolean trig2Ok = MathService.checkTrigTerm(t2.getCoefFinal(), t2.getFunction(), t2.getAngle(), trig2Ans);
        int total = MathService.calculateScore(primeOk, modOk, trig1Ok, trig2Ok);

        List<String> breakdown = new ArrayList<>();
        breakdown.add(line("Q1 Prime: " + round.getPrimeInput() + " " + round.getPrimeSeedText() + " = " + round.getPrimeFinal()
                + ", isPrime(" + round.getPrimeFinal() + ") = " + primeResult, String.valueOf(primeAns), primeOk, MathService.MARKS_PRIME));
        breakdown.add(line("Q2 Modulus: dividend " + round.getDividendInput() + " " + round.getDividendSeedText() + " = " + round.getDividendFinal()
                + ", " + round.getDividendFinal() + " % " + round.getDivisor() + " = " + remainder, String.valueOf(modAns), modOk, MathService.MARKS_MOD));
        breakdown.add(line(trigText("Q3", t1, trig1), MathService.formatTrig(trig1Ans), trig1Ok, MathService.MARKS_TRIG));
        breakdown.add(line(trigText("Q4", t2, trig2), MathService.formatTrig(trig2Ans), trig2Ok, MathService.MARKS_TRIG));

        GameScore gs = new GameScore(player, total, MathService.DIFFICULTY, new Timestamp(System.currentTimeMillis()));
        boolean saved = true;
        try {
            dao.insertScore(gs);
        } catch (SQLException e) {
            saved = false;
            log("Could not save score", e);
        }

        session.removeAttribute("round");                     // one game = one submission
        session.removeAttribute("playerName");
        session.setAttribute("result", gs);
        session.setAttribute("breakdown", breakdown);
        session.setAttribute("saved", saved);
        resp.sendRedirect(req.getContextPath() + "/game?page=result");   // PRG: refresh will not resubmit
    }

    // ---- Leaderboard ----
    private void showLeaderboard(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("scores", dao.findTop10());
        } catch (SQLException e) {
            log("Could not load leaderboard", e);
            req.setAttribute("error", "The leaderboard could not be loaded. Is MySQL running and is db.properties correct?");
        }
        req.getRequestDispatcher("/leaderboard.jsp").forward(req, resp);
    }

    // ---- helpers ----
    private void backWithError(HttpServletRequest req, HttpServletResponse resp, String page, String message)
            throws ServletException, IOException {
        req.setAttribute("error", message);
        req.getRequestDispatcher(page).forward(req, resp);
    }

    private static int parseInt(String s) {
        int v = Integer.parseInt(s.trim());
        if (Math.abs((long) v) > LIMIT) {
            throw new NumberFormatException("out of range");
        }
        return v;
    }

    private static double parseDecimal(String s) {
        double v = Double.parseDouble(s.trim().replace(',', '.'));
        if (!Double.isFinite(v)) {
            throw new NumberFormatException("not a finite number");
        }
        return v;
    }

    private static String parseFunction(String s) {
        String f = s.trim().toLowerCase(Locale.ROOT);
        if (!f.equals("sin") && !f.equals("cos") && !f.equals("tan")) {
            throw new NumberFormatException("unknown function");
        }
        return f;
    }

    /** 30.0 -> "30", 0.5 -> "0.5" */
    private static String num(double v) {
        return BigDecimal.valueOf(v).stripTrailingZeros().toPlainString();
    }

    private static String trigText(String q, TrigQuestion t, double value) {
        return q + " Trigonometry: " + t.getCoefInput() + " " + t.getSeedText() + " = " + t.getCoefFinal()
                + ", " + t.getCoefFinal() + " x " + t.getFunction() + "(" + t.getAngleText() + " degrees) = " + MathService.formatTrig(value);
    }

    private static String line(String calculation, String playerAnswer, boolean ok, int marks) {
        return calculation + "  |  Your answer: " + playerAnswer + "  |  " + (ok ? "Correct (+" + marks + ")" : "Wrong (0)");
    }
}
