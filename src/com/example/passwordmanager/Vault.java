package com.example.passwordmanager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Vault {
    private final Path path;
    private final Map<String, Credential> store = new LinkedHashMap<>();
    private byte[] salt;

    public Vault(Path path) { this.path = path; }

    public boolean exists() { return Files.exists(path); }

    public void create(char[] password) throws Exception {
        this.salt = Crypto.randomBytes(Crypto.saltLength());
        save(password);
    }

    public void open(char[] password) throws Exception {
        byte[] blob = Files.readAllBytes(path);
        byte[] jsonBytes = Crypto.decrypt(blob, password);
        String json = new String(jsonBytes, StandardCharsets.UTF_8);
        parseJson(json);
        // extract salt from blob header
        this.salt = Arrays.copyOfRange(blob, 1, 1 + Crypto.saltLength());
    }

    public void save(char[] password) throws Exception {
        String json = toJson();
        byte[] plaintext = json.getBytes(StandardCharsets.UTF_8);
        if (salt == null) salt = Crypto.randomBytes(Crypto.saltLength());
        byte[] blob = Crypto.encrypt(plaintext, password, salt);
        Files.write(path, blob);
    }

    public void addCredential(String service, String username, String password) {
        store.put(service, new Credential(service, username, password));
    }

    public Credential getCredential(String service) {
        return store.get(service);
    }

    public List<String> listServices() {
        return new ArrayList<>(store.keySet());
    }

    private void parseJson(String json) {
        store.clear();
        if (json == null || json.isEmpty()) return;
        // Minimal JSON parsing for: {"items":[{"service":"...","username":"...","password":"..."}]}
        int itemsStart = json.indexOf("[", json.indexOf("\"items\""));
        int itemsEnd = json.indexOf("]", itemsStart);
        if (itemsStart < 0 || itemsEnd < 0) return;
        String items = json.substring(itemsStart + 1, itemsEnd);
        String[] entries = items.split("},");
        for (String entry : entries) {
            String e = entry.replace("{", "").replace("}", "");
            String service = extract(e, "service");
            String username = extract(e, "username");
            String password = extract(e, "password");
            if (service != null) {
                store.put(service, new Credential(service, username, password));
            }
        }
    }

    private String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"items\":[");
        boolean first = true;
        for (Credential c : store.values()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{\"service\":\"").append(escape(c.service)).append("\",")
              .append("\"username\":\"").append(escape(c.username)).append("\",")
              .append("\"password\":\"").append(escape(c.password)).append("\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String extract(String src, String key) {
        String pat = "\"" + key + "\":\"";
        int i = src.indexOf(pat);
        if (i < 0) return null;
        int start = i + pat.length();
        int end = src.indexOf("\"", start);
        if (end < 0) return null;
        return unescape(src.substring(start, end));
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String unescape(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}