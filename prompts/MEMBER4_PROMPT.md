# Member 4: UI & Main

Branch: `member4-ui`

> **Tell your coding agent:** "Read `prompts/MEMBER4_PROMPT.md` and do exactly what it says, step by step."

---

You are my coding agent. I am **Member 4** of a 4-person team building a Course Registration System
(Java 17, **Swing**, JDBC, Oracle). I own every screen, the controllers, and `Main`. **No database needed.**
I develop against `AppContext.createWithFakes()`, which already behaves like the finished app.

## Read first
`README.md`, all of `model/` and `exception/`, every interface in `service/`, `observer/RegistrationSubject.java`
+ `RegistrationListener.java` + `RegistrationEvent.java`, `app/AppContext.java`, `app/Main.java`, `ui/MainFrame.java`.
Sample logins: S001/pass, S002/pass, A001/admin. CS101+CS102 clash, CS201 needs CS101, CS202 has 1 seat.

## My paths (the ONLY places I may create or edit files)
- `src/main/java/com/crs/ui/`
- `src/main/java/com/crs/controller/`
- `src/main/java/com/crs/app/Main.java` and new files in `src/main/java/com/crs/app/`.
  **Do NOT edit `app/AppContext.java`.** Its wiring is fixed for the whole team.
- `src/test/java/com/crs/ui/`, `src/test/java/com/crs/controller/`
- `docs/member4-ui.md`
- `prompts/MEMBER4_PROMPT.md`

Stub already there that I fill in (keep name + constructor): `ui/MainFrame(AppContext ctx)`.

## Design rules
- UI and controllers use ONLY the service interfaces from `AppContext` (`getAuthService()`, etc.). No SQL, no DAOs,
  no other member's classes.
- Every service call from a button runs in a `SwingWorker` (in a controller). In `done()`, unwrap
  `ExecutionException.getCause()` and show `JOptionPane.showMessageDialog(... e.getMessage() ...)`.
- Panels refresh themselves by `ctx.getSubject().addListener(...)`, and remove the listener on logout.
- Plain Swing only, no extra libraries. Tests must not open a window (no `JFrame` in tests).

## Plan: one commit per step, in this order
0. `git checkout -b member4-ui` (if not already on it).
1. `docs(ui): add member 4 work plan` (commit only `prompts/MEMBER4_PROMPT.md`)
2. `feat(ui): add Session holding the logged-in Person`
3. `feat(ui): implement MainFrame with CardLayout and showScreen`
4. `feat(ui): add PanelFactory building each screen by name` (Factory pattern. Constants LOGIN, STUDENT, ADMIN.)
5. `feat(controller): add BaseController with SwingWorker helper and error dialogs`
6. `feat(ui): add LoginPanel with LoginController routing students and admins`
   Student goes to a `JTabbedPane` (Browse / My Courses / Waitlist). Admin goes to the dashboard. Add a Logout button on both.
7. `feat(ui): add BrowseCoursesPanel with JTable and live search`
   Columns: Code, Title, Credits, Seats (left/capacity), Time, Prerequisites. A `DocumentListener` calls `courseService.search`.
8. `feat(controller): add RegisterController with waitlist offer on full course`
   On `CourseFullException`: YES/NO dialog "Course is full. Join the waitlist?", then `waitlistService.join`.
   Show clash, prerequisite and duplicate errors as messages.
9. `feat(ui): add MyCoursesPanel with cancel and auto-refresh`
10. `feat(ui): add WaitlistPanel showing position with leave button`
11. `feat(ui): add AdminDashboardPanel with top in-demand courses`
    `analyticsService.topInDemand(5)` in a JTable, plus a table of `getAllActiveRegistrations()`, both refreshed on every event.
12. `feat(ui): add admin add-course and remove-course dialogs`
    `Course.builder(...)` + `TimeSlot` from a day dropdown and HH:mm fields.
13. `feat(app): start the Swing GUI from Main using AppContext.createDefault`
    `SwingUtilities.invokeLater(() -> new MainFrame(AppContext.createDefault()).setVisible(true))`, with the system
    look and feel. Move the old console demo into `app/ConsoleDemo.java`.
14. `test(ui): add Session and PanelFactory tests without opening a window`
15. `docs(ui): add docs/member4-ui.md with the screen flow`

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
