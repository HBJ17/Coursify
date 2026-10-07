# Course Registration System

Java 17 Swing + JDBC + **Oracle Database**. Built by a team of 4, each owning one part.

## How the project is organised

```
src/main/java/com/crs/
├── model/, exception/, dao/*.java, service/*.java, observer/*.java   SHARED (nobody edits)
├── fake/                                                             SHARED fakes for testing (nobody edits)
├── util/, dao/impl/, service/catalog/                                Member 1  Data & Catalog (Oracle JDBC)
├── service/rules/, service/registration/                             Member 2  Registration Engine
├── observer/impl/, service/waitlist/, service/analytics/             Member 3  Waitlist & Analytics
└── ui/, controller/, app/                                            Member 4  UI & Main
database/                                                             Member 1
```

Every class the app needs **already exists as a stub** with its final constructor, and `AppContext.createReal()`
is already wired to them. Each member only fills in the stubs in **their own folders** (and adds new files there).
Because no two members ever touch the same file, merging all four branches never conflicts, and the finished
app works as soon as the last branch is merged.

Each member's instructions are in `prompts/MEMBER<N>_PROMPT.md` (arrives with that member's zip).

## Sample data (same in the fakes and in `database/schema.sql`)

| Login | Password | Notes |
|---|---|---|
| S001 | pass | Arun, CGPA 8.5 |
| S002 | pass | Priya, CGPA 9.2, has already passed CS101 |
| S003 | pass | Rahul, CGPA 7.8 |
| A001 | admin | Admin |

| Course | Built-in test case |
|---|---|
| CS101 / CS102 | Both on Monday morning, so they **clash** |
| CS201 | Needs CS101 (**prerequisite**) |
| CS202 | Only **1 seat**, good for testing full course + waitlist |
| CS301 | Needs CS102 and CS201 (**prerequisite chain**) |

## Running it

Needs **JDK 17+** and **Maven**.

```bash
mvn compile
mvn test
mvn exec:java
```

Without `src/main/resources/db.properties` the app runs on the in-memory fakes.

### Oracle setup (Member 1 during development; everyone for the final demo)
1. Install Oracle Database XE 21c or 23ai Free, plus SQL Developer.
2. As SYSTEM, run `database/00_create_user.sql` (creates user `crs` / password `crs`).
3. As `crs`, run `database/schema.sql` (tables + sample data).
4. Copy `src/main/resources/db.properties.example` to `db.properties`, then check the service name
   (`XEPDB1` for XE, `FREEPDB1` for 23ai Free). It's git-ignored.

With `db.properties` present, `mvn exec:java` uses the real Oracle-backed classes.

## Team rules
1. Only edit files in your own folders. Never `git add .`. Add your own paths only.
2. Never change a shared file or a stub's class name or constructor.
3. Commit style: `type(area): what you did`, e.g. `feat(rules): add TimeConflictRule`.
4. Understand your code. You must be able to explain every line in the viva.
