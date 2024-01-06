package bitbybit.docker;

import java.util.ArrayList;
import java.util.List;

public class Logger {
    private static final List<String> logMessages = new ArrayList<>();
    private static  String latestActionLogs;

    public static void log(String message) {
        System.out.println(message);
        SystemLogFrame sf = new SystemLogFrame(message);
        sf.setVisible(true);
        logMessages.add(message);
        latestActionLogs = message;

    }

    public static String getLatestActionLogs() {
        return latestActionLogs;
    }
}
