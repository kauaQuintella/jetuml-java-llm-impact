package jetuml.model.command;

import java.util.ArrayDeque;
import java.util.Deque;

public final class UndoManager {
    private final Deque<Command> undo = new ArrayDeque<>();
    private final Deque<Command> redo = new ArrayDeque<>();
    private int savedAt = 0;
    private int position = 0;

    public void run(Command c) {
        c.execute();
        undo.push(c);
        redo.clear();
        position++;
    }

    public boolean canUndo() {
        return !undo.isEmpty();
    }

    public void undo() {
        if (!canUndo()) {
            return;
        }
        Command c = undo.pop();
        c.undo();
        redo.push(c);
        position--;
    }

    public boolean canRedo() {
        return !redo.isEmpty();
    }

    public void redo() {
        if (!canRedo()) {
            return;
        }
        Command c = redo.pop();
        c.execute();
        undo.push(c);
        position++;
    }

    public void markSaved() {
        savedAt = position;
    }

    public boolean isModified() {
        return position != savedAt;
    }
}
