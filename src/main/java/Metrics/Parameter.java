package Metrics;

import java.io.FileWriter;
import java.io.IOException;

public class Parameter {
    private final String name;
    private final int start;
    private final int end;
    private final int step;

    public Parameter(String name, int start, int end, int step) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.step = step;
    }

    public String getName() {
        return name;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getStep() {
        return step;
    }

    public boolean isChanging() {
        return step != 0;
    }

    public void writeTo(FileWriter writer) throws IOException {
        writer.append("Parameter: ");
        writer.append(name);
        writer.append("\nStart: ");
        writer.append(Integer.toString(start));
        writer.append("\nEnd: ");
        writer.append(Integer.toString(end));
        writer.append("\nStep: ");
        writer.append(Integer.toString(step));
        writer.append("\n");
    }
}
