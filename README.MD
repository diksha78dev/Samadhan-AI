# 🌾 Samadhan with AI – Rural Intelligence System

**Voice-first, multilingual AI platform for rural India**  
Supports agriculture, healthcare & government schemes in Hindi, Marathi & English.

## 🎯 Problem Statement
Rural communities face barriers in accessing essential services due to low digital literacy, language gaps, and poor connectivity.

## 💡 Our Solution
- **Voice input** (Web Speech API) – speak naturally
- **AI-powered responses** (Gemini API + intelligent fallback)
- **Offline mode** (IndexedDB) – works without internet
- **Multi-language** – Hindi, Marathi, English (text + voice output)

## 🛠️ Tech Stack
| Layer | Technology |
|-------|------------|
| Frontend | React, Tailwind CSS, Web Speech API |
| Backend | Spring Boot, JPA, MySQL |
| AI | Google Gemini API |
| Offline | IndexedDB (browser cache) |
| Build | Vite, Maven |

## 📸 Demo
[Click to watch demo video](https://youtu.be/your-video-link) *(add after recording)*

## 🚀 How to Run Locally

### Backend (Spring Boot)
```bash
cd samadhan-ai
./mvnw spring-boot:run
```

### Frontend (React)
```bash
cd samadhan-frontend
npm install
npm run dev
```

### Database (MySQL)
1. Create a MySQL database named `samadhan_db`.
2. Update `src/main/resources/application.properties` with your MySQL credentials.

Example configuration:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/samadhan_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_mysql_password_here
spring.jpa.hibernate.ddl-auto=update
```

You can copy `src/main/resources/application.properties.example` and fill in your values.
