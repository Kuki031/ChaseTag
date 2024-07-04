package org.chasetag;

public interface Castable {
    void castSpeedBoost(long window);
    void checkFuel();
    void castIgnoreObstacles(long window);
}
