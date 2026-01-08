import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int n = readNumber(scanner);

        List<Animal> pets = readPets(scanner, n);

        List<Animal> updatedPets = pets.stream()
                .map(Main::increaseAgeIfNeeded)
                .toList();

        updatedPets.forEach(System.out::println);

        scanner.close();
    }

    private static Animal increaseAgeIfNeeded(Animal animal) {
        if (animal.getAge() > 10) {
            return createAnimalWithNewAge(animal, animal.getAge() + 1);
        }
        return animal;
    }

    private static int readNumber(Scanner scanner) {
        return Stream.of(scanner)
                .map(Main::tryParseInt)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> readNumber(scanner));
    }

    private static Integer tryParseInt(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            System.out.println("Could not parse a number. Please, try again");
            return null;
        }
    }

    private static List<Animal> readPets(Scanner scanner, int n) {
        return Stream.generate(() -> readSinglePet(scanner))
                .limit(n)
                .filter(Objects::nonNull)
                .toList();
    }

    private static Animal readSinglePet(Scanner scanner) {
        String type = scanner.nextLine().trim().toLowerCase();
        if (!type.equals("dog") && !type.equals("cat")) {
            System.out.println("Incorrect input. Unsupported pet type");
            return null;
        }

        String name = scanner.nextLine();
        Integer age = readAge(scanner);

        if (age == null) {
            return null;
        }

        return switch (type) {
            case "dog" -> new Dog(name, age);
            case "cat" -> new Cat(name, age);
            default -> null;
        };
    }

    private static Integer readAge(Scanner scanner) {
        try {
            int age = Integer.parseInt(scanner.nextLine());

            if (age <= 0) {
                System.out.println("Incorrect input. Age <= 0");
                return null;
            }

            return age;
        } catch (Exception e) {
            System.out.println("Could not parse a number. Please, try again");
            return readAge(scanner);
        }
    }

    private static Animal createAnimalWithNewAge(Animal animal, int newAge) {
        if (animal instanceof Dog) {
            return new Dog(animal.getName(), newAge);
        } else if (animal instanceof Cat) {
            return new Cat(animal.getName(), newAge);
        }
        return animal;
    }
}