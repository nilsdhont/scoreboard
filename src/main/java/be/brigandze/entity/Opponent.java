package be.brigandze.entity;

import static be.brigandze.util.Utils.isNumeric;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public String getName() {
        return short_name != null && !short_name.isEmpty() ? short_name : full_name;
    }
}
