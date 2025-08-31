[![progress-banner](https://backend.codecrafters.io/progress/redis/b098b4cc-2a5d-44f6-aaee-28621541711c)](https://app.codecrafters.io/users/codecrafters-bot?r=2qF)

This is a starting point for Java solutions to the
["Build Your Own Redis" Challenge](https://codecrafters.io/challenges/redis).

The entry point for this implementation is in `src/main/java/Main.java`.

# Redis Server Implementation in Java

This project is a custom implementation of a Redis server in Java, focusing on core Redis features such as streams, lists, sorted sets and persistence using the Redis Database (RDB) file format. The project demonstrates Redis functionality with attention to multithreading, command parsing, and data storage.
For commands refer to CommandConstants.java file inside constant folder

---

## How to Run

### Prerequisites
- Java Development Kit (JDK) 11 or above.
- Maven for project dependencies.
- Redis CLI for testing the server.

### Steps to Run
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd <repository-directory>

2. Build the project using Maven:
   ```bash
   mvn clean install

If you have redis installed

3. Run the server:
   ```bash
   java -jar target/redis-server.jar --dir=<rdb-file-directory> --dbfilename=<rdb-file-name>

4. Connect using Redis CLI:
   ```bash
   redis-cli -h localhost -p 6379

Else

3. Run the server:
   ```bash
   java -cp target/classes/ com.test.Main

4. Run the code(example):
   ```bash
   echo -e "*2\r\n$6\r\nCONFIG\r\n$3\r\nGET\r\n$3\r\ndir\r\n" | nc localhost 6379
