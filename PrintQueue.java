package logic;

import models.PrintJob;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PrintQueue<T extends PrintJob> {
    private final List<T> jobs = new ArrayList<>();
    private int paper;

    public PrintQueue(int paper) {
        if (paper < 0 || paper > 1_000_000)
            throw new IllegalArgumentException("paper: 0..1_000_000");
        this.paper = paper;
    }

    public boolean add(T job) {
        if (job == null) return false;
        if (job.isPrinted()) return false;
        for (T j : jobs) {
            if (j.getId() == job.getId()) return false;
        }
        jobs.add(job);
        return true;
    }

    public int getPaper() { return paper; }

    public boolean restock(int amount) {
        if (amount <= 0) return false;
        long total = (long) paper + amount;
        if (total > 1_000_000) return false;
        paper = (int) total;
        return true;
    }

    public Stream<T> getAnalyticsStream() {
        return jobs.stream();
    }

    public List<T> process(Predicate<T> condition,
                           Comparator<T> order,
                           Consumer<T> action) {
        List<T> waiting = new ArrayList<>();
        for (T j : jobs) if (!j.isPrinted()) waiting.add(j);
        waiting.sort(order);

        List<T> done = new ArrayList<>();
        for (T j : waiting) {
            if (!condition.test(j)) continue;   // условие на момент рассмотрения
            int need = j.getRequiredSheets();
            if (need > paper) continue;         // не хватает бумаги — пропускаем
            paper -= need;
            action.accept(j);                   // action помечает printed
            done.add(j);
        }
        return done;
    }

    public List<T> getJobs() {
        return Collections.unmodifiableList(jobs);
    }
}