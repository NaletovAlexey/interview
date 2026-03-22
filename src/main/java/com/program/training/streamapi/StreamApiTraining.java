package com.program.training.streamapi;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author naletov
 */
public class StreamApiTraining {
    public List<Integer> getListOfEvenNums(List<Integer> nums)
    {
        return nums.stream().filter(n -> n % 2 == 0).toList();
    }

    public List<String> getARows(List<String> nums)
    {
        return nums.stream().filter(s -> s.substring(0, 1).equalsIgnoreCase("A")).map(String::toUpperCase).distinct().toList();
    }

    public List<Employee> getMergeLists()
    {
        List<Department> departments = List.of(
                new Department("IT", List.of(
                        new Employee("Alice", 29, 5000),
                        new Employee("Bob", 35, 6000),
                        new Employee("Charlie", 24, 3000)
                )),
                new Department("HR", List.of(
                        new Employee("Diana", 42, 4500),
                        new Employee("Eve", 22, 2800)
                )),
                new Department("Finance", List.of(
                        new Employee("Frank", 50, 7000),
                        new Employee("Grace", 30, 5500)
                ))
        );

        return departments.stream().flatMap(dep -> dep.getEmployees().stream()).toList();
    }

    public static Map<String, Double> calculatePositions(List<Transaction> transactions) {
        Map<String, Double> positionMap = new HashMap<>();

        for (Transaction transaction : transactions) {
            positionMap.merge(transaction.securityId(),
                    transaction.transactionType().getValue() *
                    transaction.quantity(), Double::sum);

       }
        return positionMap;
    }

    public Map<String, Double> calculatePositionsStream(List<Transaction> transactions) {
        return transactions.stream().
                collect(Collectors.groupingBy(Transaction::securityId,
                        Collectors.summingDouble(t -> t.quantity()*t.transactionType().getValue())));
    }

    // Testing
    public static void main(String[] args)
    {
        StreamApiTraining  streamApiTraining = new StreamApiTraining();
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("11111", 10, new Date(), TransactionType.BUY));
        transactions.add(new Transaction("11111", 5, new Date(), TransactionType.SELL));
        transactions.add(new Transaction("11111", 3, new Date(), TransactionType.BUY));
        transactions.add(new Transaction("22222", 7, new Date(), TransactionType.BUY));
        transactions.add(new Transaction("22222", 11, new Date(), TransactionType.SELL));
        transactions.add(new Transaction("33333", 3, new Date(), TransactionType.BUY));
        transactions.add(new Transaction("33333", 7, new Date(), TransactionType.SELL));
        transactions.add(new Transaction("33333", 9, new Date(), TransactionType.SELL));
        transactions.add(new Transaction("33333", 8, new Date(), TransactionType.BUY));
        transactions.add(new Transaction("33333", 5, new Date(), TransactionType.BUY));
        transactions.add(new Transaction("33333", 2, new Date(), TransactionType.SELL));

        System.out.println(streamApiTraining.calculatePositions(transactions).size());
        System.out.println(streamApiTraining.calculatePositionsStream(transactions).size());
        System.out.println(streamApiTraining.calculatePositions(transactions).get("33333"));
        System.out.println(streamApiTraining.calculatePositionsStream(transactions).get("33333"));
    }

}

