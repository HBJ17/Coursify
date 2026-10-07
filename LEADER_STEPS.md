# Leader steps

## 1. Repo (done)
Repo: https://github.com/HBJ17/Coursify. The base project is already on `main`.
Leader: on GitHub, go to **Settings > Collaborators** and add the 3 teammates. Then send each one their zip
(`Member1.zip` ... `Member3.zip`). You keep `Member4.zip`.

## 2. Everyone (you too, as Member 4)
```
git clone https://github.com/HBJ17/Coursify.git
cd Coursify
git config user.name  "Your Name"
git config user.email "email-on-your-GitHub-account"
git checkout -b member<N>-<name>     (member1-data / member2-registration / member3-waitlist / member4-ui)
```
- Unzip **your** `Member<N>.zip` into the `Coursify` folder. Say **Yes / Replace** if asked.
- Open your coding agent in that folder and type:
  **"Read `prompts/MEMBER<N>_PROMPT.md` and do exactly what it says, step by step."**
- The agent makes 13-15 commits and pushes your branch at the end. Then tell the leader "done".

## 3. Merge everything (leader, at the end, about 5 min)
```
git checkout main
git pull
git fetch origin
git merge --no-ff origin/member1-data         -m "Merge member 1: data and catalog"
git merge --no-ff origin/member2-registration -m "Merge member 2: registration engine"
git merge --no-ff origin/member3-waitlist     -m "Merge member 3: waitlist and analytics"
git merge --no-ff origin/member4-ui           -m "Merge member 4: UI"
mvn test
git push
```
No conflicts are possible: each branch only changed its own folders, and every class already existed with its
final constructor. `--no-ff` keeps every member's commits under their own name.

Optional check before merging a branch (lists any file outside that member's folders):
`git diff --name-only main...origin/member2-registration`

## 4. Run the finished app on Oracle
1. Install Oracle XE 21c or 23ai Free and SQL Developer.
2. As SYSTEM, run `database/00_create_user.sql`. Then, as `crs`, run `database/schema.sql`.
3. Copy `src/main/resources/db.properties.example` to `db.properties` (use `FREEPDB1` instead of `XEPDB1` on 23ai).
4. Run `mvn exec:java`. It picks Oracle automatically when `db.properties` exists.

Demo flow: login S001/pass > register CS101 > CS102 (clash) > CS201 (prerequisite) > CS202 >
login S002 > CS202 full, so join the waitlist > login S001 > cancel CS202 > S002 auto-promoted > login A001/admin > dashboard.

Proof of contribution: `git shortlog -sne main`, or **Insights > Contributors** on GitHub.
