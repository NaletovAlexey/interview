package com.program.training.ya.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Task link:
 * <a href="https://contest.yandex.ru/contest/8458/problems/C/?utm_source=habr&utm_content=post070519">...</a>
 *
 * Given a non-decreasing array of 32-bit integers. It is required to remove all repetitions from it.
 *
 * It is desirable to obtain a solution that does not read the entire input file into memory,
 * i.e., uses only a constant amount of memory during operation.
 *
 * @author naletov
 */
public class DuplicatesRemoving
{
    public static void main(String[] args) throws IOException
    {
        // The first line of the input file contains a single number n, n ≤ 1000000.
        // The next n lines contain numbers - array elements, one per line. The numbers are sorted in non-descending order.
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        int n = Integer.parseInt(br.readLine());
        if (n <= 0) return;

        int[] arr = new int[n];
        for (int i = 0; i < n; i++)
        {
            arr[i] = Integer.parseInt(br.readLine());
        }

        int currentItem = arr[0];
        System.out.println(currentItem);
        for (int i = 1; i < n; i++)
        {
            if (currentItem != arr[i])
            {
                currentItem =  arr[i];
                System.out.println(currentItem);
            }
        }
    }
}
