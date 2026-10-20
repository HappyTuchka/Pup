package models;

public class DoubleSidedJob extends PrintJob {
    public DoubleSidedJob(int id, int ownerId, int pages, int copies, int priority) {
        super(id, ownerId, pages, copies, priority);
    }

    @Override
    public int getRequiredSheets() {
        // на каждый экземпляр — половина pages, округлённая вверх
        return ((getPages() + 1) / 2) * getCopies();
    }

    @Override
    public String getDetails() {
        return String.format(
                "DoubleSidedJob{id=%d, owner=%d, need=%d, pages=%d, copies=%d, priority=%d, status=%s}",
                getId(), getOwnerId(), getRequiredSheets(),
                getPages(), getCopies(), getPriority(),
                isPrinted() ? "PRINTED" : "WAITING");
    }
}