package models;

/**
 * Учебная заявка. Балл - среднее арифметическое двух оценок.
 */
public class AcademicApplication extends ScholarshipApplication {

    private final int exam1;
    private final int exam2;

    public AcademicApplication(int id, int studentId, int exam1, int exam2) {
        super(id, studentId);
        checkExam(exam1, "exam1");
        checkExam(exam2, "exam2");
        this.exam1 = exam1;
        this.exam2 = exam2;
    }

    private static void checkExam(int value, String name) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException(name + " должен быть от 0 до 100: " + value);
        }
    }

    public int getExam1() {
        return exam1;
    }

    public int getExam2() {
        return exam2;
    }

    @Override
    public double calculateScore() {
        return (exam1 + exam2) / 2.0;
    }
}
