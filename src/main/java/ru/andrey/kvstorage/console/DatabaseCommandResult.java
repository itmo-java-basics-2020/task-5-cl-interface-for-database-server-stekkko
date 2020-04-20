package ru.andrey.kvstorage.console;

import java.util.Objects;
import java.util.Optional;

public interface DatabaseCommandResult {

    Optional<String> getResult();

    DatabaseCommandStatus getStatus();

    boolean isSuccess();

    String getErrorMessage();

    enum DatabaseCommandStatus {
        SUCCESS, FAILED
    }

    static DatabaseCommandResult success(String result) {
        return new DatabaseCommandResultImpl(result, null, DatabaseCommandResult.DatabaseCommandStatus.SUCCESS);
    }

    static DatabaseCommandResult error(String errorMessage) {
        return new DatabaseCommandResultImpl(null, errorMessage, DatabaseCommandResult.DatabaseCommandStatus.FAILED);
    }

}

final class DatabaseCommandResultImpl implements DatabaseCommandResult {
    private final String result;
    private final String errorMessage;
    private final DatabaseCommandResult.DatabaseCommandStatus status;

    public DatabaseCommandResultImpl(String result, String errorMessage, DatabaseCommandResult.DatabaseCommandStatus status) {
        this.result = result;
        this.errorMessage = errorMessage;
        this.status = status;
    }

    @Override
    public Optional<String> getResult() {
        return Optional.ofNullable(result);
    }

    @Override
    public DatabaseCommandStatus getStatus() {
        return status;
    }

    @Override
    public boolean isSuccess() {
        return status.equals(DatabaseCommandStatus.SUCCESS);
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatabaseCommandResultImpl that = (DatabaseCommandResultImpl) o;
        return Objects.equals(result, that.result) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, errorMessage, status);
    }

    @Override
    public String toString() {
        return "DatabaseCommandResultImpl{" +
                "result='" + result + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", status=" + status +
                '}';
    }
}