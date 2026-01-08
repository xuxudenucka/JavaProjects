import java.util.Scanner;

public class Main {
    private static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    private static boolean isTriangle(double a, double b, double c) {
        return a + b > c && a + c > b && b + c > a;
    }
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        double x1, y1, x2, y2, x3, y3;

        while (true) {
            try {
                x1 = scanner.nextDouble();
                y1 = scanner.nextDouble();

                x2 = scanner.nextDouble();
                y2 = scanner.nextDouble();

                x3 = scanner.nextDouble();
                y3 = scanner.nextDouble();

                break;
            } catch (Exception e) {
                System.out.println("Could not parse a number. Please, try again");
                scanner.nextLine();
            }
        }
        double sideA = distance(x1, y1, x2, y2);
        double sideB = distance(x2, y2, x3, y3);
        double sideC = distance(x3, y3, x1, y1);

        if (isTriangle(sideA, sideB, sideC)) {
            double perimeter = sideA + sideB + sideC;
            System.out.printf("Perimeter = %.3f%n", perimeter);
        } else {
            System.out.println("It's not a triangle");
        }

        scanner.close();
    }
}
