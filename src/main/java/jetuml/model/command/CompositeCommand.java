package jetuml.model.command;

import java.util.ArrayList;
import java.util.List;

public final class CompositeCommand implements Command {
    private final List<Command> commands;

    public CompositeCommand(List<Command> commands) {
        this.commands = List.copyOf(commands);
    }

    public CompositeCommand(Command... commands) {
        this.commands = List.of(commands);
    }

    @Override
    public void execute() {
        for (Command c : commands) {
            c.execute();
        }
    }

    @Override
    public void undo() {
        for (int i = commands.size() - 1; i >= 0; i--) {
            commands.get(i).undo();
        }
    }

    public List<Command> commands() {
        return commands;
    }
}
