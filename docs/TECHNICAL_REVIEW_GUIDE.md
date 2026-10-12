# Coursify: Technical Review Guide

A study sheet for the faculty review. Every claim points at a real file, so open it and read it if you're asked to show the code.

---

## 1. The 30-second pitch

> Coursify is a **Course Registration System** in **Java 17**. It has a **Swing GUI** and an **Oracle database** connected through **JDBC** (driver `ojdbc11`), and it's built with **Maven**. Students log in, browse courses, register, cancel, and join waitlists. Admins see a demand dashboard and can add or remove courses.
> Before a registration goes through, four rules check it: no **duplicate**, all **prerequisites** done (found with a DFS over the prerequisite graph), no **time clash**, and a **free seat**. When a course is full, students go on a **priority-queue waitlist** ordered by CGPA. When a seat frees up, the top student on the list is registered automatically.
> The architecture is layered: **UI → Controller → Service → DAO → Database**. It uses the **Singleton, Factory, Builder, Strategy, Observer and DAO** patterns.

Demo logins: `S001 / pass`, `S002 / pass`, `S003 / pass` (students) and `A001 / admin` (admin).

---

## 2. Architecture (layers)

```
┌──────────────────────────────────────────────────────────┐
│ UI (Swing)          com.crs.ui, com.crs.ui.theme         │  JFrame, JPanels, JTable, custom painting
├──────────────────────────────────────────────────────────┤
│ Controller          com.crs.controller                   │  runs service calls off the UI thread, shows dialogs
├──────────────────────────────────────────────────────────┤
│ Service (logic)     com.crs.service.*                    │  rules, waitlist, analytics, auth
├──────────────────────────────────────────────────────────┤
│ DAO (data access)   com.crs.dao (interfaces)             │
│                     com.crs.dao.impl (Oracle JDBC)       │  SQL lives ONLY here
│                     com.crs.fake (in-memory versions)    │
├──────────────────────────────────────────────────────────┤
│ util                DBConnectionManager (Singleton)      │  reads db.properties, gives out Connections
├──────────────────────────────────────────────────────────┤
│ Oracle DB           database/schema.sql                  │
└──────────────────────────────────────────────────────────┘
      model/ (Student, Course, …) and exception/ are shared by every layer
```

**Why layers?** Each layer talks to the one below it only through an **interface**. So the UI never sees SQL, and the database can be swapped for in-memory fakes without changing any UI or service code.

**Wiring:** `app/AppContext.java` is the **only place** that calls `new` on the services and DAOs. It's a simple form of **Dependency Injection**: every class receives what it needs through its constructor.
- `createReal()` wires the Oracle `*DAOImpl` classes.
- `createWithFakes()` wires the `InMemory*DAO` classes.
- `createDefault()` picks Oracle if `src/main/resources/db.properties` exists, and the fakes otherwise.

**Startup** (`app/Main.java`): `SwingUtilities.invokeLater(...)` → `Theme.install()` → `new MainFrame(AppContext.createDefault()).setVisible(true)`. Running with `--console` starts a text-only demo (`ConsoleDemo`).

---

## 3. The classes, package by package

### `model`: the data objects (OOP concepts)
| Class | What it is | OOP point to mention |
|---|---|---|
| `Person` (abstract) | id, name, email, password + abstract `getRole()` | **Abstraction**, **Encapsulation** (private fields, validating setters) |
| `Student extends Person` | adds `cgpa` (0 to 10, validated) | **Inheritance**; `getRole()` returns `"STUDENT"` (**Polymorphism**) |
| `Admin extends Person` | `getRole()` returns `"ADMIN"` | same |
| `Course` | code, title, credits, capacity, seatsLeft, timeSlot, prerequisites, waitlistCap | **Builder pattern** (`Course.builder("CS101","Java").credits(4).build()`); `equals`/`hashCode` on the code |
| `TimeSlot` | day + start + end | **Immutable** (`final` class and fields); checks start < end |
| `Registration` | regId, studentId, courseCode, status, regTime | |
| `RegistrationStatus` | `enum { ACTIVE, CANCELLED }` | enum |
| `WaitlistEntry` | studentId, courseCode, cgpa, joinedTime | |
| `CourseDemand` | numbers for the admin dashboard (registered, waitlisted, capacity, demand score) | |

