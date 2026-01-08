import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<User> users = new ArrayList<>();
        int n;
        try {
            n = Integer.parseInt(scanner.nextLine());
            if (n <= 0) {
                System.out.println("Input error. Size <= 0");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Could not parse a number. Please, try again");
            return;
        }
        int i = 0;
        while (i < n) {
            String name = scanner.nextLine();
            String ageInput = scanner.nextLine();
            boolean validInput = false;
            try {
                int age = Integer.parseInt(ageInput);
                if (age <= 0) {
                    System.out.println("Incorrect input. Age <= 0");
                } else {
                    users.add(new User(name, age));
                    validInput = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Could not parse a number. Please, try again");
            }
            if (validInput) {
                i++;
            }
        }

        List<String> adultNames = users.stream()
                .filter(u -> u.getAge() >= 18)
                .map(User::getName)
                .toList();

        if (!adultNames.isEmpty()) {
            System.out.println(String.join(", ", adultNames));
        }
    }
}
