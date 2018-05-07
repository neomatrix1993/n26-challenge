##README

###Description:
1. Gradle based project using spring boot.
2. The end points are of O(1) having constant time and space complexity.
3. The code runs assuming precision levels to add and retrieve statistics.
4. The precision and rolling windows are configurable through the config file which can also be replaced using application properties.
5. Current precision level is 1 second.
6. API handles expired timestamps.
7. All solution is in memory.
8. All test cases are written with 100% code coverage.
9. To deploy (will build, run tests and deploy locally on port 8080):
```
To build and run:
./gradlew clean build bootrun
To build
./gradlew clean build 
To run:
./gradlew bootrun
```