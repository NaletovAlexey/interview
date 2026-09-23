package com.program.training.ya.tasks.contest2026.monostack;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Великое Лайнландское переселение
 * <a href="https://new.contest.yandex.ru/contests/97106/problems?id=30404%2F2023_01_18%2Fm3PAau25IW">...</a>
 *
 * Пример:
 * req: 10; 1 2 3 2 1 4 2 5 3 1 -> resp: -1 4 3 4 -1 6 9 8 9 -1
 *
 * @author naletov
 */
public class GreatLinelandMigration
{
    public static void main(String[] args) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
        // read data
        int n = Integer.parseInt(reader.readLine().trim());
        long[] cities = Stream.of(reader.readLine().trim()
                            .split("\\s+")).mapToLong(Long::parseLong).toArray();

        int[] answer = new int[n];
        Arrays.fill(answer, -1);   // -1 default if
        // Stack to persist cities indices without answer
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++)
        {
            while (!stack.isEmpty() && cities[i] < cities[stack.peek()])
            {
                int id = stack.pop();
                answer[id] = i;
            }
            stack.push(i);
        }

        writer.write(Arrays.stream(answer).mapToObj(String::valueOf)
                        .collect(Collectors.joining(" ")));
        reader.close();
        writer.close();
    }
}
