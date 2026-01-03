package DbModels;

import java.util.ArrayList;
import java.util.List;

public class TrainingDay {
    public int id;
    public int planId;
    public int dayOfWeek; // 1 = Lunedì, 7 = Domenica
    public String focus;

    private List<UserExercise> exercises;

    public TrainingDay(int id, int planId, int dayOfWeek, String focus) {
        this.id = id;
        this.planId = planId;
        this.dayOfWeek = dayOfWeek;
        this.focus = focus;
        this.exercises = new ArrayList<>();
    }

    public void setExercises(List<UserExercise> exercises) {
        this.exercises = exercises;
    }

    public List<UserExercise> getExercises() {
        return exercises;
    }

    @Override
    public String toString() {
        String msg = "🗓️ <b>Giorno %d – Focus: %s</b>\n\n".formatted(dayOfWeek, focus);

        if (exercises.isEmpty())
            msg = msg.concat(" ⚠️ Nessun esercizio definito\n");
        else {
            int i = 1;
            for (UserExercise ex : exercises) {
                msg = msg.concat("ℹ️  Esercizio <b>" + i + "</b>:\n" + ex.toString() + "\n");
                i++;
            }
        }

        return msg;
    }

}
