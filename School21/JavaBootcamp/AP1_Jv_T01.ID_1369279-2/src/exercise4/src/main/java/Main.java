import java.util.Scanner;

public class Main {
    public static boolean isAnswer(int number) {
        int lastDigit = number % 10;
        while (number >= 10) {
            number /= 10;
        }
        int firstDigit = number;
        return firstDigit == lastDigit;
    }
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
        int[] result = new int[n];
        int countResult = 0;
        int i = 0;
        while (i < n) {
            try {
                array[i] = scanner.nextInt();
                if (isAnswer(array[i])) {
                    result[countResult] = array[i];
                    countResult++;
                }
                i++;
            } catch (Exception e) {
                System.out.println("Could not parse a number. Please, try again");
            }
        }
        if (countResult > 0) {
            for (int j = 0; j < countResult; j++) {
                System.out.print(result[j] + " ");
            }
            System.out.println();
        } else {
            System.out.println("There are no such elements");
        }
    }
}