### `exception`: custom exceptions
- `RegistrationException` (**checked**, extends `Exception`) is the parent of:
  `DuplicateRegistrationException`, `PrerequisiteNotMetException` (carries the list of missing courses), `TimeConflictException`, `CourseFullException`.
- `AuthException` is thrown on a bad login.
- `DatabaseOperationException` (**unchecked**, extends `RuntimeException`) **wraps `SQLException`**, so the UI never sees raw JDBC errors and the interfaces don't need `throws SQLException` everywhere.

**Likely question:** *Why are some exceptions checked and some unchecked?* Business-rule failures are expected, and the caller *must* handle them (show a message), so they're checked. Database failures are unexpected infrastructure problems, so they're unchecked.

### `dao`: interfaces (the contracts)
`StudentDAO`, `AdminDAO`, `CourseDAO`, `RegistrationDAO`, `WaitlistDAO`. Each has two implementations:
- `dao.impl.*DAOImpl` for real **Oracle JDBC**
- `fake.InMemory*DAO` for a `HashMap`/`List` store (`FakeDataStore`) used for development and tests

### `service`: business logic
| Interface | Implementation | Job |
|---|---|---|
| `AuthService` | `catalog/AuthServiceImpl` | checks students first, then admins; returns a `Person`; gives the **same error message** for a wrong ID or a wrong password, so an attacker can't tell which one was wrong |
| `CourseService` | `catalog/CourseServiceImpl` | list/search/add/delete courses |
| `RegistrationService` | `registration/RegistrationServiceImpl` | runs the rule chain, then `registerAtomic`; cancel → `cancelAtomic` → promotes the next student on the waitlist |
| `WaitlistService` | `waitlist/WaitlistServiceImpl` | **one `PriorityQueue` per course**, using a comparator from `WaitlistComparators` |
| `AnalyticsService` | `analytics/AnalyticsServiceImpl` | top-k courses by demand, using a **max-heap** (`PriorityQueue` with a reversed comparator) |

### `service.rules`: the Strategy pattern
`ValidationRule` (abstract, one method: `validate(student, course)`), with these subclasses:
1. `DuplicateRule`: already actively registered?
2. `PrerequisiteRule`: **DFS** over `courseDAO.loadPrerequisiteGraph()` (an adjacency list `Map<String, List<String>>`) collects *all* transitive prerequisites, e.g. CS301 → CS201 → CS101. A `visited` set stops it repeating work and protects against cycles. The missing courses are sorted and put into the exception.
3. `TimeConflictRule`: two slots overlap when they're on the **same day AND a.start < b.end AND b.start < a.end**. A `null` slot never clashes.
4. `SeatAvailabilityRule`: throws `CourseFullException` when `seatsLeft == 0`.

`RegistrationServiceImpl` keeps them in a `List<ValidationRule>` and loops over them. A new rule means a new class with no change to the loop (the **Open/Closed principle**).

### `observer`: the Observer pattern
- `RegistrationSubject` has `addListener`, `removeListener` and `publish`.
- `RegistrationListener` (functional interface) has `onRegistrationChanged(event)`.
- `RegistrationEvent` has a type: `REGISTERED`, `CANCELLED`, `WAITLISTED` or `PROMOTED`.
- `impl/RegistrationSubjectImpl` stores listeners in a **`CopyOnWriteArrayList`**, which is safe to loop over while listeners are being added or removed. It notifies listeners on the Swing EDT with `SwingUtilities.invokeLater`, and catches each listener's exception so one bad listener can't break the others.

Who publishes: the registration and waitlist services. Who listens: every `RefreshablePanel` and the `AppShell` sidebar badges. **Result:** when you register on the Browse page, the My Courses page and the sidebar counters update by themselves.

### `controller`
- `BaseController.runAsync(task, onSuccess, onError)` wraps a **`SwingWorker`**: the service call runs on a background thread and the result comes back on the EDT. `showError` maps each exception type to a dialog with a matching colour and title.
- `LoginController`, `RegisterController` (register → if the course is full, offer the waitlist), `AdminController`.

### `ui` and `ui.theme`: see section 5.

### `util.DBConnectionManager`: see section 4.

---

## 4. How the database is connected (JDBC)

