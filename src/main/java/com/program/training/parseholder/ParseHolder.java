package com.program.training.parseholder;

import java.util.*;

/**
 * Please write a class to parse and handle a sample string in a regular structure allowing:
 *
 * 1. Get a list of possible values for a particular key by 0(1)
 * 2. Generate initial string keeping initial pairs order.
 *
 * String format: pairs delimited by ";", key/value delimited by "="
 * Sample string: "key1=val1;key2=val2;key3= val3;key1=val4;key2=val5;key1=val6"
 *
 * @author naletov
 */

public class ParseHolder
{
    private final Map<String, List<PositionAndValue>> resultMap = new LinkedHashMap<>();

    public void parse(String str)
    {
        String[] valuesArray = str.split(";");

        for (int i = 0; i < valuesArray.length; i++)
        {
            String[] values = valuesArray[i].split("=");
            if (resultMap.containsKey(values[0]))
            {
                resultMap.get(values[0]).add(new PositionAndValue(values[1], i));
            }
            else
            {
                ArrayList<PositionAndValue> firstKey = new ArrayList<>();
                firstKey.add(new PositionAndValue(values[1], i));
                resultMap.put(values[0], firstKey);
            }
        }

    }

    public String generateRecord()
    {
        PriorityQueue<PositionAndValue> itemsWithPos = new PriorityQueue<>(Comparator.comparingInt(o -> o.position));

        for (Map.Entry<String, List<PositionAndValue>> entry : resultMap.entrySet())
        {
            String key = entry.getKey();
            List<PositionAndValue> values = entry.getValue();
            for (PositionAndValue value : values)
            {
                itemsWithPos.offer(new  PositionAndValue(key + "=" + value.value , value.position));
            }
        }

        StringBuilder result = new StringBuilder();
        while (!itemsWithPos.isEmpty())
        {
            PositionAndValue item = itemsWithPos.poll();
            result.append(item.value).append(";");
        }
        return result.toString();
    }

    /** Add methods to have access to your structure
     * returns all values by key
     * @param key String key
     * @return values (e.g.: [val1, val6])
     */

    public Object getValues(String key)
    {
        List<PositionAndValue> values = resultMap.get(key);
        List<String> result = new ArrayList<>();
        for (PositionAndValue value : values)
        {
            result.add(value.value);
        }
        return result;
    }

    /**
     * To store conformance position
     * @param value
     * @param position
     */
    public record PositionAndValue(String value, int position)
    {
    }

    public static class Soolutin
    {
        public static void main(String[] args)
        {
            String testString = "key1=val1;key2=val2;key3=val3;key1=val4;key2=val5;key1=val6";
            ParseHolder parser = new ParseHolder();
            parser.parse(testString);
            System.out.println(parser.getValues ("key1"));
            System.out.println(parser.getValues("key2"));
            System.out.println(parser.getValues ("key3"));
            System.out.println(parser.generateRecord());
        }
    }
}
