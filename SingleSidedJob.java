package models;

public class SingleSidedJob extends PrintJob {
    public SingleSidedJob(int id, int ownerId, int pages, int copies, int priority) {
        super(id, ownerId, pages, copies, priority);
    }

    @Override
    public int getRequiredSheets() {
        return getPages() * getCopies();
    }

    @Override
    public String getDetails() {
        return String.format(
                "SingleSidedJob{id=%d, owner=%d, need=%d, pages=%d, copies=%d, priority=%d, status=%s}",
                getId(), getOwnerId(), getRequiredSheets(),
                getPages(), getCopies(), getPriority(),
                isPrinted() ? "PRINTED" : "WAITING");
    }
}