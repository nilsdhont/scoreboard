package be.brigandze.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Opponent {
    private String full_name;
    private int score;
    private int id;


    public void setScore(Object score) {
        if(score instanceof Integer){
            this.score = (int) score;
        }else {
            this.score=0;
        }

    }
}
