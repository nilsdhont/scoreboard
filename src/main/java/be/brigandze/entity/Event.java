package be.brigandze.entity;

import static be.brigandze.util.Utils.isNotNullString;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.json.bind.annotation.JsonbDateFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    private int id;

    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss[XXX][X]")
    private LocalDateTime start_at;

    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss[XXX][X]")
    private LocalDateTime end_at;

    private Opponent opponent_left;
    private Opponent opponent_right;

    private Links _links;


    public void setEnd_at(Object end_at) {
        if (end_at != null && isNotNullString(end_at)) {
            this.end_at = LocalDateTime.parse((String) end_at,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[XXX][X]"));
//            this.end_at = (LocalDateTime) end_at;
        }
    }

//    public void setOpponent_left(Object opponent_left) {
//        if (opponent_left != null && isNotNullString(opponent_left)) {
//            this.opponent_left = (Opponent) opponent_left;
//        }
//    }
//
//    public void setOpponent_right(Object opponent_right) {
//        if (opponent_right != null && isNotNullString(opponent_right)) {
//            this.opponent_right = (Opponent) opponent_right;
//        }
//    }
}
