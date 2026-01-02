package DbModels;
import java.util.ArrayList;
import java.util.List;

public class TrainingDay {
    public int id;
    public int planId;
    public int dayOfWeek;
    public String focus;

    public List<DayExercise> dayExercises = new ArrayList<>();
    public List<Exercise> exercises = new ArrayList<>();

    public TrainingDay(int id, int planId, int dayOfWeek, String focus) {
        this.id = id;
        this.planId = planId;
        this.dayOfWeek = dayOfWeek;
        this.focus = focus;
    }

    public void addDayExercise(DayExercise de, Exercise ex) {
        this.dayExercises.add(de);
        this.exercises.add(ex);
    }
}