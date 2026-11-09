package model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

/** One row of the game_scores table. */
public class GameScore implements Serializable {
    private int id;
    private String playerName;
    private int score;
    private String difficulty;
    private Timestamp playedAt;

    public GameScore() { }

    public GameScore(String playerName, int score, String difficulty, Timestamp playedAt) {
        this.playerName = playerName;
        this.score = score;
        this.difficulty = difficulty;
        this.playedAt = playedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public Timestamp getPlayedAt() { return playedAt; }
    public void setPlayedAt(Timestamp playedAt) { this.playedAt = playedAt; }

    /** Date/time as text for the pages, e.g. 2026-11-09 14:30:05 */
    public String getPlayedAtText() {
        return playedAt == null ? "" : playedAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
