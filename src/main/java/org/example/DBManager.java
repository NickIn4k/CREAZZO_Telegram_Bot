package org.example;
import DbModels.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private static DBManager instance;
    private Connection connection;

    private DBManager() {
        try {
            String url = "jdbc:sqlite:Database/SportManager.db";
            connection = DriverManager.getConnection(url);
            System.out.println("Connessione al database SportManager.db");
        } catch (SQLException e) {
            System.err.println("Errore di connessione: " + e.getMessage());
        }
    }

    public static DBManager getInstance() {
        if (instance == null)
            instance = new DBManager();
        return instance;
    }

    private boolean checkConnection() {
        try {
            if (connection == null || !connection.isValid(5)) {
                System.err.println("Errore di connessione");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Errore di connessione: " + e.getMessage());
            return true;
        }
        return false;
    }

    //#region users
    public boolean addUser(long telegramId, String username, String firstName) {
        String query = "INSERT INTO users (telegram_id, username, first_name, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

        if (checkConnection())
            return false;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, telegramId);
            stmt.setString(2, username);
            stmt.setString(3, firstName);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore insert user: " + e.getMessage());
            return false;
        }
    }

    public User getUserByTelegramId(long telegramId) {
        String query = "SELECT * FROM users WHERE telegram_id = ?";

        if (checkConnection())
            return null;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, telegramId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getLong("telegram_id"),
                    rs.getString("username"),
                    rs.getString("first_name")
                );
            }
        } catch (SQLException e) {
            System.err.println("Errore select user: " + e.getMessage());
        }
        return null;
    }

    public boolean updateUserState(long telegramId, String state) {
        String query = "UPDATE user_state SET current_action = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = (SELECT id FROM users WHERE telegram_id = ?)";

        if (checkConnection())
            return false;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, state);
            stmt.setLong(2, telegramId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore update user_state: " + e.getMessage());
            return false;
        }
    }
    //#endregion

    //#region training_plans
    public boolean createTrainingPlan(int userId, String name, boolean isActive) {
        String query = "INSERT INTO training_plans (user_id, name, is_active, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

        if (checkConnection())
            return false;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, name);
            stmt.setBoolean(3, isActive);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore insert training_plan: " + e.getMessage());
            return false;
        }
    }

    public List<TrainingPlan> getTrainingPlans(int userId) {
        String query = "SELECT * FROM training_plans WHERE user_id = ?";
        List<TrainingPlan> plans = new ArrayList<>();

        if (checkConnection())
            return plans;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                plans.add(new TrainingPlan(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getBoolean("is_active")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Errore select training_plans: " + e.getMessage());
        }
        return plans;
    }
    //#endregion

    //#region exercises
    public boolean addExercise(String name, String muscleGroup, String description) {
        String query = "INSERT INTO exercises (name, muscle_group, description) VALUES (?, ?, ?)";

        if (checkConnection())
            return false;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, muscleGroup);
            stmt.setString(3, description);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore insert exercise: " + e.getMessage());
            return false;
        }
    }

    public List<Exercise> getExercisesByMuscle(String muscleGroup) {
        String query = "SELECT * FROM exercises WHERE muscle_group = ?";
        List<Exercise> exercises = new ArrayList<>();

        if (checkConnection())
            return exercises;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, muscleGroup);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                exercises.add(new Exercise(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("muscle_group"),
                    rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Errore select exercises: " + e.getMessage());
        }
        return exercises;
    }
    //#endregion

    //#region favorites
    public boolean addFavorite(int userId, String category, String value) {
        String query = "INSERT INTO favorites (user_id, category, value) VALUES (?, ?, ?)";

        if (checkConnection())
            return false;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, category);
            stmt.setString(3, value);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore insert favorite: " + e.getMessage());
            return false;
        }
    }

    public List<String> getFavorites(int userId, String category) {
        String query = "SELECT value FROM favorites WHERE user_id = ? AND category = ?";
        List<String> list = new ArrayList<>();

        if (checkConnection())
            return list;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, category);
            ResultSet rs = stmt.executeQuery();

            while (rs.next())
                list.add(rs.getString("value"));

        } catch (SQLException e) {
            System.err.println("Errore select favorites: " + e.getMessage());
        }
        return list;
    }
    //#endregion

    //#region workout_log
    public boolean addWorkoutLog(int trainingDayId, String executionDate, boolean completed) {
        String query = "INSERT INTO workout_log (training_day_id, execution_date, completed) VALUES (?, ?, ?)";

        if (checkConnection())
            return false;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, trainingDayId);
            stmt.setString(2, executionDate);
            stmt.setBoolean(3, completed);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore insert workout_log: " + e.getMessage());
            return false;
        }
    }

    public List<WorkoutLog> getWorkoutLogsByDay(int trainingDayId) {
        String query = "SELECT * FROM workout_log WHERE training_day_id = ?";
        List<WorkoutLog> logs = new ArrayList<>();

        if (checkConnection())
            return logs;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, trainingDayId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                logs.add(new WorkoutLog(
                    rs.getInt("id"),
                    rs.getInt("training_day_id"),
                    rs.getString("execution_date"),
                    rs.getBoolean("completed")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Errore select workout_log: " + e.getMessage());
        }
        return logs;
    }
    //#endregion

    //#region training_days
    public boolean addTrainingDay(int planId, int dayOfWeek, String focus) {
        String query = "INSERT INTO training_days (plan_id, day_of_week, focus) VALUES (?, ?, ?)";

        if (checkConnection())
            return false;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, planId);
            stmt.setInt(2, dayOfWeek);
            stmt.setString(3, focus);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore insert training_day: " + e.getMessage());
            return false;
        }
    }

    public List<TrainingDay> getTrainingDays(int planId) {
        String query = "SELECT * FROM training_days WHERE plan_id = ?";
        List<TrainingDay> days = new ArrayList<>();

        if (checkConnection())
            return days;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, planId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                days.add(new TrainingDay(
                    rs.getInt("id"),
                    rs.getInt("plan_id"),
                    rs.getInt("day_of_week"),
                    rs.getString("focus")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Errore select training_days: " + e.getMessage());
        }
        return days;
    }
    //#endregion

    //#region api_requests
    public boolean addApiRequest(int userId, String sport, String entity, String endpoint) {
        String query = "INSERT INTO api_requests (user_id, sport, entity, endpoint, requested_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

        if (checkConnection())
            return false;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, sport);
            stmt.setString(3, entity);
            stmt.setString(4, endpoint);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore insert api_requests: " + e.getMessage());
            return false;
        }
    }

    public List<ApiRequest> getApiRequestsByUser(int userId) {
        String query = "SELECT * FROM api_requests WHERE user_id = ?";
        List<ApiRequest> requests = new ArrayList<>();

        if (checkConnection())
            return requests;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                requests.add(new ApiRequest(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("sport"),
                    rs.getString("entity"),
                    rs.getString("endpoint"),
                    rs.getString("requested_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Errore select api_requests: " + e.getMessage());
        }
        return requests;
    }
    //#endregion

    //#region day_exercises
    public boolean addExerciseToDay(int trainingDayId, int exerciseId, int sets, int reps) {
        String query = "INSERT INTO day_exercises (training_day_id, exercise_id, sets, reps) VALUES (?, ?, ?, ?)";

        if (checkConnection())
            return false;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, trainingDayId);
            stmt.setInt(2, exerciseId);
            stmt.setInt(3, sets);
            stmt.setInt(4, reps);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore insert day_exercises: " + e.getMessage());
            return false;
        }
    }

    // Recupera tutti gli esercizi associati a un giorno
    public List<DayExercise> getExercisesForDay(int trainingDayId) {
        String query = "SELECT * FROM day_exercises WHERE training_day_id = ?";
        List<DayExercise> list = new ArrayList<>();

        if (checkConnection())
            return list;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, trainingDayId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new DayExercise(
                    rs.getInt("id"),
                    rs.getInt("training_day_id"),
                    rs.getInt("exercise_id"),
                    rs.getInt("sets"),
                    rs.getInt("reps")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Errore select day_exercises: " + e.getMessage());
        }
        return list;
    }

    public boolean removeExerciseFromDay(int dayExerciseId) {
        String query = "DELETE FROM day_exercises WHERE id = ?";

        if (checkConnection())
            return false;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, dayExerciseId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore delete day_exercises: " + e.getMessage());
            return false;
        }
    }
    //#endregion

    // Ritorna l'intera scheda di allenamento con giorni ed esercizi
    public TrainingPlan getFullTrainingPlan(int trainingPlanId) {
        if (checkConnection())
            return null;

        try {
            TrainingPlan plan = getTrainingPlanById(trainingPlanId);

            if (plan == null)
                return null;

            List<TrainingDay> days = getTrainingDays(trainingPlanId);
            for (TrainingDay day : days) {
                loadExercisesForDay(day);  // Tutti gli esercizi del giorno
                plan.addTrainingDay(day);
            }

            return plan;
        } catch (SQLException e) {
            System.err.println("Errore getFullTrainingPlan: " + e.getMessage());
            return null;
        }
    }

    //#region helper
    private TrainingPlan getTrainingPlanById(int planId) throws SQLException {
        String query = "SELECT * FROM training_plans WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, planId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next())
                return null;

            return new TrainingPlan(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("name"),
                rs.getBoolean("is_active")
            );
        }
    }

    private void loadExercisesForDay(TrainingDay day) throws SQLException {
        List<DayExercise> dayExercises = getExercisesForDay(day.id);

        for (DayExercise de : dayExercises) {
            Exercise ex = getExerciseById(de.exerciseId);
            if (ex != null)
                day.addDayExercise(de, ex);
        }
    }

    private Exercise getExerciseById(int exerciseId) throws SQLException {
        String query = "SELECT * FROM exercises WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, exerciseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Exercise(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("muscle_group"),
                    rs.getString("description")
                );
            }
        }
        return null;
    }
    //#endregion
}