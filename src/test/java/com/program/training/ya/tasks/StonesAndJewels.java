package com.program.training.ya.tasks;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Task link:
 * <a href="https://contest.yandex.ru/contest/8458/problems/A/?utm_source=habr&utm_content=post070519">...</a>

 * Given two strings of lowercase Latin characters: string J and string S.
 * The characters included in string J are “jewels,” and those included in string S are “stones.”
 * We need to determine how many symbols from S are simultaneously “jewels”.
 * Simply put, you need to check how many characters from S are included in J.
 *
 * @author naletov
 */
public class StonesAndJewels
{
    public static void main(String[] args) throws Exception
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String j = br.readLine();
        String s = br.readLine();

        int result = 0;
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if (j.indexOf(c) >= 0)
                result++;
        }
        System.out.println(result);
    }
}
