package ru.andrey.kvstorage.console;

import ru.andrey.kvstorage.logic.Database;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultExecutionEnvironment implements ExecutionEnvironment{
    private final Map<String, Database> databases;

    public DefaultExecutionEnvironment() {
        databases = new HashMap<>();
    }

    @Override
    public Optional<Database> getDatabase(String name) {
        return Optional.ofNullable(databases.get(name));
    }

    @Override
    public void addDatabase(Database db) {
        databases.put(db.getName(), db);
    }
}
