package flashcards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    private static Scanner scanner;
    private static Map<String, String> cards;
    private static Map<String, Integer> mistakesList;
    private static int maxMistakes = 0;
    private static List<String> logList;
    private static String importFile = "";
    private static String exportFile = "";

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        cards = new TreeMap<>();
        mistakesList = new HashMap<>();
        logList = new ArrayList<>();
        boolean exit = false;
        String option;

        for (int i = 0; i < args.length; i += 2) {
            switch (args[i]) {
                case "-import":
                    if (args[i + 1] != null) {
                        importFile = args[i + 1];
                    }
                    break;
                case "-export":
                    if (args[i + 1] != null) {
                        exportFile = args[i + 1];
                    }
                    break;
                default:
            }
        }

        if (!"".equals(importFile)) {
            importCards(importFile);
        }

        do {
            show("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
            option = scanner.nextLine();
            switch (option) {
                case "add":
                    addCard();
                    break;
                case "remove":
                    removeCard();
                    break;
                case "import":
                    show("File name:");
                    String path = scanner.nextLine();
                    importCards(path);
                    break;
                case "export":
                    show("File name:");
                    String pathToExport = scanner.nextLine();
                    exportCards(pathToExport);
                    break;
                case "ask":
                    ask();
                    break;
                case "log":
                    saveLogs();
                    break;
                case "hardest card":
                    hardestCard();
                    break;
                case "reset stats":
                    mistakesList.clear();
                    maxMistakes = 0;
                    show("Card statistics has been reset.");
                    break;
                case "exit":
                    show("Bye bye!");
                    if (!"".equals(exportFile)) {
                        exportCards(exportFile);
                    }
                    exit = true;
            }
        } while (!exit);
    }

    private static void addCard() {
        show("The card:");
        String term = scanner.nextLine();
        if (cards.containsKey(term)) {
            show("The card \"" + term + "\" already exists.\n");
            return;
        }
        show("The definition of the card:");
        String definition = scanner.nextLine();
        if (cards.containsValue(definition)) {
            show("The definition \"" + definition + "\" already exists.\n");
            return;
        }
        cards.put(term, definition);
        show("The pair (\"" + term + "\":\"" + definition + "\") has been added.\n");
    }

    private static void removeCard() {
        show("The card:");
        String term = scanner.nextLine();
        if (cards.containsKey(term)) {
            cards.remove(term);
            if (mistakesList.get(term) == maxMistakes) {
                maxMistakes = getMaxMistake();
            }
            mistakesList.remove(term);
            show("The card has been removed.\n");
        } else {
            show("Can't remove \"" + term + "\": there is no such card.\n");
        }
    }

    private static void importCards(String path) {
        File file = new File(path);
        int count = 0;
        int num;
        String[] map;
        try (Scanner scannerFile = new Scanner(file)) {
            while (scannerFile.hasNext()) {
                map = scannerFile.nextLine().split("\\|");
                num = Integer.parseInt(map[2]);
                if (cards.containsKey(map[0])) {
                    cards.replace(map[0], map[1]);
                    if (num > 0) {
                        mistakesList.replace(map[0], num);
                    }
                } else {
                    cards.put(map[0], map[1]);
                    if (num > 0) {
                        mistakesList.put(map[0], num);
                    }
                }
                if (num > maxMistakes) {
                    maxMistakes = num;
                }
                count++;
            }
            show(count + " cards have been loaded.\n");
        } catch (FileNotFoundException e) {
            show("File not found.\n");
        }
    }

    private static void exportCards(String path) {
        int countExported = 0;
        File file = new File(path);
        try (PrintWriter printWriter = new PrintWriter(file)) {
            for (Map.Entry<String, String> entry : cards.entrySet()) {
                printWriter.printf("%s|%s|%s\n", entry.getKey(), entry.getValue(), mistakesList.getOrDefault(entry.getKey(), 0));
                countExported++;
            }
        } catch (IOException e) {
            show("An exception occurs " + e.getMessage());
        }
        show(countExported + " cards have been saved.\n");
    }

    private static String generateRandomKeyCard() {
        int position = new Random().nextInt(cards.size());
        String[] key = cards.keySet().toArray(new String[0]);
        return key[position];
    }

    private static void ask() {
        show("How many times to ask?");
        int times = Integer.parseInt(scanner.nextLine());
        String key;
        String answer;
        int numMistake = 0;
        for (int i = 0; i < times; i++) {
            key = generateRandomKeyCard();
            show("Print the definition of \"" + key + "\":");
            answer = scanner.nextLine();
            if (cards.get(key).equals(answer)) {
                show("Correct answer.\n");
            } else {
                if (cards.containsValue(answer)) {
                    show("Wrong answer. The correct one is \"" + cards.get(key)
                            + "\", you've just written the definition of \"" +  getKey(cards, answer) + "\".");
                } else {
                    show("Wrong answer. The correct one is \"" + cards.get(key) + "\".");
                }

                if (mistakesList.containsKey(key)) {
                    numMistake = mistakesList.get(key) + 1;
                    mistakesList.replace(key, numMistake);
                } else {
                    mistakesList.put(key, 1);
                }

                if (numMistake > maxMistakes) {
                    maxMistakes = numMistake;
                }
            }
        }
    }

    private static void show(String line) {
        System.out.println(line);
        logList.add(line);
    }

    private static void saveLogs() {
        show("File name:");
        String path = scanner.nextLine();
        File file = new File(path);
        try (PrintWriter printWriter = new PrintWriter(file)) {
            for (String line : logList) {
                printWriter.printf(line + "\n");
            }
        } catch (IOException e) {
            show("An exception occurs " + e.getMessage());
        }
        show("The log has been saved.");
    }

    private static void hardestCard() {
        if (mistakesList.size() == 0) {
            show("There are no cards with errors.\n");
        } else {
            int countMistakes = 0;
            StringBuilder mistakes = new StringBuilder();
            for (Map.Entry<String, Integer> entry : mistakesList.entrySet()) {
                if (entry.getValue() == maxMistakes) {
                    mistakes.append("\"").append(entry.getKey()).append("\"");
                    countMistakes++;
                }
            }
            show("The hardest " + (countMistakes > 1 ? " cards are " : "card is ") + mistakes
                    + ". You have " + maxMistakes + " errors answering "
                    + (countMistakes > 1 ? "them" : "it") + ".\n");
        }
    }

    private static int getMaxMistake() {
        int max = 0;
        int num;
        for (Map.Entry<String, Integer> entry : mistakesList.entrySet()) {
            num = entry.getValue();
            if (num > max) {
                max = num;
            }
        }
        return max;
    }

    private static <K, V> K getKey(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}