package jetuml.ui.tools;

public final class ActionTool implements Tool {
    private final Runnable action;

    public ActionTool(Runnable action) {
        this.action = action;
    }

    @Override
    public void mousePressed(ToolContext c, MouseInput e) {
        action.run();
        c.requestSelectTool();
    }

    @Override
    public void mouseDragged(ToolContext c, MouseInput e) {
    }

    @Override
    public void mouseReleased(ToolContext c, MouseInput e) {
    }
}
