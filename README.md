# Minimal Java Password Manager

A small command-line password manager written in Java. Credentials are stored in an encrypted vault on disk using AES‑GCM with PBKDF2 key derivation.

## Features
- Encrypted vault bound to a master password
- Add, list, and retrieve credentials (service, username, password)
- Modern cryptography primitives (AES‑GCM, PBKDF2WithHmacSHA256)

## Requirements
- Java 8+ (tested on recent JDKs)
- No external dependencies

## Build
Compile the sources to `out`:
```bash
javac -d out src/com/example/passwordmanager/*.java
```

Create an optional JAR:
```bash
jar --create --file password-manager.jar -C out .
```

## Run
Run the CLI:
```bash
java -cp out com.example.passwordmanager.App
```
Or with the JAR:
```bash
java -cp password-manager.jar com.example.passwordmanager.App
```

On first run, you will be prompted for a vault file path (press Enter for the default `vault.dat`) and a master password. Use the following commands inside the application:
- `add` — add a credential
- `list` — list service names
- `get` — display username and password for a service
- `exit` — quit

## Security
- AES‑GCM with a random 12‑byte IV per encryption and a 128‑bit authentication tag
- PBKDF2WithHmacSHA256 with 120,000 iterations to derive a 256‑bit key from the master password
- The vault stores a JSON payload that is encrypted as a whole

Note: This project is intentionally minimal. For production scenarios, implement secure input handling, robust JSON parsing, and secret hygiene.

## Project Structure
- `src/com/example/passwordmanager/App.java` — CLI entry point
- `src/com/example/passwordmanager/Vault.java` — vault persistence and JSON serialization
- `src/com/example/passwordmanager/Crypto.java` — encryption and key derivation utilities
- `src/com/example/passwordmanager/Credential.java` — credential model

## License
MIT