package com.program.training.ya.tasks.contest2026.expressions;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Постфиксная запись
 * <a href="https://new.contest.yandex.ru/contests/97105/problems?id=30404%2F2023_01_18%2FqS3OsBniXH">...</a>
 * Пример 8 9 + 1 7 - * = -102
 * @author naletov
 */
public class PostfixRecord {
    private static final String PLUS = "+";
    private static final String MUNUS = "-";
    private static final String MULTIPLY = "*";
    private static final String DIVIDE = "/";

    public static void main(String[] args) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        String record = reader.readLine();
        String[] symbols = record.trim().split("\\s+");     // split by one or more whitespace characters
        Deque<Long> stack = new ArrayDeque<>();

        for (String symbol : symbols)
        {
                long result = switch (symbol) {
                    case PLUS -> {
                        long b = stack.pop();
                        long a = stack.pop();
                        yield a + b;
                    }
                    case MUNUS -> {
                        long b = stack.pop();
                        long a = stack.pop();
                        yield a - b;
                    }
                    case MULTIPLY -> {
                        long b = stack.pop();
                        long a = stack.pop();
                        yield a * b;
                    }
                    case DIVIDE ->
                    {
                        long b = stack.pop();
                        long a = stack.pop();
                        yield a / b;
                    }
                    default -> Long.parseLong(symbol);
                };
                stack.push(result);
        }
        writer.write(String.valueOf(stack.pop()));
        reader.close();
        writer.close();
    }
}
