package com.rogue.domain.entities.monsters;

import com.rogue.domain.entities.MonsterType;
import com.rogue.domain.util.Position;

import java.util.List;

public class Ogre extends Monster {
    private int directionIndex = RANDOM.nextInt(4);
    private int restCounter = 0;
    private static final int[][] CARDINAL = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    public Ogre(int x, int y) {
        super(MonsterType.OGRE, x, y);
    }

    public int getDirectionIndex() {
        return directionIndex;
    }

    public void setDirectionIndex(int directionIndex) {
        this.directionIndex = Math.floorMod(directionIndex, CARDINAL.length);
    }

    public int getRestCounter() {
        return restCounter;
    }

    public void setRestCounter(int restCounter) {
        this.restCounter = Math.max(0, restCounter);
    }

    public void triggerRest() {
        this.restCounter = 1;
    }

    @Override
    protected Position choosePatternStep(char[][] map, List<Monster> monsters) {
        if (restCounter > 0) {
            restCounter--;
            return null;
        }
        for (int i = 0; i < CARDINAL.length; i++) {
            int currentIndex = (directionIndex + i) % CARDINAL.length;
            int[] dir = CARDINAL[currentIndex];
            int midX = getX() + dir[0];
            int midY = getY() + dir[1];
            int endX = getX() + dir[0] * 2;
            int endY = getY() + dir[1] * 2;
            if (isStepFree(midX, midY, map, monsters) && isStepFree(endX, endY, map, monsters)) {
                directionIndex = currentIndex;
                return new Position(endX, endY);
            }
        }
        return null;
    }
}
