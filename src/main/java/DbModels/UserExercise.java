package DbModels;

public class UserExercise {
    public int id;
    public int trainingDayId;
    public String name;
    public int sets;
    public int reps;
    public double weight;
    public String notes;

    public UserExercise(int id, int trainingDayId, String name, int sets, int reps, double weight, String notes) {
        this.id = id;
        this.trainingDayId = trainingDayId;
        this.name = name;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
        this.notes = notes;
    }

    @Override
    public String toString() {
        String msg = "<b>%s</b> – %d x %d".formatted(name, sets, reps);

        if (weight > 0)
            msg = msg.concat(" – %.1f kg".formatted(weight));

        if (notes != null && !notes.isEmpty())
            msg = msg.concat(" (%s)".formatted(notes));

        return msg;
    }

}
