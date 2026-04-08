package com.program.training.ya.tasks;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Task link:
 * <a href="https://contest.yandex.ru/contest/8458/problems/B/?utm_source=habr&utm_content=post070519">...</a>
 *
 * You need to find the longest sequence of ones in a binary vector and print its length.
 *
 * It is desirable to obtain a solution that runs in linear time and at the same time passes through the input array only once.
 *
 * @author naletov
 */
public class ConsecutiveUnits
{
    public static void main(String[] args) throws Exception
    {
        // The first line of the input file contains one number n, n ≤ 10000.
        // Each of the next n lines contains exactly one number - an alternating element of the array.
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        int n = Integer.parseInt(br.readLine());
        int[] arr = new int[n];
        for (int i =0; i < n; i++)
        {
            arr[i] = Integer.parseInt(br.readLine());
        }

        int result = 0;
        int currResult = 0;
        for (int i = 0; i < n; i++)
        {
            if (arr[i] == 1)
            {
                currResult++;
                result = Math.max(currResult, result);
            }
            else
            {
                currResult = 0;
            }
        }
        System.out.println(result);
    }
}
