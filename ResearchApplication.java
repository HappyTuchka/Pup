package models;

/**
 * Научная заявка. Балл - 20 * articles + 10 * talks, но не более 100.
 */
public class ResearchApplication extends ScholarshipApplication {

    private final int articles;
    private final int talks;

    public ResearchApplication(int id, int studentId, int articles, int talks) {
        super(id, studentId);
        if (articles < 0 || articles > 5) {
            throw new IllegalArgumentException("articles должен быть от 0 до 5: " + articles);
        }
        if (talks < 0 || talks > 10) {
            throw new IllegalArgumentException("talks должен быть от 0 до 10: " + talks);
        }
        this.articles = articles;
        this.talks = talks;
    }

    public int getArticles() {
        return articles;
    }

    public int getTalks() {
        return talks;
    }

    @Override
    public double calculateScore() {
        return Math.min(100, 20 * articles + 10 * talks);
    }
}
