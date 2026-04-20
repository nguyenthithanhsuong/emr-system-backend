package com.eclinic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;


//Database cho ứng dụng. Lưu trữ dữ liệu tạm thời trong bộ nhớ.
//Có thể được thay bằng các Database siêu việt hơn như là MySQL, MongoDB,... mà không cần thay đổi code ở tầng trên.
public class Database {
    private static final String DATA_FILE_PATH = "data/eclinic-data.txt";
    private final AtomicInteger currentUserId = new AtomicInteger(0);
    private final List<User.UserRecord> users = Collections.synchronizedList(new ArrayList<User.UserRecord>());

    public Database() {
        loadFromDisk();
    }

    public int nextUserId() {
        return currentUserId.incrementAndGet();
    }

    public synchronized void addUserRecord(User.UserRecord userRecord) {
        users.add(userRecord);
        persist();
    }

    public List<User.UserRecord> getUsers() {
        return users;
    }

    private void loadFromDisk() {
        File dataFile = new File(DATA_FILE_PATH);
        if (!dataFile.exists()) {
            return;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }

                String[] parts = line.split("\\|", 6);
                if (parts.length < 2) {
                    continue;
                }

                String type = parts[0];
                if ("USER".equals(type) && parts.length >= 4) {
                    int id = parseInteger(parts[1]);
                    String fullName = decode(parts[2]);
                    String roleKey;
                    String role;
                    String userInformation;

                    if (parts.length >= 6) {
                        roleKey = decode(parts[3]);
                        role = decode(parts[4]);
                        userInformation = decode(parts[5]);
                    } else {
                        role = decode(parts[3]);
                        roleKey = role + " - User";
                        userInformation = "";
                    }

                    users.add(new User.UserRecord(id, fullName, roleKey, role, userInformation));
                    updateCurrentUserId(id);
                    continue;
                }
            }
        } catch (IOException ignored) {
            // If the file cannot be read, start with an empty in-memory snapshot.
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                    // Ignore close failure.
                }
            }
        }
    }

    private synchronized void persist() {
        File dataFile = new File(DATA_FILE_PATH);
        File parent = dataFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFile), "UTF-8"));
            for (User.UserRecord user : users) {
                writer.write("USER|"
                        + user.id()
                        + "|" + encode(user.fullName())
                        + "|" + encode(user.roleKey())
                        + "|" + encode(user.role())
                        + "|" + encode(user.userInformation()));
                writer.newLine();
            }
        } catch (IOException ignored) {
            // Persistence is best-effort for the demo app.
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                    // Ignore close failure.
                }
            }
        }
    }

    private void updateCurrentUserId(int id) {
        while (true) {
            int current = currentUserId.get();
            if (id <= current) {
                return;
            }
            if (currentUserId.compareAndSet(current, id)) {
                return;
            }
        }
    }

    private static int parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value == null ? "" : value, "UTF-8");
        } catch (Exception ex) {
            return "";
        }
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value == null ? "" : value, "UTF-8");
        } catch (Exception ex) {
            return value == null ? "" : value;
        }
    }

    public void printSnapshot() {
        System.out.println("Users:");
        for (User.UserRecord user : users) {
            System.out.println("- " + user);
        }
    }
}
