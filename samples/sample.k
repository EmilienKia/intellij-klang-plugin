// Line comment
/* Block
 comment */
module demo::colors;

import k::io;

public enum Direction {
    NORTH = 0; SOUTH = 1; EAST = 2; WEST = 3 default;
}

@Deprecated
public interface Printable {
    print() : bool;
}

@Serializable
public struct Point : Printable {
    public:
        x : float = 0.0f;
        y : float = 0.0f;

    Point(x : float, y : float) : x(x), y(y) {}

    distance(other : Point&) result : float {
        dx : float = this.x - other.x;
        dy : float = this.y - other.y;
        result = (dx * dx + dy * dy) * 0.5f;
    }

    operator ==(other : Point&) : bool {
        return x == other.x && y == other.y;
    }

    print() : bool {
        msg : const char[] = "Point";
        k::io::print(msg);
        return true;
    }
}

sum(values : int[], n : int) : int {
    total : int = 0;
    i : int = 0;
    while (i < n) {
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

main() : int {
    p : Point = Point(1.0f, 2.0f);
    q : Point = new Point(3.0f, 4.0f);
    d : float = p.distance(q);
    mask : int = 0xFF & ~0x0F;
    flag : bool = (mask >> 4) != 0 || false;
    label : const char* = flag ? "set" : "clear";
    data : int[] = { 10, 20, 30, null };
    return 0;
}