### 4.1 Configuration
`src/main/resources/db.properties` (git-ignored; copy it from `db.properties.example`):
```properties
db.url=jdbc:oracle:thin:@localhost:1521/XEPDB1
db.user=crs
db.password=crs
```
- `jdbc:oracle:thin` is Oracle's pure-Java **Thin driver** (type 4: it needs no native Oracle client installed).
- `localhost:1521` is the host and the default Oracle listener port.
- `XEPDB1` is the **service name** of the pluggable database (`FREEPDB1` on 23ai Free).
- The driver comes in through Maven: `com.oracle.database.jdbc:ojdbc11` in `pom.xml`.

### 4.2 `DBConnectionManager`: a Singleton (`util/DBConnectionManager.java`)
- **Private constructor** that reads `db.properties` **once** with `getResourceAsStream("/db.properties")` and `Properties.load`.
- `public static synchronized getInstance()` uses lazy initialisation. `synchronized` means two threads can't create two instances at the same time.
- `getConnection()` returns `DriverManager.getConnection(url, user, password)`, a **new connection each time**. The caller closes it with try-with-resources.
- **Likely question:** *Where is `Class.forName("oracle.jdbc.OracleDriver")`?* It isn't needed. Since **JDBC 4.0**, `DriverManager` finds drivers automatically through `META-INF/services/java.sql.Driver` inside the ojdbc jar (ServiceLoader).

### 4.3 The standard JDBC steps (shown with `StudentDAOImpl.findById`)
```java
try (Connection conn = DBConnectionManager.getInstance().getConnection();         // 1. connect
     PreparedStatement ps = conn.prepareStatement(SELECT + " WHERE student_id = ?")) { // 2. prepare SQL
    ps.setString(1, studentId);                                                      // 3. bind parameter
    try (ResultSet rs = ps.executeQuery()) {                                         // 4. execute
        return rs.next() ? Optional.of(mapStudent(rs)) : Optional.empty();           // 5. map row -> object
    }
} catch (SQLException e) {
    throw new DatabaseOperationException("Could not load student " + studentId, e);  // 6. wrap error
}                                                                                    // 7. auto-close all
```
Talking points:
- **`PreparedStatement` everywhere, never string concatenation of user input**, which prevents **SQL injection** and lets Oracle reuse the query plan.
- **try-with-resources** closes the `ResultSet`, then the `Statement`, then the `Connection` automatically, even when an exception is thrown, so connections can't leak.
- `executeQuery()` is for SELECT and returns a `ResultSet`. `executeUpdate()` is for INSERT/UPDATE/DELETE and returns the **number of rows changed**. The code uses that count, e.g. `StudentDAOImpl.save` runs an UPDATE and, if it changed 0 rows, does an INSERT (an "upsert").
- A private `mapXxx(ResultSet)` method turns each row into an object (simple **ORM by hand**).

### 4.4 Transactions: the most important part (`RegistrationDAOImpl.registerAtomic`)
Registering takes **two SQL statements that must both succeed or both fail**:
```java
conn.setAutoCommit(false);                       // start transaction
// 1) take a seat, but ONLY if one is left
UPDATE courses SET seats_left = seats_left - 1 WHERE course_code = ? AND seats_left > 0
   -> if 0 rows updated: no seat → throw → rollback
// 2) insert the registration and read back the generated ID
INSERT INTO registrations (...) VALUES (?, ?, ?, ?)   // prepareStatement(sql, new String[]{"REG_ID"})
   -> ps.getGeneratedKeys() → registration.setRegId(...)
conn.commit();                                   // both done
// on any SQLException/RuntimeException: conn.rollback(); finally: setAutoCommit(true)
```
- **ACID / atomicity:** a seat is never taken without a matching registration, and the reverse can't happen either.
- **Concurrency:** `AND seats_left > 0` in the `WHERE` clause makes the check and the decrement **one atomic statement**. If two students click Register for the last seat at the same moment, Oracle's row lock lets only one UPDATE change a row. The other gets 0 rows and is rolled back. The table also has `CHECK (seats_left >= 0)` as a final safety net.
- **Generated keys:** Oracle has no `AUTO_INCREMENT`. The schema uses `GENERATED BY DEFAULT AS IDENTITY`, and JDBC reads the new ID back with `getGeneratedKeys()`.
- `cancelAtomic` follows the same pattern: set the status to CANCELLED, then `seats_left + 1`.
- `CourseDAOImpl` puts the pattern in a reusable helper, `inTransaction(errorMessage, SqlWork work)`, which takes a **lambda** (`SqlWork` is a private functional interface). `save` and `update` write the course row and its prerequisite rows in one transaction, using **batch inserts** (`addBatch` / `executeBatch`).
- **Avoiding N+1 queries:** `CourseDAOImpl.findAll()` loads the whole prerequisite graph in **one** query instead of one query per course.

