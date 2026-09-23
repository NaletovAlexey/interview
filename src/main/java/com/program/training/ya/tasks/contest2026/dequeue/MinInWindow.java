package com.program.training.ya.tasks.contest2026.dequeue;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Минимум в окне
 * <a href="https://new.contest.yandex.ru/contests/97109/problems?id=40119%2F2021_06_15%2F99tiG1bl65">...</a>
 *
 * Пример:
 * 7 3
 * 1 3 2 4 5 3 1 -> 1 2 2 3 1
 *
 * @author naletov
 */
public class MinInWindow
{

    public static void main(String[] args) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        String[] parameters = reader.readLine().trim().split("\\s+");
        int n = Integer.parseInt(parameters[0]);
        int k = Integer.parseInt(parameters[1]);

        String[] sequence = reader.readLine().trim().split("\\s+");
        int[]nums = new int[n];

        for (int i = 0; i < n; i++)
        {
            nums[i] =  Integer.parseInt(sequence[i]);
        }

        Deque<Integer> deque = new ArrayDeque<>();  // items indexes
        int[] result = new int[n - k + 1];

        for (int i = 0; i < n; i++)
        {
            // Removing element that extend beyond the window boundaries
            while (!deque.isEmpty() && deque.peekFirst() < i - k + 1)
            {
                deque.pollFirst();
            }

            // Remove elements greater than or equal to the current one (from the right)
            while (!deque.isEmpty() && nums[deque.peekLast()] >= nums[i])
            {
                deque.pollLast();
            }

            deque.offerLast(i);  // Add the current index

            // forming the result from position i = k - 1
            if (i >= k - 1)
            {
                result[i - k + 1] = nums[deque.peekFirst()];
            }
        }

        for (int i = 0; i < n - k + 1; i++)
        {
            writer.write(String.valueOf(result[i]));
            writer.newLine();
        }

        reader.close();
        writer.close();
    }
}
