# From workspace root
cd Demo
javac -encoding UTF-8 -d target/classes src/main/java/com/eclinic/*.java
java -cp target/classes com.eclinic.App

# Use this from workspace root:
powershell -ExecutionPolicy Bypass -File run-clinic.ps1

# If port 8080 is already in use, run:
powershell -ExecutionPolicy Bypass -File run-clinic.ps1 -ForceRestart

# Electronic Clinic Workspace

Workspace nay da duoc tach thanh 2 project:

- `Demo/`: Toan bo code hien tai (ban demo).
- `Clinic/`: Scaffold project moi (dang de trong, co cau hinh Java co ban).

## Cau truc

```text
Electronic-Clinic/
|- Demo/
|  |- pom.xml
|  |- src/main/java/com/eclinic/*.java
|  |- target/classes/
|  |- data/
|  \- frontend/
|- Clinic/
|  |- pom.xml
|  |- src/main/java/com/eclinic/App.java
|  |- target/classes/
|  |- data/
|  \- frontend/
\- .vscode/tasks.json
```

## Lenh cho Demo

Compile:

```powershell
javac -encoding UTF-8 -d Demo/target/classes Demo/src/main/java/com/eclinic/*.java
```

Run:

```powershell
java -cp Demo/target/classes com.eclinic.App
```

## Lenh cho Clinic

Compile:

```powershell
javac -encoding UTF-8 -cp Clinic/lib/postgresql-42.7.5.jar -d Clinic/target/classes Clinic/src/main/java/com/eclinic/*.java
```

Run:

```powershell
java -cp "Clinic/target/classes;Clinic/lib/postgresql-42.7.5.jar" com.eclinic.App
```

Luu y: Clinic da duoc noi voi Supabase bang JDBC, xem huong dan tai `Clinic/README.md`.

## VS Code Tasks

Tasks da duoc cap nhat de tach rieng:

- `Compile Demo Java`
- `Run Demo App`
- `Compile Clinic Java`
- `Run Clinic App`

Ban van co the xem tai lieu demo chi tiet o `Demo/README.md`.