### 4.5 Schema (`database/schema.sql`)
| Table | Key points |
|---|---|
| `students` | PK `student_id`; `email UNIQUE`; `CHECK (cgpa BETWEEN 0 AND 10)` |
| `admins` | PK `admin_id` |
| `courses` | PK `course_code`; `CHECK capacity > 0`, `seats_left >= 0`; `class_day` checked against the 7 day names; times stored as `'HH24:MI'` text |
| `prerequisites` | composite PK `(course_code, prereq_code)`, both FKs to `courses` (a **self-referencing many-to-many**, i.e. a graph) |
| `completed_courses` | composite PK `(student_id, course_code)` |
| `registrations` | identity PK; FKs; `status CHECK IN ('ACTIVE','CANCELLED')`; `reg_time DEFAULT SYSTIMESTAMP` |
| `waitlist` | identity PK; `UNIQUE (student_id, course_code)` |

- Every FK uses `ON DELETE CASCADE`, so deleting a course also removes its prerequisites, registrations and waitlist rows.
- Oracle specifics: there's no ENUM type, so the schema uses VARCHAR2 + CHECK; there's no TIME type, so times are text parsed by `LocalTime.parse`. The script can be re-run safely because a PL/SQL block ignores `ORA-00942` (table does not exist) when it drops tables.
- **Normalisation:** prerequisites and completed courses are in their own tables, not comma-separated columns (1NF), and the tables are in 3NF.
- Cancelling is a **soft delete** (`status = 'CANCELLED'`), so history is kept.

### 4.6 Setup steps (if asked to show it)
1. As SYSTEM, run `database/00_create_user.sql`, which creates user `crs/crs`.
2. As `crs`, run `database/schema.sql`.
3. Create `db.properties`, then run `mvn exec:java`. The status bar at the bottom shows **"Data: Oracle database"**.

---

## 5. How the GUI was developed (Swing)

### 5.1 Toolkit and threading
- **Java Swing**, built in code (no GUI builder or drag-and-drop), on top of the Metal look-and-feel, which `Theme.install()` restyles through `UIManager` (fonts, colours, scrollbars).
- **Event Dispatch Thread (EDT) rules:**
  - The window is created inside `SwingUtilities.invokeLater` (`Main`).
  - Database calls **never run on the EDT**. `SwingWorker` runs them in `doInBackground()` and updates the UI in `done()`. This happens in `BaseController.runAsync`, `RefreshablePanel.reload` and `AppShell.refreshCounters`. **Why:** a slow query would otherwise freeze the window.
  - Observer notifications are pushed back onto the EDT with `invokeLater`.

### 5.2 Window and navigation
- `MainFrame extends JFrame implements ScreenNavigator`: the **only window**. It uses a **`CardLayout`** to swap between three screens: `LOGIN`, `STUDENT` and `ADMIN`.
- `PanelFactory` (**Factory pattern**) turns a screen name into a new panel. `MainFrame` never calls `new LoginPanel()` itself.
- Each time a screen is closed, `MainFrame.detachListeners` walks the component tree and calls `detach()` on every `LiveComponent`, which unsubscribes it from the observer. This **prevents memory leaks** and stops hidden screens from refreshing.
- `ScreenNavigator` is a small interface, so panels can be created and **unit-tested without opening a real window**.
- `Session` remembers the logged-in `Person` (Student or Admin) for the whole window.

### 5.3 Screens
| Screen | Class | Notes |
|---|---|---|
| Login | `LoginPanel` | ID and password fields → `LoginController` → `AuthService`. Goes to the STUDENT or ADMIN screen depending on `getRole()` |
| App frame | `AppShell` | `BorderLayout`: sidebar (WEST), header (NORTH), pages (CENTER, its own `CardLayout`), status bar (SOUTH). Sidebar badges show counts, e.g. My Courses "2" |
| Student: Browse Courses | `BrowseCoursesPanel` | stat cards, live search (`DocumentListener`), credits filter, course table with a Register / Join waitlist button on each row |
| Student: My Courses | `MyCoursesPanel` + `TimetableStrip` | registered courses and a weekly timetable drawn by hand |
| Student: My Waitlists | `WaitlistPanel` | shows your position in each queue; leave a waitlist |
| Admin: Dashboard | `AdminDashboardPanel` + `DemandChart` | KPI cards and a bar chart of top courses by demand, drawn by hand |
| Admin: Courses | `AdminCoursesPanel` + `AddCourseDialog` (`JDialog`) | add or delete courses |

