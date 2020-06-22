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

}
