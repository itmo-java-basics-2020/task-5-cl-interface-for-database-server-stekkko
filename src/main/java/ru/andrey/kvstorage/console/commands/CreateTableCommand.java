package ru.andrey.kvstorage.console.commands;

import ru.andrey.kvstorage.console.DatabaseCommand;
import ru.andrey.kvstorage.console.DatabaseCommandResult;
import ru.andrey.kvstorage.console.ExecutionEnvironment;
import ru.andrey.kvstorage.exception.DatabaseException;
import ru.andrey.kvstorage.logic.DatabaseImp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public final class CreateTableCommand implements DatabaseCommand {
    private final ExecutionEnvironment environment;
    private final String databaseName;
    private final String tableName;

    public CreateTableCommand(ExecutionEnvironment environment, String databaseName, String tableName) {
        this.environment = environment;
        this.databaseName = databaseName;
        this.tableName = tableName;
    }


    @Override
    public DatabaseCommandResult execute() {
        var database = environment.getDatabase(databaseName);
        if (database.isEmpty()) {
            var path = Paths.get(databaseName);
            try {
                if (Files.exists(path)) {
                    database = Optional.of(new DatabaseImp(databaseName));
                    environment.addDatabase(database.get());
                }
            } catch (IOException e) {
                return DatabaseCommandResult.error(e.getMessage());
            }
        }

        if (database.isPresent()) {
            try {
                database.get().createTableIfNotExists(tableName);
            } catch (DatabaseException e) {
                return DatabaseCommandResult.error(e.getMessage());
            }
        } else {
            return DatabaseCommandResult.error("Couldn't create database " +
                    "object for database name " + databaseName + ".");
        }
        return DatabaseCommandResult.success(null);
    }

}