# Doctor Appointment System

A JavaFX application for managing doctor appointments. This system allows patients to register, log in, and book appointments with doctors. Administrators can manage user accounts and appointments.

## Features

*   User registration and login for patients and administrators.
*   Patients can view available doctors and book appointments.
*   Patients can view their upcoming appointments.
*   Administrators can manage appointments (e.g., view all appointments, potentially cancel or modify them - *exact admin capabilities might need further clarification from code*).
*   Secure password storage using hashing and salting (as suggested by `db_schema.sql`).

## Technologies Used

*   **Programming Language:** Java (JDK 11 as per `build.gradle`)
*   **UI Framework:** JavaFX (version 17.0.2 as per `build.gradle`)
*   **Build Tool:** Gradle
*   **Backend as a Service (BaaS):** Supabase (for database and potentially authentication)
*   **Database:** PostgreSQL (managed by Supabase)

## Setup and Installation

### Prerequisites

*   **Java Development Kit (JDK):** Version 11 or higher. You can download it from [Oracle JDK](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) or [OpenJDK](https://openjdk.java.net/projects/jdk/11/).
*   **Gradle:** Ensure Gradle is installed and configured on your system. You can find installation instructions on the [official Gradle website](https://gradle.org/install/). The project uses the Gradle wrapper (`gradlew` or `gradlew.bat`), which should download the correct Gradle version automatically if you have an internet connection.
*   **JavaFX SDK:** The project is configured to use JavaFX version 17.0.2.
    *   The `build.gradle` file includes a `downloadJavaFX` task that attempts to download the necessary JavaFX JARs into a `lib` directory.
    *   Alternatively, you can download the JavaFX SDK (version 17.0.2 for Windows x64 is included in the `temp/javafx-sdk-21.0.2` directory, though the version in `build.gradle` is 17.0.2, which should be preferred for consistency) separately from the [JavaFX website](https://gluonhq.com/products/javafx/) and configure your IDE if needed. The `run-application.bat` script also refers to a local JavaFX SDK path.

### Database Setup

This project uses Supabase for its backend database (PostgreSQL).

1.  **Create a Supabase Project:** If you don't have one, create a new project on [Supabase](https://supabase.com/).
2.  **Database Schema:**
    *   The initial database schema can be found in `docs/db_schema.sql`. You can run this SQL script in your Supabase SQL editor to create the necessary tables (`users`, `appointments`).
    *   The `supabase/migrations/` directory contains incremental migrations that were applied during development. You might need to apply these in order if you are setting up a new Supabase project and want to match the exact schema evolution.
3.  **Environment Configuration:**
    *   The application will require Supabase project URL and API keys to connect to your Supabase instance.
    *   There is an `.env.example` file. Rename it to `.env` and fill in your Supabase credentials:
        ```
        SUPABASE_URL=YOUR_SUPABASE_URL
        SUPABASE_KEY=YOUR_SUPABASE_ANON_KEY
        ```
    *   The application code (likely in Java) will need to be configured to read these environment variables or have these values passed to it to connect to Supabase. (Actual implementation for reading .env in Java is not specified in the provided files, this is a general guidance).

## Building the Project

This project uses Gradle as its build tool.

1.  **Open a terminal or command prompt** in the root directory of the project.
2.  **Build the project** by running the Gradle wrapper script:

    *   On Windows:
        ```bash
        .\gradlew.bat build
        ```
    *   On macOS/Linux:
        ```bash
        ./gradlew build
        ```
    This command will compile the source code, process resources, and create a distributable version of the application (usually in the `build/libs` directory). The `downloadJavaFX` task should also run as part of the build if not already done, placing JavaFX JARs into the `lib/` directory.

## Running the Application

After building the project, you can run the application using Gradle or the provided batch scripts.

### Using Gradle

1.  **Open a terminal or command prompt** in the root directory of the project.
2.  **Run the application** using the Gradle wrapper script:

    *   On Windows:
        ```bash
        .\gradlew.bat run
        ```
    *   On macOS/Linux:
        ```bash
        ./gradlew run
        ```
    This command will execute the `main` method in the `DoctorAppointmentSystem.Main` class.

### Using Batch Scripts (Windows)

There are batch scripts provided for convenience on Windows:

*   **`run-application.bat`:** This script likely runs the compiled application. You might need to check its content to ensure it points to the correct JavaFX SDK path and main class if you encounter issues.
*   **`download-and-run.bat`:** This script seems to automate both downloading/setting up dependencies (possibly JavaFX) and running the application.

**Note on JavaFX SDK Path:**
The application requires the JavaFX SDK modules to be available at runtime. The `run` task in `build.gradle` should handle this. If running outside of Gradle (e.g., directly from an IDE or using the batch scripts), ensure that the JVM is launched with the correct `--module-path` pointing to your JavaFX SDK `lib` directory and the required `--add-modules` (e.g., `javafx.controls,javafx.fxml`). The `run-application.bat` script likely contains examples of these flags.
The `build.gradle` specifies `javafx.controls` and `javafx.fxml` as modules.

## Database Schema

The application relies on a PostgreSQL database, managed via Supabase. The main tables are:

*   **`users`**: Stores user information, including username, hashed password, salt, role (admin/patient), and email.
*   **`appointments`**: Stores appointment details, including patient name, appointment date, and a reference to the user who booked it (`user_id`).

For the complete and detailed schema, including column types, constraints, and relationships, please refer to the `docs/db_schema.sql` file.
The `supabase/migrations/` directory also contains SQL files showing the evolution of the schema.

## Contributing

Contributions to the Doctor Appointment System are welcome! If you'd like to contribute, please follow these general steps:

1.  **Fork the repository.**
2.  **Create a new branch** for your feature or bug fix: `git checkout -b feature-name` or `git checkout -b bugfix-name`.
3.  **Make your changes** and commit them with clear, descriptive messages.
4.  **Ensure your code builds and runs** correctly.
5.  **Push your changes** to your forked repository.
6.  **Create a Pull Request** to the main repository, detailing the changes you've made.

If you're planning a larger contribution, it's a good idea to open an issue first to discuss your ideas.
