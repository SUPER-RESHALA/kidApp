package com.example.kidapp.log;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileLogger  {

    private static final String LOG_TAG = "FileLogger";
    private static final String LOG_FILE_NAME = "app_logs.txt";
    private static File logFile;

    public static void init(Context context) {
        File logDir = new File(context.getFilesDir(), "logs");
        if (!logDir.exists()) {
            logDir.mkdirs();  // Создание директории для логов
        }
        logFile = new File(logDir, LOG_FILE_NAME);
    }

    public static void log(String tag, String message) {
        logToFile(tag, message);
        Log.d(tag, message);  // Параллельно выводим в консоль
    }

    public static void logError(String tag, String message) {
        logToFile(tag, "ERROR: " + message);
        Log.e(tag, message);
    }

    private static void logToFile(String tag, String message) {
        if (logFile == null) {
            Log.e(LOG_TAG, "Logger not initialized. Call FileLogger.init(context) first.");
            return;
        }

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String logMessage = timeStamp + " [" + tag + "] " + message + "\n";

        try (FileWriter writer = new FileWriter(logFile, true)) {  // true = append mode
            writer.append(logMessage);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to write log to file", e);
        }
    }

    // Метод для получения пути к файлу логов (например, для отправки на сервер)
    public static String getLogFilePath() {
        return logFile != null ? logFile.getAbsolutePath() : null;
    }
}