### 5.4 Reusable building blocks
- **`RefreshablePanel<T>`** (abstract, **generic**): a **Template Method** base class. Subclasses only write `loadData()`, which runs in the background, and `render(T)`, which runs on the EDT. The base class subscribes to the observer and reloads on every event.
- **`CourseTable extends JTable`**: one table class with three modes (`CATALOG`, `MY_COURSES`, `ADMIN`). It has its own `AbstractTableModel` and a custom `TableCellRenderer` that paints code chips, seat bars, prerequisite chips and action buttons. Swing reuses **one renderer component for every row** of a column (the **flyweight** idea).
- **`ui.theme`**: the design system.
  - `Theme`: every colour and font is a constant, so the whole look changes from one file.
  - Custom components: `UiButton extends JButton`, `InputField extends JTextField`, `PasswordInput`, `UiComboBox<E>`, `Card` (a rounded panel), `Chip` (a badge), `StatCard`, `Banner`, `Avatar`, `EmptyState`.
  - `Toast` (popup notification), `MessageDialog` (styled replacement for `JOptionPane`), `ThinScrollBarUI extends BasicScrollBarUI`, `FieldBorder extends AbstractBorder`.
  - `Icons`: vector line icons drawn with `Graphics2D`, so there are no image files.
  - `Draw`: helpers for anti-aliased rounded rectangles, text and chips.
- **Custom painting:** override `paintComponent(Graphics g)` and cast to `Graphics2D` with anti-aliasing. Examples: `DemandChart`, `TimetableStrip`, `AppShell.NavItem`.

### 5.5 Layout managers used
`BorderLayout` (page structure), `CardLayout` (switching screens and pages), `GridLayout` (the row of stat cards), `FlowLayout` (button rows), `BoxLayout` (sidebar list, stacked titles), `GridBagLayout` (centring).

### 5.6 Event handling
`ActionListener` lambdas on buttons, `MouseAdapter` (sidebar hover and click, table action buttons), `DocumentListener` (live search as you type), and the custom `RegistrationListener` (data changes).

---

## 6. Walk-throughs of the key flows

**Register (happy path):** click Register on a row → `RegisterController.register(code)` → `SwingWorker` runs `RegistrationServiceImpl.register` in the background → load the Student and Course → Duplicate → Prerequisite (DFS) → TimeConflict → Seat → `registerAtomic` (transaction) → `publish(REGISTERED)` → every listening panel reloads → toast: "Registered for CS101".

**Course full:** `SeatAvailabilityRule` throws `CourseFullException` → the controller catches it → dialog: "CS202 is full. Join the waitlist? You'd be #2" → `WaitlistServiceImpl.join` checks that the course is full, the student isn't registered or already queued, and the waitlist isn't over its cap → saves a row in the DB and adds to the in-memory `PriorityQueue` → `publish(WAITLISTED)`.

**Cancel → auto-promotion:** `cancelAtomic` (transaction) → `publish(CANCELLED)` → `waitlistService.promoteNext(code)` → `poll()` the highest-priority entry (**highest CGPA first; ties go to the earlier join time, then the student ID**) → remove it from the waitlist table → `registerAtomic` for that student → `publish(PROMOTED)`.

**Try these in the demo:**
- S001 registers for CS101, then CS102: **time clash** (both on Monday morning).
- S001 registers for CS201: **prerequisite missing** (CS101). S002 can register, because S002 has already completed CS101.
- CS301: the DFS finds CS102, CS201 **and** CS101 (the prerequisite chain).
- CS202 has 1 seat: S001 registers; S003 and then S002 join the waitlist; S001 cancels; **S002 is promoted** (CGPA 9.2 beats 7.8).

---

