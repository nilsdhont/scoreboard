package be.brigandze.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.json.bind.annotation.JsonbDateFormat;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchData {
    private int id;

    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss[XXX][X]")
    private LocalDateTime start_at;

    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss[XXX][X]")
    private LocalDateTime end_at;

    private Opponent opponent_left;
    private Opponent opponent_right;


}
