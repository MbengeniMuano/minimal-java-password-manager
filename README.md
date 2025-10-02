# Minimal Java Password Manager

Tech Stack: Java, File I/O, AES-GCM encryption.

Features:
- Create a vault file encrypted with a master password
- Add, list, and get credentials (service, username, password)
- Uses AES-GCM with PBKDF2 key derivation

## Build & Run

### Using `javac`
```bash
javac -d out src/com/example/passwordmanager/*.java
java -cp out com.example.passwordmanager.App
```

### Using `jar`
```bash
javac -d out src/com/example/passwordmanager/*.java
jar --create --file password-manager.jar -C out .
java -cp password-manager.jar com.example.passwordmanager.App
```

## Usage

On first run, you'll be prompted to create or open a vault file.

Commands inside the app:
- `add` to add a credential
- `list` to list services
- `get` to retrieve username/password for a service
- `exit` to quit

Vault file is stored at `vault.dat` in the working directory by default.

## Security Notes
- AES-GCM with random 12-byte IV per encryption
- PBKDF2WithHmacSHA256 with 120,000 iterations for key derivation
- Credentials are stored as JSON and encrypted as a whole
- This is a minimal demo; for production use, consider secure input handling and better secret management