## 7. Design patterns (be ready to name and point at each)
| Pattern | Where |
|---|---|
| **Singleton** | `DBConnectionManager` |
| **Factory** | `PanelFactory`; `AppContext.createReal/createWithFakes` |
| **Builder** | `Course.builder(...)…build()` |
| **Strategy** | `ValidationRule` subclasses; waitlist `Comparator` (`BY_CGPA_THEN_TIME` vs `BY_JOIN_TIME`) |
| **Observer** | `RegistrationSubject` / `RegistrationListener` / `RefreshablePanel` |
| **DAO** | `dao` interfaces + `dao.impl` |
| **Template Method** | `RefreshablePanel` (`loadData` / `render`) |
| **Dependency Injection** (constructor) | `AppContext` |
| **MVC-style split** | model / ui (view) / controller |

## 8. Data structures and algorithms
- **Graph + DFS**: transitive prerequisites (`PrerequisiteRule`), using an adjacency list `Map<String, List<String>>` and a `visited` set.
- **PriorityQueue (binary heap)**: per-course waitlist, O(log n) insert and poll. Analytics top-k uses a max-heap.
- **Interval overlap**: time clashes, O(1) per pair.
- **HashMap / LinkedHashMap / HashSet**: lookups, keeping insertion order, finding missing prerequisites.
- **CopyOnWriteArrayList**: thread-safe list of listeners.
- **Streams and lambdas**: `getMyCourses`, filters, comparators.

## 9. Testing
- **JUnit 5**, 18 test classes: rules, services, waitlist comparators, the observer, the DAOs, and UI pieces (`PanelFactoryTest`, `SessionTest`, `CourseTableTest`).
- The services are tested against the **in-memory fakes**, so they need no database (a benefit of coding to interfaces).
- `OracleDaoTest` uses `assumeTrue(db.properties exists)`, so it **skips itself** when no database is set up.
- Current result: `mvn test` → **76 run, 0 failed, 8 skipped** (the Oracle tests, which skip without a database).
- **CI:** `.github/workflows/ci.yml` runs `mvn test` on JDK 17 for every pull request and every push to main.

---

## 10. Questions you may get, with short answers

1. **Statement vs PreparedStatement?** A PreparedStatement is precompiled and takes bound parameters (`?`), which prevents SQL injection and is faster when run repeatedly. A Statement concatenates strings.
2. **What does `setAutoCommit(false)` do?** Statements stay in one transaction until `commit()`, or are undone by `rollback()`. With the default (`true`), every statement commits on its own.
3. **What if two students take the last seat at once?** The conditional `UPDATE … WHERE seats_left > 0` is atomic, and Oracle row-locks the row, so only one of them succeeds. The other's transaction rolls back.
4. **Why wrap SQLException?** It hides JDBC details from upper layers, keeps interfaces clean, and lets the UI show a friendly "Database error" dialog.
5. **Do you use connection pooling?** No. Each DAO call opens and closes a connection through `DriverManager`. **Improvement:** use HikariCP or Oracle UCP.
6. **Why SwingWorker?** Swing is single-threaded. Long work on the EDT freezes the window. SwingWorker moves the work to a background thread and delivers the result back on the EDT.
7. **How do other screens refresh after registration?** The Observer pattern: the service publishes an event, and each `RefreshablePanel` reloads through a SwingWorker.
8. **How does the app run without Oracle?** `AppContext.createDefault()` switches to the in-memory fakes when there's no `db.properties`. The same interfaces are used, so no other code changes.
9. **Abstract class vs interface here?** `Person` and `ValidationRule` are abstract classes (shared state or base behaviour). The DAOs and services are interfaces (pure contracts with several implementations).
10. **How are passwords stored?** As plain text. That's acceptable for a demo, but it's a known gap (see below).

## 11. Known limitations: say these before the reviewers find them
- **Passwords are plain text.** Fix: hash them with BCrypt or PBKDF2 and a salt.
- **No connection pool.** A new physical connection per DAO call is slow under load.
- **The waitlist queue is kept in memory** (`WaitlistServiceImpl` loads it from the DB in its constructor). It's correct for a single desktop instance, but two running app instances wouldn't share it.
- **Promotion isn't one transaction:** `promoteNext` removes the waitlist row and then calls `registerAtomic` as two separate DB operations. If the second fails, the student is dropped from the waitlist without being registered. Fix: do both in one transaction in the DAO.
- **Race between rule check and insert:** the seat rule checks a `Course` object loaded a moment earlier. The real guard is the SQL `seats_left > 0`, which then raises a `DatabaseOperationException` ("No seats left") instead of the friendlier `CourseFullException`.
- **Rules make several queries per registration** (e.g. `TimeConflictRule` loads each registered course separately). That's fine at this size, but a JOIN would be better at scale.
