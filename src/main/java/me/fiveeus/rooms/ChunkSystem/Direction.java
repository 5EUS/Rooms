package me.fiveeus.rooms.ChunkSystem;

import java.util.ArrayList;
import java.util.List;

import static me.fiveeus.rooms.ChunkSystem.Direction.Directions.*;

public class Direction {

    public enum Directions {

        NORTH, SOUTH, EAST, WEST;

    }

    public static Directions getOpposite(Directions direction) {

        switch (direction) {

            case NORTH:
                return SOUTH;

            case SOUTH:
                return NORTH;

            case EAST:
                return WEST;

            case WEST:
                return EAST;

            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);

        }
    }

    public static Directions fromString(String direction) {

        switch (direction.toUpperCase()) {

            case "NORTH":
                return NORTH;

            case "SOUTH":
                return SOUTH;

            case "EAST":
                return EAST;

            case "WEST":
                return WEST;

            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);

        }
    }

    public static Directions getEdge(int i, int j, int width, int length) {
        // Determine if (i, j) is on one of the edges
        if (j == 0 && i >= 0 && i < width) {
            return Directions.NORTH;
        } else if (j == length - 1 && i >= 0 && i < width) {
            return Directions.SOUTH;
        } else if (i == 0 && j >= 0 && j < length ) {
            return Directions.WEST;
        } else if (i == width - 1 && j >= 0 && j < length) {
            return Directions.EAST;
        } else {
            return null;
        }
    }

    public static Directions getRotated90(Directions direction) {

        switch (direction) {
            case NORTH:
                return WEST;

            case SOUTH:
                return EAST;

            case EAST:
                return NORTH;

            case WEST:
                return SOUTH;

            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);
        }

    }

    public static List<Directions> getCornerDirectionList(int corner) {

        List<Directions> directions = new ArrayList<>();
        switch (corner) {

            case 0:
                directions.add(NORTH);
                directions.add(WEST);
                break;

            case 1:
                directions.add(SOUTH);
                directions.add(WEST);
                break;

            case 2:
                directions.add(NORTH);
                directions.add(EAST);
                break;

            case 3:
                directions.add(SOUTH);
                directions.add(EAST);
                break;

        }

        return directions;
    }


    public static boolean hasCommonElement(List<?> list1, List<?> list2) {
        // Create copies of the lists to avoid modifying the original lists
        List<?> copyList1 = new ArrayList<>(list1);
        List<?> copyList2 = new ArrayList<>(list2);

        // Use retainAll method to find common elements
        copyList1.retainAll(copyList2);

        // If copyList1 is not empty, it means there are common elements
        return !copyList1.isEmpty();
    }
}
