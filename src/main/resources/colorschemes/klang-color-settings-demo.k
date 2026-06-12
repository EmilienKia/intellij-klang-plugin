// Line comment
/* Block
   comment */
module demo::colors;

import k::io;
import k::math;

// ── Enumerations ──────────────────────────────────────────────
public enum Color : int {
    RED = 0; GREEN = 1; BLUE = 2 default;
}

// ── Interfaces ────────────────────────────────────────────────
@Deprecated
public interface Printable {
    print() : bool;
    ~Printable();
}

template<typename T>
public interface Comparable {
    compareTo(other : T&) : int;
}

// ── Templates ─────────────────────────────────────────────────

template<typename T>
struct Box {
    value : T;
    Box(v : T) : value(v) {}
    get() : T& { return value; }
}

template<typename K, typename V>
struct Pair {
    first  : K;
    second : V;
    Pair(a : K, b : V) : first(a), second(b) {}
}

// ── Aggregates ────────────────────────────────────────────────
@Serializable
public struct Point : Printable, Comparable<Point> {
    public:
        x : float = 0.0f;
        y : float = 0.0f;

    Point(x : float, y : float) : x(x), y(y) {}

    ~Point() {}

    distance(other : Point&) result : float {
        dx : float = this.x - other.x;
        dy : float = this.y - other.y;
        result = (dx * dx + dy * dy) ** 0.5f;
    }

    override compareTo(other : Point&) : int {
        d : float = distance(other);
        return (d > 0.0f) ? 1 : ((d < 0.0f) ? -1 : 0);
    }

    override print() : bool {
        msg : const char[] = "Point";
        k::io::print(msg);
        return true;
    }

    operator ==(other : Point&) : bool {
        return x == other.x && y == other.y;
    }
}

// ── Template function ─────────────────────────────────────────

template<typename T>
max(a : T, b : T) : T {
    return (a > b) ? a : b;
}

// ── Regular functions ─────────────────────────────────────────

sum(values : int[], n : int) : int {
    total : int = 0;
    i : int = 0;
    while (i < n) {
        if (i > 1000) { break; }
        total += values[i];
        i++;
    }
    return total;
}

classify(x : int) : const char* {
    if (x > 0) {
        return "positive";
    } else if (x < 0) {
        return "negative";
    } else {
        return "zero";
    }
}

tryGetValue(ptr : int*) : bool {
    if (v : int* = ptr) {
        return *v > 0;
    }
    return false;
}

// ── Main ──────────────────────────────────────────────────────

main() : int {
    p : Point = Point(1.0f, 2.0f);
    q : Point = new Point(3.0f, 4.0f);

    b : Box<Point> = Box<Point>(p);
    pr : Pair<int, float> = Pair<int, float>(42, 3.14f);
    fa : FixedArray<8>;

    d : float = p.distance(q);
    m : int = max<int>(10, 20);

    mask : int = 0xFF & ~0x0F;
    flag : bool = (mask >> 4) != 0 || false;
    label : const char* = flag ? "set" : "clear";
    data : int[] = { 10, 20, 30 };

    bits : int = (data[0] << 2) ^ (data[1] | 0xAB);
    pct : float = 100.0f * 3 / fa.size();

    color : Color = Color::BLUE;
    ptr : int* = null;

    if (ref : int* = ptr) {
        *ref = 42;
    } else {
        destroy ptr;
    }

    for (i : int = 0; i < fa.size(); i++) {
        fa.data[i] = i * 2;
    }

    delete q;
    return 0;
}

