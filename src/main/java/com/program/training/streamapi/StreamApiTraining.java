package com.program.training.streamapi;

import java.security.SecureRandom;
import java.util.List;

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
}

