module test::collections;

/**
 * SimpleVector<T> — a simple dynamic array.
 * Used for testing aggregate rename with constructors/destructors.
 */
template<typename T>
class SimpleVector {
    private:
    _data : T*;
    _size : int;
    _capacity : int;

    public:
    // Constructor - should be renamed when SimpleVector is renamed
    SimpleVector() {
        _size = 0;
        _capacity = 0;
        _data = null;
    }

    // Destructor - should be renamed when SimpleVector is renamed
    ~SimpleVector() {
        if (_data != null) {
            // Free memory
        }
    }

    pushBack(value : T&) {
        _size = _size + 1;
    }

    clear() {
        _size = 0;
    }
}

main() : int {
    // Constructor calls - should be updated when SimpleVector is renamed
    v1 : SimpleVector<int> = SimpleVector<int>();
    v2 : SimpleVector<int> = new SimpleVector<int>();
    
    v1.pushBack(42);
    v2.clear();
    
    return 0;
}
