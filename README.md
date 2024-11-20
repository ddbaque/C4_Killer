# C4_THE_GAME 

## Prerequisites

### Java

Ensure you have the JDK (Java Development Kit) installed. Verify the installation with the following command:

```bash
java -version
```

If not installed, you can install it using your package manager. For example:

```bash
sudo apt install default-jdk
```

### Apache Ant

You need **Apache Ant** installed. Follow these steps to install it:

1. Install Ant using your package manager:
   ```bash
   sudo apt install ant
   ```

2. Verify the installation:
   ```bash
   ant -version
   ```

## Build and Run the Project

1. Clone this repository or download the source code.
2. Navigate to the root directory of the project where the `build.xml` file is located.
3. Use the following command to compile and run the project:

   ```bash
   ant
   ant run
   ```

## Build Tasks in `build.xml`

The `build.xml` file includes the following main tasks:

- **`ant compile`**: Compiles the source code.
- **`ant clean`**: Cleans up generated files.
- **`ant run`**: Runs the application.

