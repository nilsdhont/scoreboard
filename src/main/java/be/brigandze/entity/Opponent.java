package be.brigandze.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static be.brigandze.util.Utils.isNumeric;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Opponent {
    private String full_name;
    private String short_name;
    private int score;
    private int id;


    public void setScore(Object score) {
        if (score instanceof String) {
            String scoreString = (String) score;
            this.score = isNumeric(scoreString) ? Integer.parseInt(scoreString) : 0;
        }
    }
}
