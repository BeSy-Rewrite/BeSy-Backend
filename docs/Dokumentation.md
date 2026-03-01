# Dokumentation – BeSy Backend

> **Bestellsystem (BeSy)** – Backend der Fakultät Informatik und Informationstechnik  
> Hochschule Esslingen

---

## Inhaltsverzeichnis

1. [Kurzfassung](#1-kurzfassung)
2. [Einleitung](#2-einleitung)
   - 2.1 [Motivation der Arbeit](#21-motivation-der-arbeit)
   - 2.2 [Problemstellung](#22-problemstellung)
   - 2.3 [Abgrenzung](#23-abgrenzung)
3. [Architektur](#3-architektur)
4. [Features](#4-features)
   - 4.1 [Geforderter Funktionsumfang](#41-geforderter-funktionsumfang)
   - 4.2 [Bestellverwaltung](#42-bestellverwaltung)
   - 4.3 [Bestellstatus und Zustandsübergänge](#43-bestellstatus-und-zustandsübergänge)
   - 4.4 [Personen- und Adressverwaltung](#44-personen--und-adressverwaltung)
   - 4.5 [Lieferantenverwaltung](#45-lieferantenverwaltung)
   - 4.6 [Kostenstellen](#46-kostenstellen)
   - 4.7 [Angebote und Rechnungen](#47-angebote-und-rechnungen)
   - 4.8 [PDF-Generierung](#48-pdf-generierung)
   - 4.9 [Insy-Integration](#49-insy-integration)
   - 4.10 [Paperless-ngx-Integration](#410-paperless-ngx-integration)
   - 4.11 [Authentifizierung und Autorisierung](#411-authentifizierung-und-autorisierung)
   - 4.12 [REST-API und Swagger-Dokumentation](#412-rest-api-und-swagger-dokumentation)
5. [Datenmodell](#5-datenmodell)
6. [Zielsetzung](#6-zielsetzung)

---

## Abkürzungsverzeichnis

| Abkürzung | Bedeutung |
|-----------|-----------|
| API       | Application Programming Interface |
| BeSy      | Bestellsystem |
| DFG       | Deutsche Forschungsgemeinschaft |
| EDV       | Elektronische Datenverarbeitung |
| HTTP      | Hypertext Transfer Protocol |
| IT        | Informationstechnik |
| JDBC      | Java Database Connectivity |
| JWT       | JSON Web Token |
| OAS       | OpenAPI Specification |
| OAuth2    | Open Authorization 2.0 |
| PDF       | Portable Document Format |
| PHP       | Hypertext Preprocessor |
| REST      | Representational State Transfer |
| SQL       | Structured Query Language |
| URL       | Uniform Resource Locator |
| UUID      | Universally Unique Identifier |
| Yii       | Yes it is! (PHP-Framework) |

---

## Abbildungsverzeichnis

| Nr. | Abbildung |
|-----|-----------|
| Abb. 1 | [Datenmodell des BeSy-Backends](#5-datenmodell) |
| Abb. 2 | [Zustandsmatrix einer Bestellung (Bestellmatrix)](#43-bestellstatus-und-zustandsübergänge) |
| Abb. 3 | [Ausschnitt der Swagger-API-Dokumentation](#412-rest-api-und-swagger-dokumentation) |

---

## 1. Kurzfassung

Das Bestellsystem (BeSy) der Fakultät Informatik und Informationstechnik der Hochschule Esslingen ist eine interne Webanwendung zur Verwaltung und Erstellung von Bestellungen. Das vorliegende Dokument beschreibt das neu entwickelte Backend, das auf Basis moderner Technologien – insbesondere Spring Boot 3, PostgreSQL und Keycloak – als Ersatz für das veraltete Yii-1.1-basierte System entwickelt wurde. Es werden Architektur, Datenmodell, Funktionsumfang sowie die Integration mit externen Systemen (Insy, Paperless-ngx) beschrieben.

---

## 2. Einleitung

In der Einleitung wird auf die Beweggründe dieser Arbeit eingegangen und welche Problemstellung dabei besteht. Abschließend wird daraus der Rahmen der Arbeit eingegrenzt und das erwünschte Ziel definiert.

### 2.1 Motivation der Arbeit

Die Fakultät Informatik und Informationstechnik (IT) der Hochschule Esslingen betreibt eine interne, eigene Bestellsoftware. Diese wird genutzt, um eine Übersicht aller Bestellungen zu haben, Bestellungen zu verwalten und um diese anzulegen. Gerade beim Erstellen von Bestellungen bietet die Software neben dem automatischen Abspeichern für Verwaltungs- und Dokumentationszwecke auch eine Zeitersparnis gegenüber dem manuellen Erstellen von Bestellungsformularen. Für die Zeitersparnis nutzt das Programm weitere Einträge, die öfters in Bestellungen vorkommen. Dafür gibt es Tabellen von Lieferanten, Personen der Hochschule, welche Bestellungen aufgeben, und Adressen zu den bestellenden Personen. Anhand der ausgewählten Einträge aus den weiteren Tabellen und weiteren manuellen Eingaben erstellt die Software einen Bestellungseintrag. Diese Bestellung kann anschließend mit einem Klick als automatisch ausgefülltes Bestellformular, im Portable Document Format (PDF), heruntergeladen werden. Die generierten Bestelldokumente können dann abschließend an weitere Stellen weitergereicht werden.

### 2.2 Problemstellung

Die bisherige Bestellsoftware basiert auf dem „Yes it is! (Yii)" Framework. Yii ist wiederum ein Hypertext Preprocessor (PHP) Framework. Die Software der Hochschule nutzt dabei eine der 1.1 Versionen des Frameworks. Zwar werden Sicherheitslücken und PHP-Kompatibilitätsprobleme in der Version noch behoben, jedoch ist das Ende der Unterstützung auf den 31.12.2026 datiert und somit nicht mehr allzu fern. Der Herausgeber des Frameworks empfiehlt auch neue Projekte in Yii 2.0 umzusetzen, weist jedoch auch darauf hin, dass bestehende 1.1 Applikationen in den meisten Fällen auf 2.0 komplett neu geschrieben werden müssen.

Aufgrund dessen, dass der Support in baldiger Zukunft eingestellt werden könnte und es damit zu Sicherheitsproblematiken kommen kann und eine Migration auf neuere Yii-Versionen einen kompletten Neuanfang der Programmierung bedeutet, entschloss sich die Fakultät, die Bestellsoftware auf Basis neuer und aktueller Technologien neu zu entwickeln.

### 2.3 Abgrenzung

Das Projekt umfasst ausschließlich das **Backend** des neuen Bestellsystems. Die Entwicklung eines Frontends sowie die Pflege oder Weiterentwicklung des Altsystems sind nicht Bestandteil dieser Arbeit. Das Backend stellt eine REST-API bereit, über die ein beliebiges Frontend angebunden werden kann. Die Authentifizierung und Benutzerverwaltung werden an Keycloak delegiert und sind nicht Gegenstand der eigentlichen Implementierung. Die Datenmigration aus dem Altsystem ist als separates Migrationsskript umgesetzt und ebenfalls nicht Teil der Kernanwendung.

---

## 3. Architektur

Das BeSy-Backend ist als klassische **Drei-Schichten-Architektur** auf Basis von **Spring Boot 3** realisiert:

- **Controller-Schicht:** REST-Endpunkte (Spring MVC), Eingabevalidierung und HTTP-Antworten.
- **Service-Schicht:** Geschäftslogik, Statusübergänge, Integrationen mit externen Systemen.
- **Persistenzschicht:** JPA/Hibernate mit **PostgreSQL** als relationale Datenbank.

Die Absicherung der API erfolgt über **OAuth2/JWT** mittels **Keycloak**. Externe Integrationen bestehen zu **Insy** (Übermittlung genehmigter Bestellungen) und **Paperless-ngx** (automatische Archivierung von Bestelldokumenten). Die API wird über **OpenAPI 3 / Swagger** dokumentiert.

---

## 4. Features

### 4.1 Geforderter Funktionsumfang

Der geforderte Funktionsumfang des neuen Backends umfasst:

- Anlegen, Bearbeiten und Löschen von Bestellungen
- Statusverfolgung von Bestellungen (Zustandsmaschine)
- Verwaltung von Personen, Adressen, Lieferanten und Kostenstellen
- Angebots- und Rechnungsverwaltung pro Bestellung
- Automatische PDF-Generierung für Bestellformulare
- Anbindung an das externe Bestellsystem Insy
- Archivierung über Paperless-ngx
- Sichere Authentifizierung und rollenbasierte Autorisierung
- Vollständige REST-API mit OpenAPI-Dokumentation

---

### 4.2 Bestellverwaltung

**Problem:** Das Altsystem ermöglichte keine strukturierte, nachvollziehbare Verwaltung des Bestelllebenszyklus mit klaren Statusübergängen.

**Lösung:** Bestellungen werden als zentrale Entität mit zahlreichen Attributen (Buchungsjahr, Rabatte, Flags für Genehmigungen, Kostenstellen, beteiligte Personen usw.) gespeichert. Über dedizierte Endpunkte können Bestellungen erstellt, geändert, gefiltert und exportiert werden.

---

### 4.3 Bestellstatus und Zustandsübergänge

**Problem:** Im Altsystem war der Bestellstatus nicht klar modelliert, was zu inkonsistenten Zuständen führte.

**Lösung:** Eine explizite Zustandsmaschine (Enum `OrderStatus`) steuert die erlaubten Übergänge. Die folgende Abbildung zeigt die Zustandsmatrix:

**Abb. 2 – Zustandsmatrix einer Bestellung (Bestellmatrix)**

![Zustandsmatrix einer Bestellung](https://github.com/user-attachments/assets/80753b94-6a3a-4af2-9e04-914fbe6b8301)

Die Zustände im Überblick:

| Status | Beschreibung |
|--------|--------------|
| `IN_PROGRESS` | Bestellung wird angelegt / bearbeitet |
| `COMPLETED` | Bestellung vollständig erstellt (Constraints geprüft) |
| `APPROVALS_RECEIVED` | Alle Zustimmungen erteilt |
| `APPROVED` | Vom Dekan abgesegnet |
| `SENT` | Abgeschickt |
| `SETTLED` | Abgerechnet |
| `ARCHIVED` | Archiviert |
| `DELETED` | Gelöscht |

---

### 4.4 Personen- und Adressverwaltung

**Problem:** Bestellende Personen und ihre Adressen mussten im Altsystem manuell und redundant eingegeben werden.

**Lösung:** Personen und Adressen werden als eigene Entitäten verwaltet und können bei Bestellungen referenziert werden. Dadurch entfällt doppelte Dateneingabe und die Konsistenz wird gewährleistet.

---

### 4.5 Lieferantenverwaltung

**Problem:** Lieferantendaten wurden nicht zentral gepflegt.

**Lösung:** Lieferanten sind als eigenständige Entität mit Attributen (Name, Adresse, Kontakt, MwSt.-ID, bevorzugter Status, Deaktivierungsdatum usw.) modelliert. Über die `SupplierController`-Endpunkte können Lieferanten verwaltet und Bestellungen zugeordnet werden.

---

### 4.6 Kostenstellen

**Problem:** Kostenstellen mussten manuell in jede Bestellung eingetragen werden.

**Lösung:** Kostenstellen (`CostCenter`) werden zentral verwaltet (mit Gültigkeitszeitraum und Kommentar) und können einer Bestellung als primäre oder sekundäre Kostenstelle zugewiesen werden.

---

### 4.7 Angebote und Rechnungen

**Problem:** Im Altsystem gab es keine strukturierte Verwaltung von Angeboten und Rechnungen zu einer Bestellung.

**Lösung:** Angebote (`Quotation`) und Rechnungen (`Invoice`) sind eigene Entitäten, die einer Bestellung zugeordnet werden. Rechnungen werden zudem automatisch in Paperless-ngx archiviert.

---

### 4.8 PDF-Generierung

**Problem:** Bestellformulare mussten manuell ausgefüllt werden.

**Lösung:** Der `OrderPDFService` füllt automatisch ein PDF-Bestellformular anhand der gespeicherten Bestelldaten aus. Das ausgefüllte Dokument kann per API-Endpunkt heruntergeladen werden.

---

### 4.9 Insy-Integration

**Problem:** Genehmigte Bestellungen mussten manuell in das externe System Insy übertragen werden.

**Lösung:** Der `InsyService` übermittelt Bestelldaten automatisch an Insy, sobald eine Bestellung den Status `APPROVED` erreicht hat. Dadurch entfällt der manuelle Übertragungsschritt.

---

### 4.10 Paperless-ngx-Integration

**Problem:** Bestelldokumente und Rechnungen wurden nicht systematisch digital archiviert.

**Lösung:** Der `PaperlessService` lädt generierte PDF-Dokumente und Rechnungen automatisch in Paperless-ngx hoch. Ein `PaperlessRetryService` stellt sicher, dass fehlgeschlagene Uploads wiederholt werden.

---

### 4.11 Authentifizierung und Autorisierung

**Problem:** Das Altsystem bot keine moderne, sichere Authentifizierung.

**Lösung:** Die API ist vollständig durch **OAuth2/JWT** via **Keycloak** abgesichert. Benutzer werden über ihren Keycloak-UUID identifiziert und können im System als `User`-Entität geführt werden.

---

### 4.12 REST-API und Swagger-Dokumentation

**Problem:** Das Altsystem hatte keine maschinenlesbare API-Dokumentation.

**Lösung:** Alle Endpunkte sind als RESTful API nach OpenAPI 3 spezifiziert und über Swagger UI erreichbar. Der folgende Screenshot zeigt einen Ausschnitt der generierten Swagger-Dokumentation:

**Abb. 3 – Ausschnitt der Swagger-API-Dokumentation**

![Swagger-Dokumentation](https://github.com/user-attachments/assets/aaee63f9-0b07-4201-8ad3-3d0f101efca0)

---

## 5. Datenmodell

Das folgende Entity-Relationship-Diagramm zeigt das vollständige Datenmodell des BeSy-Backends:

**Abb. 1 – Datenmodell des BeSy-Backends**

![Datenmodell](https://github.com/user-attachments/assets/5d3e28b9-cd38-41f5-bea7-8b8507f2041f)

Die zentralen Entitäten im Überblick:

| Entität | Beschreibung |
|---------|--------------|
| `order` | Zentrale Bestellentität mit allen Bestellattributen und Statusfeld |
| `item` | Einzelne Bestellpositionen mit Preis, Menge und MwSt.-Typ |
| `person` | Personen der Hochschule (Besteller, Lieferadresse, Ansprechpartner) |
| `address` | Adressen, die Personen und Lieferanten zugeordnet werden |
| `supplier` | Lieferanten mit Kontaktdaten und Präferenzmarkierung |
| `cost_center` | Kostenstellen mit Gültigkeitszeitraum |
| `quotation` | Angebote zu einer Bestellung |
| `invoice` | Rechnungen zu einer Bestellung |
| `currency` | Währungen für Bestellungen und Positionen |
| `vat` | Mehrwertsteuersätze |
| `user` | Systembenutzer (verknüpft mit Keycloak-UUID) |
| `customer_id` | Kundennummern eines Lieferanten |

---

## 6. Zielsetzung

Die Zielsetzung des Projekts ist im folgenden Dokument festgehalten:

📄 [Zielsetzung_Studienprojekt_Bestellsystem_saliit01.pdf](https://github.com/kr1pt0n05/Besy-backend/files/25660474/Zielsetzung_Studienprojekt_Bestellsystem_saliit01.pdf)
