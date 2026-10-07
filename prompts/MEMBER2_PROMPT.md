# Member 2: Registration Engine

Branch: `member2-registration`

> **Tell your coding agent:** "Read `prompts/MEMBER2_PROMPT.md` and do exactly what it says, step by step."

---

You are my coding agent. I am **Member 2** of a 4-person team building a Course Registration System
(Java 17, Swing, JDBC, Oracle). I own the rules that decide whether a student may register, and the registration
service. **No database needed.** Everything is tested with the fakes.

## Read first
`README.md`, all of `model/` and `exception/`, `service/RegistrationService.java`, `service/WaitlistService.java`,
the DAO interfaces in `dao/`, `observer/RegistrationSubject.java` + `RegistrationEvent.java`,
`fake/FakeRegistrationService.java` (a simple version of what I'm building) and `fake/FakeDataStore.java` (sample data:
CS101/CS102 clash on Monday, CS201 needs CS101, CS301 needs CS102 + CS201, CS202 has 1 seat, S002 passed CS101).

## My paths (the ONLY places I may create or edit files)
- `src/main/java/com/crs/service/rules/`
- `src/main/java/com/crs/service/registration/`
- `src/test/java/com/crs/service/rules/`, `src/test/java/com/crs/service/registration/`
- `docs/member2-registration.md`
- `prompts/MEMBER2_PROMPT.md`

Stub already there that I fill in (keep name + constructor): `RegistrationServiceImpl`.
New files I create in `service/rules/`: `ValidationRule`, `DuplicateRule`, `SeatAvailabilityRule`, `TimeConflictRule`,
`PrerequisiteRule`.

## Design
- `public abstract class ValidationRule { public abstract void validate(Student s, Course c) throws RegistrationException; }`
  Each subclass gets the DAOs it needs through its constructor. (Polymorphism + Strategy pattern.)
- `RegistrationServiceImpl` builds `List<ValidationRule>` in its constructor in this order:
  Duplicate, Prerequisite, TimeConflict, SeatAvailability (seats last, so only eligible students are offered the waitlist).
- `validate` throws the base `RegistrationException`. In `register`, re-throw the 4 declared types with a multi-catch.
  Anything else gets wrapped in `IllegalStateException`.
- Unknown student or course: `IllegalArgumentException("Unknown course: " + code)`.

## Plan: one commit per step, in this order
0. `git checkout -b member2-registration` (if not already on it).
1. `docs(registration): add member 2 work plan` (commit only `prompts/MEMBER2_PROMPT.md`)
2. `feat(rules): add abstract ValidationRule base class`
3. `feat(rules): add DuplicateRule using a HashSet of active course codes`
4. `feat(rules): add SeatAvailabilityRule throwing CourseFullException`
5. `feat(rules): add TimeConflictRule with same-day interval overlap`
   Overlap = same day AND `a.start < b.end && b.start < a.end`. A null TimeSlot never clashes. Throw with the clashing code.
6. `feat(rules): add PrerequisiteRule using DFS over the prerequisite graph`
   Graph = `courseDAO.loadPrerequisiteGraph()`. DFS with a visited set collects ALL transitive prerequisites.
   Missing = those not in `studentDAO.findCompletedCourses`. Throw `PrerequisiteNotMetException(code, missing)`.
7. `test(rules): add DuplicateRule and SeatAvailabilityRule tests`
8. `test(rules): add TimeConflictRule tests incl. CS101/CS102 clash and back-to-back slots`
   (10:00-11:00 then 11:00-12:00 is NOT a clash)
9. `test(rules): add PrerequisiteRule tests for CS201 and the CS301 chain`
   (S001 blocked from CS201, S002 allowed, S002 blocked from CS301 with missing CS102 + CS201)
10. `feat(registration): implement register running the rule chain`
    Validate, then `registrationDAO.registerAtomic(new Registration(studentId, courseCode))`, then publish
    `new RegistrationEvent(REGISTERED, ...)`, then return it.
11. `feat(registration): implement cancel with CANCELLED event and waitlist promotion`
    No active registration: `IllegalArgumentException`. Otherwise `cancelAtomic`, publish CANCELLED, then
    `waitlistService.promoteNext(courseCode)`.
12. `feat(registration): implement getMyCourses and getAllActiveRegistrations`
13. `test(registration): add RegistrationServiceImpl tests with in-memory DAOs`
    Wire: `FakeDataStore.withSampleData()`, `InMemory*DAO`, `NoOpWaitlistService`, `SimpleRegistrationSubject`.
    Cover: success lowers seats, clash, prerequisite, duplicate, full CS202, cancel restores the seat, events published,
    and promoteNext called on cancel (use a tiny recording WaitlistService written inside the test file).
14. `docs(registration): add docs/member2-registration.md explaining rules, DFS and overlap`

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
