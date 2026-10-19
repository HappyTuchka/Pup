package models;

import interfaces.Scorable;

/**
 * Абстрактная заявка на стипендию.
 * ID заявки и ID студента после создания не изменяются.
 */
public abstract class ScholarshipApplication implements Scorable {

    private final int id;
    private final int studentId;
    private boolean awarded;

    public ScholarshipApplication(int id, int studentId) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID заявки должен быть положительным: " + id);
        }
        if (studentId <= 0) {
            throw new IllegalArgumentException("ID студента должен быть положительным: " + studentId);
        }
        this.id = id;
        this.studentId = studentId;
        this.awarded = false;
    }

    public int getId() {
        return id;
    }

    public int getStudentId() {
        return studentId;
    }

    public boolean isAwarded() {
        return awarded;
    }

    public void setAwarded(boolean awarded) {
        this.awarded = awarded;
    }

    @Override
    public abstract double calculateScore();

    /**
     * Сведения о заявке, её балле и результате назначения.
     */
    public String getDetails() {
        return String.format("%s{id=%d, studentId=%d, балл=%.1f, стипендия назначена: %s}",
                getClass().getSimpleName(), id, studentId, calculateScore(), awarded ? "да" : "нет");
    }
}
