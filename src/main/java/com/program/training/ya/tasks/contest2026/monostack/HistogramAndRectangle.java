package com.program.training.ya.tasks.contest2026.monostack;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.stream.Stream;

/**
 * Гистограмма и прямоугольник
 * <a href="https://new.contest.yandex.ru/contests/97106/problems?id=30404%2F2023_01_18%2FEJ31cTOEJL">...</a>
 *
 * Пример:
 * req: 7 2 1 4 5 1 3 3 -> resp: 8
 *
 * @author naletov
 */
public class HistogramAndRectangle
{
    public static void main(String[] args) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
        // read data
        long[] all = Stream.of(reader.readLine().trim().split("\\s+")).mapToLong(Long::parseLong).toArray();

        int n = (int)all[0];
        long[] rectangles = Arrays.copyOfRange(all, 1, all.length);

        int[] left = new int[n];   // nearest index to the left with a smaller height
        int[] right = new int[n];  // nearest index to the right with a smaller height
        Deque<Integer> stack = new ArrayDeque<>();

        // from left to right: find left[i]
        for (int i = 0; i < n; i++)
        {
            while (!stack.isEmpty() && rectangles[stack.peek()] >= rectangles[i])
            {
                stack.pop();
            }
            left[i] = stack.isEmpty() ? -1 : stack.peek();
            stack.push(i);
        }

        stack.clear();

        //  from right to left: find right[i]
        for (int i = n - 1; i >= 0; i--)
        {
            while (!stack.isEmpty() && rectangles[stack.peek()] >= rectangles[i])
            {
                stack.pop();
            }
            right[i] = stack.isEmpty() ? n : stack.peek();  // n - to include the last rectangle S
            stack.push(i);
        }

        // find max S
        long maxS = 0;
        for(int i = 0; i < rectangles.length; i++)
        {
            long width = right[i] - left[i] - 1;
            long currentS = rectangles[i] * width;
            if (currentS > maxS)
            {
                maxS = currentS;
            }
        }

        writer.write(String.valueOf(maxS));

        reader.close();
        writer.close();
    }
}
