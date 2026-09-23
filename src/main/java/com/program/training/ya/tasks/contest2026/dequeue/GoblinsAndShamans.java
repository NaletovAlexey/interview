package com.program.training.ya.tasks.contest2026.dequeue;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Гоблины и шаманы
 * <a href="https://new.contest.yandex.ru/contests/97109/problems?id=30404%2F2023_01_27%2FCuRXCfvxXa">...</a>
 *
 * Пример
 * Ввод:
 * 7
 * + 1
 * + 2
 * -
 * + 3
 * + 4
 * -
 * -
 * Вывод:
 * 1
 * 2
 * 3
 *
 * @author naletov
 */
public class GoblinsAndShamans
{
    private static final String PUSH_BACK = "+";
    private static final String PUSH_MIDDLE = "*";
    private static final String POP_FRONT = "-";

    public static void main(String[] args) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        int n = Integer.parseInt(reader.readLine().trim());

        Deque<Integer> left = new ArrayDeque<>();
        Deque<Integer> right = new ArrayDeque<>();

        for (int q = 0; q < n; q++)
        {
            String[] parts = reader.readLine().trim().split("\\s+");
            String type = parts[0];

            switch (type) {
                case PUSH_BACK -> {
                    int i = Integer.parseInt(parts[1]);
                    right.addLast(i);
                    rebalanceQueues(left, right);
                }
                case PUSH_MIDDLE -> {
                    int i = Integer.parseInt(parts[1]);
                    left.addLast(i);
                    rebalanceQueues(left, right);
                }
                case POP_FRONT -> {
                    int goblin;
                    if (!left.isEmpty()) {
                        goblin = left.pollFirst();
                    } else {
                        goblin = right.pollFirst();
                    }
                    rebalanceQueues(left, right);

                    writer.write(String.valueOf(goblin));
                    writer.newLine();
                }
            }
        }

        reader.close();
        writer.close();
    }

    private static void rebalanceQueues(Deque<Integer> left, Deque<Integer> right)
    {
        if (left.size() - right.size() > 1)
        {
            right.addFirst(left.pollLast());
        }
        else if (right.size() > left.size())
        {
            left.addLast(right.pollFirst());
        }
    }
}
