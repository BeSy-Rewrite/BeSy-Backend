# BeSy – Backend

Backend des Bestellsystems (BeSy) der Fakultät Informationstechnik der Hochschule Esslingen. Gebaut mit **Spring Boot 3**, **PostgreSQL** und **Keycloak (OAuth2)**.

- 🔗 **Live-Version:** [https://test.besy.hs-esslingen.com/](https://test.besy.hs-esslingen.com/)
- 📄 **Swagger UI:** [https://swagger.test.besy.hs-esslingen.com/](https://swagger.test.besy.hs-esslingen.com/)

---

## Features

- Bestellverwaltung (Erstellen, Bearbeiten, Statusverfolgung, Export)
- Verwaltung von Personen, Lieferanten und Kostenstellen
- Angebots- und Rechnungsbearbeitung
- PDF-Generierung für Bestellungen
- Integration mit **Insy** (externes Bestellsystem)
- Integration mit **Paperless-ngx** (Dokumentenverwaltung)
- OAuth2/JWT-Authentifizierung via Keycloak
- REST API mit OpenAPI/Swagger-Dokumentation

---

## Umgebungsvariablen

| Variable | Beschreibung |
|---|---|
| `SPRING_DATASOURCE_URL` | JDBC-URL zur PostgreSQL-Datenbank (z. B. `jdbc:postgresql://localhost:5432/besy`) |
| `POSTGRES_USERNAME` | Datenbankbenutzername |
| `POSTGRES_PASSWORD` | Datenbankpasswort |
| `POSTGRES_DATABASE` | Name der Datenbank |
| `PGA_EMAIL` | Admin-E-Mail für pgAdmin |
| `PGA_PASSWORD` | Admin-Passwort für pgAdmin |

---

## Docker Setup

### 1. Umgebungsvariablen setzen

Erstelle eine `.env`-Datei im Projektverzeichnis:

```env
POSTGRES_USERNAME=besy
POSTGRES_PASSWORD=geheimespasswort
POSTGRES_DATABASE=besy
PGA_EMAIL=admin@example.com
PGA_PASSWORD=admin
```

### 2. Datenbank starten

```bash
docker compose -f dev.migrated.docker-compose.yml up -d
```

Startet PostgreSQL und pgAdmin (erreichbar unter [http://localhost:5050](http://localhost:5050)).

### 3. Backend starten

```bash
docker compose -f migrated.dev.backend.docker-compose.yml up -d
```

Das Backend ist danach unter [http://localhost:3000](http://localhost:3000) erreichbar.

### 4. Anwendung selbst bauen (optional)

```bash
docker build -t besy-backend .
```

---

## SQL-Migration

Das Skript `migrated_data_schema_2025-10-25.sql` migriert Daten aus dem alten Schema (`besy`) in das neue Schema (`migrated_data`).

### Ausführung

Verbinde dich mit der laufenden PostgreSQL-Instanz (z. B. über pgAdmin oder `psql`) und führe das Skript aus:

```bash
psql -h localhost -p 5432 -U besy -d besy -f migrated_data_schema_2025-10-25.sql
```

> **Hinweis:** Das Schema `migrated_data` muss in der Datenbank vorhanden sein, bevor das Skript ausgeführt wird. Hibernate legt es beim ersten Start der Anwendung automatisch an (via `ddl-auto=create-only`).
