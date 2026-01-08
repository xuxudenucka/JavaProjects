import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int n = readNumberOfAnimals(scanner);
        List<Animal> animals = readAnimals(scanner, n);
        printAnimals(animals);
    }

    private static int readNumberOfAnimals(Scanner scanner) {
        int n;
        while (true) {
            try {
                n = Integer.parseInt(scanner.nextLine());
                return n;
            } catch (Exception e) {
                System.out.println("Could not parse a number. Please, try again");
            }
        }
    }

    private static List<Animal> readAnimals(Scanner scanner, int n) {
        List<Animal> list = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            String type = scanner.nextLine().toLowerCase();

            if (!type.equals("dog") && !type.equals("cat")) {
                System.out.println("Incorrect input. Unsupported pet type");
                continue;
            }

            String name = scanner.nextLine();
            Integer age = readAge(scanner);

            if (age != null) {
                if (type.equals("dog")) {
                    list.add(new Dog(name, age));
                } else {
                    list.add(new Cat(name, age));
                }
            }
        }

        return list;
    }

    private static Integer readAge(Scanner scanner) {
        while (true) {
            try {
                int age = Integer.parseInt(scanner.nextLine());
                if (age <= 0) {
                    System.out.println("Incorrect input. Age <= 0");
                    return null;
                }
                return age;
            } catch (Exception e) {
                System.out.println("Could not parse a number. Please, try again");
            }
        }
    }

    private static void printAnimals(List<Animal> animals) {
        for (Animal pet : animals) {
            System.out.println(pet.toString());
        }
    }
}
