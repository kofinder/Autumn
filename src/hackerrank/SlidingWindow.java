package hackerrank;

import java.util.Collections;
import java.util.List;

public class SlidingWindow {
    private static String CLASS_INFO;
    private List<Integer> arr;
    private int targetSum;
    private int segmentLength;

    static {
        CLASS_INFO = "SlidingWindow - HackerRank Chocolate Problem Solver";
        System.out.println(CLASS_INFO);
    }

    {
        System.out.println("Preparing to create a new SlidingWindow instance...");
        targetSum = 0;
        segmentLength = 0;
        arr = Collections.emptyList();
    }

    public SlidingWindow(List<Integer> arr, int targetSum, int segmentLength) {
        System.out.println("Initializing SlidingWindow object...");
        this.arr = arr;
        this.targetSum = targetSum;
        this.segmentLength = segmentLength;
    }

    public int execute() {

        if (arr.isEmpty() || segmentLength <= 0) {
            return 0;
        }

        int count = 0;
        int currentSum = 0;

        for (int i = 0; i < segmentLength && i < arr.size(); i++) {
            currentSum += arr.get(i);
        }

        if (currentSum == targetSum) {
            count++;
        }

        for (int i = segmentLength; i < arr.size(); i++) {
            currentSum += arr.get(i) - arr.get(i - segmentLength);
            if (currentSum == targetSum) {
                count++;
            }
        }

        return count;
    }

    public static void main(String[] args) {
        int day = 3;
        int month = 2;
        var chocolate = List.of(1, 2, 1, 3, 2);

        var solver = new SlidingWindow(chocolate, day, month);

        int result = solver.execute();

        System.out.println("Valid segments: " + result);

    }
}
