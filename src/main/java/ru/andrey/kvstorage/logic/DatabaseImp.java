package ru.andrey.kvstorage.logic;

import ru.andrey.kvstorage.exception.DatabaseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class DatabaseImp implements Database {
    private final static char SEPARATOR = '|';

    private final Map<String, TableBounds> tables;
    private final Path path;

    public DatabaseImp(String name) throws IOException {
        this.path = Paths.get(name);
        this.tables = new HashMap<>();

        indexTables();
    }

    @Override
    public String getName() {
        return path.getFileName().toString();
    }

    @Override
    public void createTableIfNotExists(String tableName) throws DatabaseException {
        if (tables.containsKey(tableName)) {
            throw new DatabaseException("Table with name " + tableName + " already exist.");
        }

        var line = (System.lineSeparator() + SEPARATOR + tableName
                + SEPARATOR + System.lineSeparator()).getBytes();
        try {
            Files.write(path, line, StandardOpenOption.APPEND);

            var linesCount = Files.lines(path).count();
            tables.put(tableName, new TableBounds(linesCount, linesCount));
        } catch (IOException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    private boolean isPairOfKey(String objectKey, String line) {
        if (objectKey.length() >= line.length()) {
            return false;
        }
        int i;
        for (i = 0; i < objectKey.length(); i++) {
            if (line.charAt(i) != objectKey.charAt(i)) {
                return false;
            }
        }
        return line.charAt(i) == SEPARATOR;
    }

    @Override
    public void write(String tableName, String objectKey, String objectValue) throws DatabaseException {
        if (!tables.containsKey(tableName)) {
            return;
        }
        var tableBounds = tables.get(tableName);
        var newLine = (objectKey + SEPARATOR + objectValue);
        try {
            var lines = Files.readAllLines(path);
            AtomicBoolean isFound = new AtomicBoolean();
            AtomicLong lineNumberToChange = new AtomicLong(tableBounds.getStart());
            lines.stream()
                    .skip(tableBounds.getStart())
                    .limit(tableBounds.getEnd() - tableBounds.getStart())
                    .forEach(s -> {
                        if (isPairOfKey(objectKey, s)) {
                            isFound.set(true);
                        }
                        if (!isFound.get()) {
                            lineNumberToChange.getAndIncrement();
                        }
                    });
            if (lineNumberToChange.get() >= lines.size()) {
                lines.add(newLine);
            } else {
                lines.set((int) lineNumberToChange.get(), newLine + (isFound.get() ? "" : System.lineSeparator()));
            }
            Files.write(path, lines);
            updateIndices(lineNumberToChange.get());
        } catch (IOException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public String read(String tableName, String objectKey) throws DatabaseException {
        if (!tables.containsKey(tableName)) {
            return null;
        }

        var tableBounds = tables.get(tableName);
        try {
            var pair = Files.lines(path)
                    .skip(tableBounds.getStart())
                    .limit(tableBounds.getEnd() - tableBounds.getStart())
                    .filter(s -> isPairOfKey(objectKey, s))
                    .findFirst()
                    .orElse(null);
            if (pair == null) {
                return null;
            }
            return pair.substring(objectKey.length() + 1);
        } catch (IOException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    private void indexTables() throws IOException {
        var lineCounter = new AtomicLong();
        var lastTableStartLine = new AtomicLong();
        var lastTableName = new AtomicReference<String>();

        Files.lines(path)
                .forEach(s -> {
                    if (s.isEmpty()) {
                        if (lastTableName.get() != null) {
                            tables.put(
                                    lastTableName.get(),
                                    new TableBounds(lastTableStartLine.getAndSet(0L), lineCounter.get())
                            );
                            lastTableName.set(null);
                        }
                    } else if (s.charAt(0) == SEPARATOR) {
                        lastTableName.set(s.substring(1, s.length() - 1));
                        lastTableStartLine.set(lineCounter.get() + 1);
                    }

                    lineCounter.getAndIncrement();
                });
        if (lastTableName.get() != null) {
            tables.put(
                    lastTableName.get(),
                    new TableBounds(lastTableStartLine.getAndSet(0L), lineCounter.get())
            );
        }
    }

    private void updateIndices(long fromIndex) {
        boolean isAppendedToTable = false;
        for (var pairs : tables.entrySet()) {
            if (pairs.getValue().getEnd() == fromIndex) {
                isAppendedToTable = true;
                tables.put(
                        pairs.getKey(),
                        new TableBounds(
                                pairs.getValue().getStart(),
                                pairs.getValue().getEnd() + 1
                        )
                );
            }
        }
        if (isAppendedToTable) {
            for (var pairs : tables.entrySet()) {
                if (pairs.getValue().getEnd() > fromIndex + 1) {
                    tables.put(
                            pairs.getKey(),
                            new TableBounds(
                                    pairs.getValue().getStart() + 1,
                                    pairs.getValue().getEnd() + 1
                            )
                    );
                }
            }
        }
    }

    @Override
    public void createTableIfNotExists(String tableName, int segmentSizeInBytes) throws DatabaseException { }

    private final static class TableBounds {
        private final long start;
        private final long end;

        private TableBounds(long start, long end) {
            this.start = start;
            this.end = end;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TableBounds that = (TableBounds) o;
            return start == that.start &&
                    end == that.end;
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, end);
        }

        @Override
        public String toString() {
            return "TableBounds{" +
                    "start=" + start +
                    ", end=" + end +
                    '}';
        }
    }

}