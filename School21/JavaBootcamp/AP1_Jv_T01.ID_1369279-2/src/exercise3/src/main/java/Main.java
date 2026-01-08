import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = -1;
        while (true) {
            try {
                n = scanner.nextInt();
                if (n <= 0) {
                    System.out.println("Input error. Size <= 0");
                    return;
                }
                break;
            } catch (Exception e) {
                System.out.println("Could not parse a number. Please, try again");
            }
        }
        int[] array = new int[n];
        int sumNegative = 0;
        int countNegative = 0;
        for (int i = 0; i < n; i++) {
            while (true) {
                try {
                    array[i] = scanner.nextInt();
                    if (array[i] < 0) {
                        sumNegative += array[i];
                        countNegative++;
                    }
                    break;
                } catch (Exception e) {
                    System.out.println("Could not parse a number. Please, try again");
                }
            }
        }
        if (countNegative > 0) {
            int result = sumNegative / countNegative;
            System.out.println(result);
        } else {
            System.out.println("There are no negative elements");
        }
    }
}
