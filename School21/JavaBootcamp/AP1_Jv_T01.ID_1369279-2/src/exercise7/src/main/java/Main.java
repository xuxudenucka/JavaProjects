import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int a = -1;
        int counter = 0;
        try {
            a = scanner.nextInt();
        } catch (Exception e) {
            System.out.println("Input error");
            return;
        }
        while (true) {
            try {
                int b = scanner.nextInt();
                counter++;
                if (a > b) {
                    System.out.println("The sequence is not ordered from the ordinal number of the number " + counter);
                    return;
                }
                a = b;
            } catch (Exception e) {
                System.out.println("The sequence is ordered in ascending order");
                return;
            }
        }
    }
}
