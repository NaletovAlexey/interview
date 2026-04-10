package com.program.training.ya.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Task link:
 * <a href="https://contest.yandex.ru/contest/8458/problems/G/">...</a>
 *
 * If paths satisfying the conditions described above exist,
 * output the minimum number of roads needed to travel from the starting point to the destination.
 * If no path exists, output -1.
 *
 * @author naletov
 */
public class InterestingJourney
{
    public static void main(String[] args) throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int cities = Integer.parseInt(br.readLine());
        Coordinates[] coords = new Coordinates[cities];
        for (int i = 0; i < cities; i++)
        {
            String city =  br.readLine();
            String[]xy = city.trim().split("\\s+");
            coords[i] = new Coordinates(Integer.parseInt(xy[0]), Integer.parseInt(xy[1]));
        }
        long gas = Long.parseLong(br.readLine());
        String[] destination = br.readLine().trim().trim().split("\\s+");
        int startCity = Integer.parseInt(destination[0]) - 1;   // array index
        int finishCity = Integer.parseInt(destination[1]) - 1;

        System.out.println(bfs(coords, cities, gas, startCity, finishCity));
    }

    private static int bfs(Coordinates[] coords, int cities, long gas, int startCity, int finishCity)
    {
        if (startCity == finishCity) return 0;

        int[] ways = new int[cities];
        Arrays.fill(ways, -1);

        Queue<Integer> queue = new LinkedList<>();
        queue.add(startCity);
        ways[startCity] = 0;

        while (!queue.isEmpty())
        {
            int currentCity = queue.poll();
            for (int next = 0; next  < cities; next ++)
            {
                if (ways[next] == -1)
                {
                    long distance = Math.abs(coords[currentCity].x - coords[next].x)
                            + Math.abs(coords[currentCity].y - coords[next].y);
                    if (distance <= gas)
                    {
                        ways[next] = ways[currentCity] + 1;
                        if (next == finishCity)
                            return ways[finishCity];
                        queue.add(next);
                    }
                }
            }
        }
        return -1;
    }

    public record Coordinates(long x, long y)
    {
    }
}
