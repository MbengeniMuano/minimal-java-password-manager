package com.example.passwordmanager;

import java.io.Console;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Minimal Password Manager (Java + AES-GCM)\n");

        System.out.print("Vault file path [default vault.dat]: ");
        String pathInput = scanner.nextLine().trim();
        if (pathInput.isEmpty()) pathInput = "vault.dat";
        Path vaultPath = Paths.get(pathInput);

        char[] masterPassword = readPassword(scanner, "Master password: ");

        Vault vault = new Vault(vaultPath);

        try {
            if (!vault.exists()) {
                System.out.println("\nNo vault found. Creating new vault...");
                vault.create(masterPassword);
                System.out.println("Vault created at: " + vaultPath.toAbsolutePath());
            } else {
                vault.open(masterPassword);
                System.out.println("Vault opened: " + vaultPath.toAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Failed to open/create vault: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.print("\nCommand [add|list|get|exit]: ");
            String cmd = scanner.nextLine().trim().toLowerCase();
            try {
                switch (cmd) {
                    case "add":
                        System.out.print("Service: ");
                        String service = scanner.nextLine().trim();
                        System.out.print("Username: ");
                        String username = scanner.nextLine().trim();
                        char[] pwd = readPassword(scanner, "Password: ");
                        vault.addCredential(service, username, new String(pwd));
                        vault.save(masterPassword);
                        System.out.println("Saved.");
                        break;
                    case "list":
                        for (String s : vault.listServices()) {
                            System.out.println("- " + s);
                        }
                        break;
                    case "get":
                        System.out.print("Service: ");
                        String svc = scanner.nextLine().trim();
                        Credential c = vault.getCredential(svc);
                        if (c == null) {
                            System.out.println("Not found.");
                        } else {
                            System.out.println("Username: " + c.username);
                            System.out.println("Password: " + c.password);
                        }
                        break;
                    case "exit":
                        System.out.println("Bye.");
                        return;
                    default:
                        System.out.println("Unknown command.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private static char[] readPassword(Scanner scanner, String prompt) {
        Console console = System.console();
        if (console != null) {
            return console.readPassword(prompt);
        } else {
            System.out.print(prompt);
            return scanner.nextLine().toCharArray();
        }
    }
}