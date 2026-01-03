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

    public boolean removeTrainingPlan(int userId, int planId) {
        String query = "DELETE FROM training_plans WHERE user_id = ? AND id = ?";

        if (checkConnection())
            return false;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, planId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore delete training_plan: " + e.getMessage());
            return false;
        }
    }


    public TrainingPlan getTrainingPlanById(int planId){
        String query = "SELECT * FROM training_plans WHERE id = ?";

        if(checkConnection())
            return null;

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
        } catch (SQLException e) {
            System.err.println("Errore get training_plans: " + e.getMessage());
            return null;
        }
    }

    public void setAllTrainingPlansInactive(int userId) {
        String query = "UPDATE training_plans SET is_active = 0 WHERE user_id = ?";

        if (checkConnection())
            return;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore update all training_plans: " + e.getMessage());
        }
    }

    public boolean setTrainingPlanActive(int planId) {
        String query = "UPDATE training_plans SET is_active = 1 WHERE id = ?";

        if (checkConnection())
            return false;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, planId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore update training_plan active: " + e.getMessage());
            return false;
        }
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

    public boolean removeTrainingDay(int trainingDayId) {
        String query = "DELETE FROM training_days WHERE id = ?";

        if (checkConnection())
            return false;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, trainingDayId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore delete training_day: " + e.getMessage());
            return false;
        }
    }

    public int getTrainingDayOfWeek(int trainingDayId) {
        String query = "SELECT day_of_week FROM training_days WHERE id = ?";

        if (checkConnection())
            return -1;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, trainingDayId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next())
                return rs.getInt("day_of_week");

        } catch (SQLException e) {
            System.err.println("Errore getTrainingDayOfWeek: " + e.getMessage());
        }
        return -1;
    }
    //#endregion

    //#region user_exercises
    public boolean addUserExercise(int trainingDayId, String name, int sets, int reps, double weight, String notes) {
        String query = "INSERT INTO user_exercises (training_day_id, name, sets, reps, weight, notes) VALUES (?, ?, ?, ?, ?, ?)";

        if (checkConnection())
            return false;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, trainingDayId);
            stmt.setString(2, name);
            stmt.setInt(3, sets);
            stmt.setInt(4, reps);
            stmt.setDouble(5, weight);
            stmt.setString(6, notes);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore insert user_exercise: " + e.getMessage());
            return false;
        }
    }

    public List<UserExercise> getUserExercises(int trainingDayId) {
        String query = "SELECT * FROM user_exercises WHERE training_day_id = ?";
        List<UserExercise> exercises = new ArrayList<>();

        if (checkConnection())
            return exercises;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, trainingDayId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                exercises.add(new UserExercise(
                    rs.getInt("id"),
                    rs.getInt("training_day_id"),
                    rs.getString("name"),
                    rs.getInt("sets"),
                    rs.getInt("reps"),
                    rs.getDouble("weight"),
                    rs.getString("notes")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Errore select user_exercises: " + e.getMessage());
        }
        return exercises;
    }

    public boolean removeUserExercise(int exerciseId) {
        String query = "DELETE FROM user_exercises WHERE id = ?";

        if (checkConnection())
            return false;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, exerciseId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore delete user_exercise: " + e.getMessage());
            return false;
        }
    }
    //#endregion

    //#region workout_sessions
    public boolean addWorkoutSession(int trainingDayId) {
        String query = "INSERT INTO workout_sessions (training_day_id, execution_date, completed) VALUES (?, CURRENT_TIMESTAMP, false)";

        if (checkConnection())
            return false;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, trainingDayId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore insert workout_session: " + e.getMessage());
            return false;
        }
    }

    public List<WorkoutSession> getWorkoutSessions(int trainingDayId) {
        String query = " SELECT * FROM workout_sessions WHERE training_day_id = ? ORDER BY execution_date DESC";

        List<WorkoutSession> list = new ArrayList<>();

        if (checkConnection())
            return list;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, trainingDayId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new WorkoutSession(
                    rs.getInt("id"),
                    rs.getInt("training_day_id"),
                    rs.getTimestamp("execution_date").toLocalDateTime(),
                    rs.getBoolean("completed")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Errore select workout_sessions: " + e.getMessage());
        }

        return list;
    }

    public WorkoutSession getLastWorkoutSession(int trainingDayId) {
        String query = "SELECT * FROM workout_sessions WHERE training_day_id = ? ORDER BY execution_date DESC LIMIT 1 ";

        if (checkConnection())
            return null;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, trainingDayId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new WorkoutSession(
                    rs.getInt("id"),
                    rs.getInt("training_day_id"),
                    rs.getTimestamp("execution_date").toLocalDateTime(),
                    rs.getBoolean("completed")
                );
            }
        } catch (SQLException e) {
            System.err.println("Errore select last workout_session: " + e.getMessage());
        }

        return null;
    }

    public int countWorkoutsByPlan(int planId) {
        String query = " SELECT COUNT(ws.id) FROM workout_sessions ws JOIN training_days td ON ws.training_day_id = td.id WHERE td.plan_id = ?";

        if (checkConnection())
            return 0;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, planId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next())
                return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("Errore count workout_sessions: " + e.getMessage());
        }

        return 0;
    }

    public boolean completeWorkoutSession(int sessionId) {
        String query = "UPDATE workout_sessions SET completed = true WHERE id = ?";

        if (checkConnection())
            return false;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, sessionId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore update workout_session: " + e.getMessage());
            return false;
        }
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

    //#region helper: full plan
    public TrainingPlan getFullTrainingPlan(int trainingPlanId) {
        if (checkConnection())
            return null;

        try {
            TrainingPlan plan = getTrainingPlanById(trainingPlanId);

            if (plan == null)
                return null;

            List<TrainingDay> days = getTrainingDays(trainingPlanId);
            for (TrainingDay day : days) {
                List<UserExercise> exercises = getUserExercises(day.id);
                day.setExercises(exercises);
                plan.addTrainingDay(day);
            }
            return plan;
        } catch (Exception e) {
            System.err.println("Errore getFullTrainingPlan: " + e.getMessage());
            return null;
        }
    }
    //#endregion
}
