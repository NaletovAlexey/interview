package com.program.training.ya.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Task link:
 * <a href="https://contest.yandex.ru/contest/8458/problems/E/">...</a>
 *
 * Given two strings consisting of lowercase Latin letters,
 * the task is to determine whether these strings are anagrams,
 * i.e., whether they differ only in the order of the characters.
 *
 * @author naletov
 */
public class Anagrams
{
    public static void main(String[] args) throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        // 1st approach
//        char[] first = br.readLine().toLowerCase().toCharArray();
//        char[] second = br.readLine().toLowerCase().toCharArray();
//
//        if (first.length != second.length)
//        {
//            System.out.print("0");
//            return;
//        }

//        Arrays.sort(first);
//        Arrays.sort(second);
//        System.out.print(Arrays.equals(first, second) ? "1" : "0");
        // 2d approach

        String first = br.readLine();
        String second = br.readLine();
        if (first.length() != second.length())
        {
            System.out.print("0");
            return;
        }
        StringBuilder sb = new StringBuilder(first);
        for (int i = 0; i < second.length(); i++)
        {
            int index = sb.indexOf(String.valueOf(second.charAt(i)));
            if (index != -1)
                sb.deleteCharAt(index);
            else
            {
                System.out.print("0");
                return;
            }
        }
        System.out.print("1");
    }
}
