package DbModels;
import java.util.ArrayList;
import java.util.List;

public class TrainingPlan {
    public int id;
    public int userId;
    public String name;
    public boolean isActive;
    public List<TrainingDay> trainingDays = new ArrayList<>();

    public TrainingPlan(int id, int userId, String name, boolean isActive) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.isActive = isActive;
    }

    public void addTrainingDay(TrainingDay day) {
        this.trainingDays.add(day);
    }
}
