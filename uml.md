@startuml
skinparam classAttributeIconSize 0
hide empty methods
left to right direction

package "com.eclinic" {

class App {
- PORT: int
+ main(args: String[]): void
}

class "App.HttpRequest" as HttpRequest {
- method: String
- path: String
- headers: Map<String, String>
- body: String
}

class Client {
- registrationService: UserService
+ Client(registrationService: UserService)
+ createNewAccount(userType: String, fullName: String, userInformation: String): User
}

class Database {
- DATA_FILE_PATH: String
- currentUserId: AtomicInteger
- users: List<User.UserRecord>
+ Database()
+ nextUserId(): int
+ addUserRecord(userRecord: User.UserRecord): void
+ getUsers(): List<User.UserRecord>
+ printSnapshot(): void
}

abstract class User <<Product>> {
- database: Database
- id: int
- fullName: String
- userInformation: String
+ getId(): int
+ getFullName(): String
+ getUserInformation(): String
+ getRole(): String
+ saveProfile(): void
}

class "User.UserRecord" as UserRecord {
- id: int
- fullName: String
- roleKey: String
- role: String
- userInformation: String
+ id(): int
+ fullName(): String
+ roleKey(): String
+ role(): String
+ userInformation(): String
+ toString(): String
}

abstract class UserFactory <<Creator>> {
+ registerAccount(request: UserRegistrationRequest): User
# createUser(request: UserRegistrationRequest): User
# validateRequest(request: UserRegistrationRequest): void
# isBlank(value: String): boolean
# audit(user: User): void
}

class Doctor <<ConcreteProduct>>
class Patient <<ConcreteProduct>>
class Nurse <<ConcreteProduct>>

class DoctorCreator <<ConcreteCreator>> {
- database: Database
+ DoctorCreator(database: Database)
# validateRequest(request: UserRegistrationRequest): void
# createUser(request: UserRegistrationRequest): User
}

class PatientCreator <<ConcreteCreator>> {
- database: Database
+ PatientCreator(database: Database)
# validateRequest(request: UserRegistrationRequest): void
# createUser(request: UserRegistrationRequest): User
}

class NurseCreator <<ConcreteCreator>> {
- database: Database
+ NurseCreator(database: Database)
# validateRequest(request: UserRegistrationRequest): void
# createUser(request: UserRegistrationRequest): User
}

class UserService {
- factories: Map<UserType, UserFactory>
+ UserService(database: Database)
+ register(userType: String, fullName: String, userInformation: String): User
}

class "UserService.UserRegistrationRequest" as UserRegistrationRequest {
- fullName: String
- userInformation: String
+ fullName(): String
+ userInformation(): String
}

enum UserType {
DOCTOR
PATIENT
NURSE
+ from(rawValue: String): UserType
}

}

' Relationships
App --> Client
App --> Database
App ..> HttpRequest

Client --> UserService

UserService --> UserType
UserService --> UserFactory : uses map
UserService *-- UserRegistrationRequest

DoctorCreator --|> UserFactory
PatientCreator --|> UserFactory
NurseCreator --|> UserFactory

Doctor --|> User
Patient --|> User
Nurse --|> User

DoctorCreator --> Doctor : create
PatientCreator --> Patient : create
NurseCreator --> Nurse : create

User --> Database : saveProfile()
Database --> UserRecord : stores
User *-- UserRecord

note right of UserFactory
Factory Method:
- registerAccount() defines the fixed workflow
- createUser() is delegated to ConcreteCreators
end note

@enduml