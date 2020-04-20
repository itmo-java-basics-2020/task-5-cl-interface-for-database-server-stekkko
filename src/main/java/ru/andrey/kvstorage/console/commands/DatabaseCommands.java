package ru.andrey.kvstorage.console.commands;

import ru.andrey.kvstorage.console.DatabaseCommand;
import ru.andrey.kvstorage.console.ExecutionEnvironment;
import ru.andrey.kvstorage.utils.StringUtils;

import java.util.stream.Stream;

public enum DatabaseCommands {
    CREATE_DATABASE() {
        @Override
        public DatabaseCommand getCommand(ExecutionEnvironment environment, String... options) {
            return isOptionsCountCorrect(options.length)
                    ? new CreateDatabaseCommand(environment, options[1])
                    : new WrongCommand();
        }

        @Override
        public int getOptionsCount() {
            return 2;
        }
    },
    CREATE_TABLE() {
        @Override
        public DatabaseCommand getCommand(ExecutionEnvironment environment, String... options) {
            return isOptionsCountCorrect(options.length)
                    ? new CreateTableCommand(environment, options[1], options[2])
                    : new WrongCommand();
        }

        @Override
        public int getOptionsCount() {
            return 3;
        }
    },
    READ_KEY() {
        @Override
        public DatabaseCommand getCommand(ExecutionEnvironment environment, String... options) {
            return isOptionsCountCorrect(options.length)
                    ? new ReadKeyCommand(environment, options[1], options[2], options[3])
                    : new WrongCommand();
        }

        @Override
        protected int getOptionsCount() {
            return 4;
        }
    },
    UPDATE_KEY() {
        @Override
        public DatabaseCommand getCommand(ExecutionEnvironment environment, String... options) {
            return isOptionsCountCorrect(options.length)
                    ? new UpdateKeyCommand(environment, options[1], options[2], options[3], options[4])
                    : new WrongCommand();
        }

        @Override
        protected int getOptionsCount() {
            return 5;
        }
    },
    INVALID_INPUT() {
        @Override
        public DatabaseCommand getCommand(ExecutionEnvironment environment, String... options) {
            return new WrongCommand();
        }

        @Override
        protected int getOptionsCount() {
            return 0;
        }
    };

    public static DatabaseCommand of(
            ExecutionEnvironment environment,
            String input
    ) {
        if (StringUtils.isEmptyOrNull(input)) {
            return INVALID_INPUT.getCommand(environment, input);
        }
        var options = input.split(" ");
        if (Stream.of(DatabaseCommands.values())
                .noneMatch(s -> s.name().equals(options[0]))) {
            return INVALID_INPUT.getCommand(environment, input);
        }
        return DatabaseCommands.valueOf(options[0]).getCommand(environment, options);
    }

    public abstract DatabaseCommand getCommand(
            ExecutionEnvironment environment,
            String... options
    );

    protected abstract int getOptionsCount();

    protected boolean isOptionsCountCorrect(int optionsCount) {
        return getOptionsCount() == optionsCount;
    }

}