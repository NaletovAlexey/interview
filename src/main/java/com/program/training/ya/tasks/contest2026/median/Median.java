package com.program.training.ya.tasks.contest2026.median;

import java.io.*;
import java.util.Collections;
import java.util.PriorityQueue;

/**
 * Медиана в окне
 * <a href="https://new.contest.yandex.ru/contests/97111/problems?id=4105%2F2015_10_19%2FfnOQhIILXx>...</a>
 * <p>
 * Пример
 * Ввод:
 * 5
 * 1 5 2 4 3
 * Вывод:
 * 1 1 2 2 3
 *
 * @author naletov
 */
public class Median
{
    public static void main(String[] args) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));

        int count = Integer.parseInt(reader.readLine().trim());
        StringBuilder result = new StringBuilder();

        if (count > 0)
        {
            String[] sequence = reader.readLine().trim().split("\\s+");

            // stores the smaller half of the numbers; the root is the maximum
            PriorityQueue<Integer> heapMax = new PriorityQueue<>(Collections.reverseOrder());
            // stores the larger half of the numbers, the top is the minimum
            PriorityQueue<Integer> heapMin = new PriorityQueue<>();

            for (int i = 0; i < count; i++)
            {
                int item = Integer.parseInt(sequence[i]);

                if (heapMax.isEmpty() || item <= heapMax.peek())
                {
                    heapMax.add(item);
                }
                else
                {
                    heapMin.add(item);
                }

                // rebalance: lower.size() must be upper.size() or > by 1
                if (heapMax.size() > heapMin.size() + 1)
                {
                    heapMin.add(heapMax.poll());
                }
                else if (heapMin.size() > heapMax.size())
                {
                    heapMax.add(heapMin.poll());
                }

                // median - top of heapMax
                result.append(heapMax.peek());
                if (i < count - 1)
                {
                    result.append(' ');
                }
            }
        }

        writer.write(result.toString());

        reader.close();
        writer.close();
    }
}

