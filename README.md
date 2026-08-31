# BeSy – Backend

Backend des Bestellsystems (BeSy) der Fakultät Informationstechnik der Hochschule Esslingen. Gebaut mit **Spring Boot 3**, **PostgreSQL** und **Keycloak (OAuth2)**.

- 🔗 **Live-Version:** [https://besy.lind3.de/](https://besy.lind3.de/)
- 📄 **Swagger UI:** [https://swagger-besy.lind3.de/](https://swagger-besy.lind3.de/)

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

## PDFBox internal API usage

`src/main/java/org/apache/pdfbox/pdmodel/interactive/form/PlainTextFormatterTrampoline.java`
is deliberately placed inside PDFBox's own package to gain package-private
access to `PlainTextFormatter`/`AppearanceStyle`/`PlainText` — none of
which are public API. This exists because PDFBox 3.5.3's automatic AcroForm
appearance generation silently substitutes a broken fallback font
(Liberation Sans) when re-resolving certain embedded CID-keyed fonts (e.g.
Noto Sans SC) from `/DR` by name, making CJK/emoji field values either
crash or lose their embedded glyph data entirely. Every documented,
public-API alternative was tried and ruled out first (see
`FontEmbeddingDiagnostic` and `PlainTextFormatterTrampolineDiagnostic` in
the test sources for the full investigation).

**Maintenance note**: this relies on undocumented PDFBox internals. A
future PDFBox version upgrade is likely to require re-verifying or
adjusting this class — expect a **compile error** (safe failure mode)
rather than a silent behavior change, since it's real Java code in that
package, not reflection. Re-run the two diagnostic test classes above
after any PDFBox version bump, before assuming this still works.
