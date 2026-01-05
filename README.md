# 📝 Note Sharing Platform API

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue)
![AWS S3](https://img.shields.io/badge/AWS-S3-orange)

A robust RESTful API built with Spring Boot designed for uploading, sharing, and managing digital notes. The application features secure authentication (OAuth2 & JWT), cloud file storage via AWS S3, and email notifications.

## 🌟 Key Features

* **Secure Authentication:**
    * Local Registration/Login (Email & Password) with BCrypt encryption.
    * **OAuth2 Integration:** Login support for Google and GitHub.
    * Stateless **JWT (JSON Web Token)** authorization.
* **Note Management:**
    * Upload notes (PDF, Text, Images) directly to **AWS S3**.
    * Secure file retrieval via **S3 Presigned URLs**.
    * Update and Delete functionality with ownership validation.
    * Search and filter notes by Title, Subject, or Uploader.
* **User Engagement:**
    * **Bookmark System:** Save notes for quick access later.
    * User Profiles: View uploaded notes and bookmark history.
* **Notifications:**
    * Automated email notifications for registration and successful uploads.
* **Developer Friendly:**
    * Integrated **Swagger UI / OpenAPI** documentation.
    * Includes a basic HTML/JS frontend for API testing.

## 🛠️ Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.5.4
* **Database:** PostgreSQL
* **Cloud Storage:** AWS S3 (via AWS SDK 2.x)
* **Security:** Spring Security 6, OAuth2 Client, JJWT
* **Documentation:** SpringDoc OpenAPI (Swagger)
* **Build Tool:** Maven

## ⚙️ Configuration & Prerequisites

Before running the application, ensure you have the following installed:
* Java Development Kit (JDK) 21
* PostgreSQL
* An AWS Account (with S3 access)
* Google/GitHub Developer credentials (for OAuth2)

### Environment Variables
The `application.properties` file uses environment variables for sensitive data. You must set these in your IDE or system environment, or create a `application-local.properties` file.

| Variable | Description |
| :--- | :--- |
| `DB_USERNAME` | Your PostgreSQL username |
| `DB_PASSWORD` | Your PostgreSQL password |
| `JWT_SECRET_KEY` | A strong base64 encoded secret key for signing tokens |
| `AWS_SECRET_KEY_ID` | AWS IAM Access Key ID |
| `AWS_SECRET_KEY` | AWS IAM Secret Access Key |
| `GITHUB_CLIENT_ID` | GitHub OAuth2 Client ID |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth2 Client Secret |
| `GOOGLE_CLIENT_ID` | Google OAuth2 Client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 Client Secret |
| `YOUR_EMAIL` | Sender email address for notifications (SMTP) |
| `YOUR_EMAIL_PASSWORD` | App Password (if using Gmail) or SMTP password |

> **Note:** The application defaults to AWS Region `eu-north-1` and bucket `sian-noteshare-bucket`. Modify `src/main/resources/application.properties` if your bucket is in a different region.

## 🚀 Getting Started

1.  **Clone the repository**
    ```bash
    git clone [https://github.com/sianshrestha/note-sharing-app.git](https://github.com/sianshrestha/note-sharing-app.git)
    cd note-sharing-app
    ```

2.  **Configure Database**
    Create a PostgreSQL database named `note_sharing_app`.
    ```sql
    CREATE DATABASE note_sharing_app;
    ```

3.  **Build the project**
    ```bash
    ./mvnw clean install
    ```

4.  **Run the application**
    ```bash
    ./mvnw spring-boot:run
    ```

The server will start on `http://localhost:8080`.

## 📖 API Documentation

Once the application is running, you can access the interactive API documentation (Swagger UI) to test endpoints directly:

👉 **http://localhost:8080/swagger-ui/index.html**

### Key Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/auth/register` | Register a new local user |
| `POST` | `/auth/login` | Login and receive JWT |
| `GET` | `/notes` | List/Search notes (supports pagination) |
| `POST` | `/notes/upload` | Upload a file (Multipart) |
| `GET` | `/notes/{id}/download` | Get file download URL |
| `POST` | `/bookmarks/{noteId}` | Bookmark a specific note |
| `GET` | `/users/profile/me` | Get current user profile details |

## 🧪 Testing Client

The project includes a lightweight static HTML frontend for testing the API flows (Auth, Upload, Listing).

Access it at: **http://localhost:8080/index.html**

## 🤝 Contributing

Contributions are welcome! Please follow these steps:
1.  Fork the project.
2.  Create your feature branch (`git checkout -b feature/AmazingFeature`).
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4.  Push to the branch (`git push origin feature/AmazingFeature`).
5.  Open a Pull Request.
