# Member 1: Data & Catalog (Oracle JDBC)

Branch: `member1-data`

> **Tell your coding agent:** "Read `prompts/MEMBER1_PROMPT.md` and do exactly what it says, step by step."

---

You are my coding agent. I am **Member 1** of a 4-person team building a Course Registration System
(Java 17, Swing, JDBC, **Oracle Database**). I own the database layer, course catalog and login.

## Read first
`README.md`, all of `src/main/java/com/crs/model/`, `exception/`, the interfaces in `dao/` and
`service/AuthService.java` + `service/CourseService.java`, `database/schema.sql`, `src/main/resources/db.properties.example`,
and the fakes in `fake/` (`InMemory*DAO` show the exact expected behaviour, so match them).

## My paths (the ONLY places I may create or edit files)
- `src/main/java/com/crs/util/`
- `src/main/java/com/crs/dao/impl/`
- `src/main/java/com/crs/service/catalog/`
- `src/test/java/com/crs/util/`, `src/test/java/com/crs/dao/impl/`, `src/test/java/com/crs/service/catalog/`
- `database/`
- `docs/member1-data.md`
- `prompts/MEMBER1_PROMPT.md`

Stubs already there that I fill in (keep names + constructors): `DBConnectionManager`, `StudentDAOImpl`,
`AdminDAOImpl`, `CourseDAOImpl`, `RegistrationDAOImpl`, `WaitlistDAOImpl`, `AuthServiceImpl`, `CourseServiceImpl`.

## Oracle facts to use
- Driver is already in `pom.xml` (`ojdbc11`). URL looks like `jdbc:oracle:thin:@localhost:1521/XEPDB1`.
- Column for the day is `class_day` (VARCHAR2, e.g. `'MONDAY'`, maps to `DayOfWeek.valueOf`). `start_time` / `end_time`
  are VARCHAR2 `'HH:mm'` (use `LocalTime.parse` / `toString()`). NULL day means a null `TimeSlot`.
- Generated IDs: `conn.prepareStatement(sql, new String[]{"REG_ID"})` then `getGeneratedKeys()`
  (same for `WAITLIST_ID`). Oracle does not support `RETURN_GENERATED_KEYS` reliably.
- Timestamps: `rs.getTimestamp(..).toLocalDateTime()` / `Timestamp.valueOf(LocalDateTime)`.
- `PreparedStatement` everywhere, try-with-resources everywhere, every `SQLException` wrapped in
  `DatabaseOperationException` (with a readable message).

## Plan: one commit per step, in this order
0. `git checkout -b member1-data` (if not already on it).
1. `docs(data): add member 1 work plan` (commit only `prompts/MEMBER1_PROMPT.md`)
2. `feat(util): implement DBConnectionManager singleton reading db.properties`
   Load `/db.properties` from the classpath once. If it's missing, throw `DatabaseOperationException` with a clear
   message. `getConnection()` = `DriverManager.getConnection(url, user, password)`.
3. `feat(dao): implement StudentDAOImpl findById, findAll and save`
4. `feat(dao): add findCompletedCourses using completed_courses table`
5. `feat(dao): implement AdminDAOImpl`
6. `feat(dao): implement CourseDAOImpl reads with prerequisites and TimeSlot mapping`
   (`findByCode`, `findAll`, `loadPrerequisiteGraph`. Fill `Course.builder(...).prerequisites(...)`.)
7. `feat(dao): implement CourseDAOImpl save, update and delete in one transaction`
   (course row + its prerequisites rows together. Rollback on error.)
8. `feat(dao): implement transactional registerAtomic and cancelAtomic`
   registerAtomic: `setAutoCommit(false)`, then `UPDATE courses SET seats_left = seats_left - 1 WHERE course_code = ? AND seats_left > 0`.
   If 0 rows: rollback and throw `DatabaseOperationException("No seats left in " + code, null)`. Then INSERT the
   registration, set the generated regId, commit. Any error: rollback. Restore auto-commit in `finally`.
   cancelAtomic: UPDATE status to 'CANCELLED' for the ACTIVE row (0 rows means throw), then seats_left + 1, commit.
9. `feat(dao): implement RegistrationDAOImpl query methods`
10. `feat(dao): implement WaitlistDAOImpl joining students for cgpa`
11. `feat(catalog): implement CourseServiceImpl with HashMap lookup and TreeMap sorted view`
    Rebuild `HashMap<String, Course>` and `TreeMap<String, Course>` from `courseDAO.findAll()` on every read, so
    seat counts are always fresh. `search`: case-insensitive match on code or title. Blank query returns all.
12. `feat(catalog): implement AuthServiceImpl for students and admins`
    Students first, then admins. Wrong ID or password throws `AuthException("Invalid ID or password")`.
13. `test(catalog): add CourseServiceImpl and AuthServiceImpl tests with in-memory DAOs`
14. `test(dao): add Oracle DAO tests that skip when db.properties is missing`
    Use `Assumptions.assumeTrue(getClass().getResource("/db.properties") != null)`. Test register/cancel seat counts
    and the CS301 prerequisite graph. Clean up rows you insert.
15. `docs(data): add docs/member1-data.md explaining schema, transactions and Singleton`

## Rules you must follow (this is what makes the final merge conflict-free)

1. **Only create or edit files inside MY paths listed above.** Never touch any other file: not `README.md`,
   `pom.xml`, `model/`, `exception/`, `fake/`, the interfaces in `dao/*.java`, `service/*.java`, `observer/*.java`,
   `app/AppContext.java` (unless I'm Member 4 and the step says so), `.github/`, `.gitattributes`, `.gitignore`,
   `src/test/java/com/crs/SkeletonTest.java`, or another member's folders or prompt file.
2. **Never rename a stub class, change its package, or change its constructor parameters.** `AppContext.createReal()`
   already calls them exactly as they are. Replace the method bodies and add private helpers, fields and new files.
3. If an interface or model seems to need a change: **stop and tell me**. Don't change it, and work around it.
4. Code only against the interfaces in `com.crs.dao` / `com.crs.service` / `com.crs.observer`, never against another
   member's class. For tests use `com.crs.fake` (`FakeDataStore.withSampleData()`, `InMemory*DAO`, `NoOpWaitlistService`,
   `SimpleRegistrationSubject`). Tests must pass **without Oracle installed**.
5. Simple, readable Java 17 with short comments. I have to explain every line in a viva.

## How to commit (do this for every step)
1. Write the code for that step only.
2. Run `mvn -q compile`. It must succeed. After any step that adds tests, also run `mvn -q test`. It must pass.
3. Run `git status`. If any file outside my paths changed, undo it with `git checkout -- <file>` (or delete it if new).
4. `git add` **only my paths** (never `git add .` or `git add -A`).
5. `git commit -m "<the exact message from the plan>"`.
6. Commit messages contain ONLY the message from the plan. **No `Co-Authored-By` lines, no "Generated with" lines, no AI/agent attribution of any kind.** Commits must be authored by me (my `git config user.name` / `user.email`).
7. Move to the next step. Don't squash, amend, or rebase.

After the last step: run `mvn -q test` once more, then `git push -u origin <my branch>`, and give me:
- a summary of every class I own (what it does, which OOP concept / pattern / data structure it shows),
- the 5 viva questions I'm most likely to be asked about my part, with short answers.
