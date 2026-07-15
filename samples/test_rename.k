module test::rename;

// Test aggregate rename refactoring
public struct Person {
    name : const char*;
    age : int;

    // Constructor with same name as aggregate
    Person(name : const char*, age : int) : name(name), age(age) {}

    // Destructor with ~ prefix
    ~Person() {}

    greet() : void {
        // ...
    }
}

main() : int {
    // Constructor call - should be renamed too
    p : Person = Person("Alice", 30);
    
    // Another constructor call
    q : Person = new Person("Bob", 25);
    
    return 0;
}
