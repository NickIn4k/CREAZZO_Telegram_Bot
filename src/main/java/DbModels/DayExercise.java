package DbModels;

public class DayExercise {
    public int id;
    public int trainingDayId;
    public int exerciseId;
    public int sets;
    public int reps;

    public DayExercise(int id, int trainingDayId, int exerciseId, int sets, int reps) {
        this.id = id;
        this.trainingDayId = trainingDayId;
        this.exerciseId = exerciseId;
        this.sets = sets;
        this.reps = reps;
    }
}

