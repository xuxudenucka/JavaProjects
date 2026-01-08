import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = -1;

        while (true) {
            try {
                n = scanner.nextInt();
                if (n < 0) {
                    System.out.println("Number must be non-negative. Please, try again.");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Could not parse a number. Please, try again");
            }
        }

        try {
            int result = fibonacci(n);
            System.out.println(result);
        } catch (StackOverflowError e) {
            System.out.println("Recursion depth limit exceeded. Try a smaller number.");
        } catch (ArithmeticException e) {
            System.out.println("Overflow occurred while calculating Fibonacci number.");
        }
    }

    public static int fibonacci(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        int a = fibonacci(n - 1);
        int b = fibonacci(n - 2);
        if (a > Integer.MAX_VALUE - b) {
            throw new ArithmeticException("Integer overflow");
        }
        return a + b;
    }
}

