# Users Management

Servizio REST per **Gestione Utenti** sviluppato in **Java 21 + Spring Boot 3**, in linea con i *Requisiti Funzionali Obbligatori* (CRUD utenti con ruoli).

## 🎯 Obiettivi
- Gestione utenti: **lista, dettaglio, creazione, modifica, cancellazione**.
- Campi utente: `username`, `email` (unica e **non modificabile**), `codiceFiscale` (valido e unico), `nome`, `cognome`, `roles`.
- Architettura a layer (**web**, **application**, **domain**, **infrastructure**) con **DTO**, **validation**, **test**.
- Documentazione automatica via **OpenAPI/Swagger UI**.

## 🧰 Tech Stack
- Java 21, Spring Boot 3 (Web, Validation, Data JPA)
- Lombok, MapStruct
- DB **H2 file-based** (profilo `demo`) – nessuna installazione DB necessaria
- JUnit + MockMvc + H2 per test d’integrazione
- springdoc-openapi per Swagger

---

## ⚙️ Prerequisiti
- **JDK 21** installato e nel PATH (`java -version`).
- **Maven 3.9+** installato e nel PATH (`mvn -v`).

> Se si usa IntelliJ IDEA puoi anche eseguire `clean package` dalla finestra **Maven** senza shell.

---

## 🚀 Esecuzione *step-by-step* del JAR (profilo `demo`)
Il profilo `demo` usa un database **H2 su file** in `./.localdb/usersdb` (creato automaticamente al primo avvio).

1. **Clona** il repository
   ```bash
   git clone <URL_DEL_REPO>
   cd user-management
   ```

2. **Build del progetto**
   ```bash
   mvn -U clean package
   ```
   Questo comando produce `target/<nome-jar>-SNAPSHOT.jar`.

3. **Esegui il JAR con il profilo `demo`**
   ```bash
   java -jar target/*-SNAPSHOT.jar --spring.profiles.active=demo
   ```
   Output atteso in console: `Tomcat started on port 8080` e `Started Application...`

4. **Verifica** in browser:
   - Swagger UI: http://localhost:8080/swagger-ui/index.html
   - API base: `http://localhost:8080/api/v1/users`

### (In alternativa) Avvio da IntelliJ IDEA
- Crea una **Run Configuration → Application** con `Main class`: `com.users.Application`
- In **Program arguments** inserisci: `--spring.profiles.active=demo`
- Run ▶️

---

## 🔌 Endpoint principali
- `GET    /api/v1/users` — lista paginata (parametri: `page`, `size`, `sort`)
- `GET    /api/v1/users/{id}` — dettaglio
- `POST   /api/v1/users` — crea utente + ruoli
- `PUT    /api/v1/users/{id}` — modifica dati/ruoli (**email** non modificabile)
- `DELETE /api/v1/users/{id}` — elimina

### DTO di esempio
**Create**
```json
{
  "username": "mario",
  "email": "mario.rossi@example.com",
  "codiceFiscale": "RSSMRA85T10A562S",
  "nome": "Mario",
  "cognome": "Rossi",
  "roles": ["DEVELOPER", "REPORTER"]
}
```

**Update**
```json
{
  "username": "marioX",
  "nome": "MarioX",
  "cognome": "RossiX",
  "roles": ["OWNER", "DEVELOPER"]
}
```

### Sorting sicuro
Swagger talvolta propone `sort=string,asc` → genera 500.  
Il controller filtra i campi ammessi: `id, username, email, codiceFiscale, nome, cognome`.  
Esempio valido: `GET /api/v1/users?sort=nome,asc`.

---

## 🧪 Test
```bash
mvn -Ptest -U clean test
```

Casi coperti:
- create 201 + Location
- list 200 + struttura Page (o PageDTO)
- get 200 / 404
- update 200 (email invariata)
- delete 204

---

## 🧩 Profili applicativi
- **demo**: H2 file-based, schema auto, seed opzionale.
- **test**: H2 in-memory (per i test).
---
