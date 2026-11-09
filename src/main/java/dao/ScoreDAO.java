package dao;

import model.GameScore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** All SQL of the application. Every query uses PreparedStatement. */
public class ScoreDAO {

    private static final String INSERT_SQL =
            "INSERT INTO game_scores (player_name, score, difficulty, played_at) VALUES (?, ?, ?, ?)";

    private static final String TOP10_SQL =
            "SELECT id, player_name, score, difficulty, played_at FROM game_scores "
          + "ORDER BY score DESC, played_at ASC LIMIT 10";

    public void insertScore(GameScore s) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_SQL)) {
            ps.setString(1, s.getPlayerName());
            ps.setInt(2, s.getScore());
            ps.setString(3, s.getDifficulty());
            ps.setTimestamp(4, s.getPlayedAt());
            ps.executeUpdate();
        }
    }

    public List<GameScore> findTop10() throws SQLException {
        List<GameScore> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(TOP10_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                GameScore g = new GameScore();
                g.setId(rs.getInt("id"));
                g.setPlayerName(rs.getString("player_name"));
                g.setScore(rs.getInt("score"));
                g.setDifficulty(rs.getString("difficulty"));
                g.setPlayedAt(rs.getTimestamp("played_at"));
                list.add(g);
            }
        }
        return list;
    }
}
