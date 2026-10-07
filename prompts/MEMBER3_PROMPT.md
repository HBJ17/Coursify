# Member 3: Waitlist & Analytics

Branch: `member3-waitlist`

> **Tell your coding agent:** "Read `prompts/MEMBER3_PROMPT.md` and do exactly what it says, step by step."

---

You are my coding agent. I am **Member 3** of a 4-person team building a Course Registration System
(Java 17, Swing, JDBC, Oracle). I own the Observer (event notifications), the waitlist with auto-promotion, and the
"most in-demand courses" analytics. **No database needed.** Everything is tested with the fakes.

## Read first
`README.md`, all of `model/` (especially `WaitlistEntry`, `CourseDemand`), `service/WaitlistService.java`,
`service/AnalyticsService.java`, the DAO interfaces in `dao/`, `observer/RegistrationSubject.java`,
`RegistrationListener.java`, `RegistrationEvent.java`, and the fakes `FakeWaitlistService`, `FakeAnalyticsService`,
`SimpleRegistrationSubject`, `FakeDataStore` (CS202 has 1 seat. CGPA: S001 8.5, S002 9.2, S003 7.8).

## My paths (the ONLY places I may create or edit files)
- `src/main/java/com/crs/observer/impl/`
- `src/main/java/com/crs/service/waitlist/`
- `src/main/java/com/crs/service/analytics/`
- `src/test/java/com/crs/observer/impl/`, `src/test/java/com/crs/service/waitlist/`, `src/test/java/com/crs/service/analytics/`
- `docs/member3-waitlist.md`
- `prompts/MEMBER3_PROMPT.md`

Stubs already there that I fill in (keep names, constructors and field names): `RegistrationSubjectImpl`
(both constructors), `WaitlistComparators` (`BY_JOIN_TIME`, `BY_CGPA_THEN_TIME`), `WaitlistServiceImpl`, `AnalyticsServiceImpl`.

## Plan: one commit per step, in this order
0. `git checkout -b member3-waitlist` (if not already on it).
1. `docs(waitlist): add member 3 work plan` (commit only `prompts/MEMBER3_PROMPT.md`)
2. `feat(observer): implement RegistrationSubjectImpl with CopyOnWriteArrayList`
   `useEdt == true` notifies through `SwingUtilities.invokeLater`. `false` notifies directly on the calling thread.
   One listener throwing must not stop the others (catch + `System.err`).
3. `test(observer): add RegistrationSubjectImpl tests` (use `new RegistrationSubjectImpl(false)`)
4. `feat(waitlist): implement BY_JOIN_TIME and BY_CGPA_THEN_TIME comparators`
   CGPA high to low, then earlier join time, then studentId (so the order is always the same).
5. `test(waitlist): add comparator ordering tests`
6. `feat(waitlist): load one PriorityQueue per course from WaitlistDAO`
   `Map<String, PriorityQueue<WaitlistEntry>>` built in the constructor from `waitlistDAO.findAll()` using `priority`.
7. `feat(waitlist): implement join and leave with checks and WAITLISTED event`
   join: student exists, course exists AND is full, the student isn't already registered
   (`registrationDAO.findActiveByStudent`) or already waiting, and the queue size is below `course.getWaitlistCap()`.
   Otherwise throw `IllegalStateException("...clear message...")`. Persist with `waitlistDAO.add(new WaitlistEntry(id, code, cgpa))`.
8. `feat(waitlist): implement positionOf, getWaitlist and getMyWaitlists`
   A PriorityQueue does not iterate in order. Copy it to a list and sort with `priority`. Position is 1-based, and -1 if absent.
9. `feat(waitlist): implement promoteNext auto-registering the next student`
   Only if the course has `getSeatsLeft() > 0`. Then `poll()`, `waitlistDAO.remove`, `registrationDAO.registerAtomic`,
   publish PROMOTED. Otherwise `Optional.empty()`.
10. `test(waitlist): add join, position and auto-promotion tests`
    Fakes + `RegistrationSubjectImpl(false)` + `BY_CGPA_THEN_TIME`. S001 takes CS202 (`registrationDAO.registerAtomic`),
    then S003 and S002 join, and S002 is position 1. `registrationDAO.cancelAtomic("S001","CS202")`, then `promoteNext`
    returns S002, and S002 is now registered. Also cover join errors (course not full, joining twice).
11. `feat(analytics): implement topInDemand with a max-heap`
    Build a `CourseDemand` per course (registered = active registrations, waitlisted = waitlist entries).
    Use a `PriorityQueue` with a reversed comparator on `getDemandScore()`, ties broken by code. Poll k times.
    Handle k <= 0 and k > count.
12. `test(analytics): add top-k ranking tests`
13. `docs(waitlist): add docs/member3-waitlist.md explaining Observer, PriorityQueue and max-heap`

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
