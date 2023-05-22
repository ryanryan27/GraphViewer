if not exist ".\build\" mkdir .\build
javac -cp "./include/CPLEX/cplex.jar" -d ./build/ ./src/*.java