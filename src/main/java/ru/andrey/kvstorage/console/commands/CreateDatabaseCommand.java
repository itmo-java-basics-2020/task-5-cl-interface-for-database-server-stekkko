package ru.andrey.kvstorage.console.commands;

import ru.andrey.kvstorage.console.DatabaseCommand;
import ru.andrey.kvstorage.console.DatabaseCommandResult;
import ru.andrey.kvstorage.console.ExecutionEnvironment;
import ru.andrey.kvstorage.logic.DatabaseImp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class CreateDatabaseCommand implements DatabaseCommand {
    private final ExecutionEnvironment environment;
    private final String databaseName;

    public CreateDatabaseCommand(ExecutionEnvironment environment, String databaseName) {
        this.environment = environment;
        this.databaseName = databaseName;
    }

    @Override
    public DatabaseCommandResult execute() {
        var path = Paths.get(databaseName);
        try {
            if (Files.exists(path)) {
                return DatabaseCommandResult.error("Database with name " + databaseName + " already exists.");
            } else {
                Files.createFile(path);
            }
            environment.addDatabase(new DatabaseImp(databaseName));
        } catch (IOException e) {
            return DatabaseCommandResult.error(e.getMessage());
        }

        return DatabaseCommandResult.success(null);
    }

}