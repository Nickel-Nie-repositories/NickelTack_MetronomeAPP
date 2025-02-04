package com.example.nickeltack.starting;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum AverageAlgorithm {
    ARITHMETIC_MEAN("算数平均"),
    FIRST_VALUE_FIRST("初值优先"),
    LAST_VALUE_FIRST("末值优先"),
    ONLY_LAST_VALUE("仅限末值"),
    HARMONIC_MEAN("调和平均");


    public static final Map<AverageAlgorithm, Function<long[], Long>> AverageAlgorithms = new HashMap<>() {{
        // 算数平均数：
        put(AverageAlgorithm.ARITHMETIC_MEAN,(long[] numbers) -> {
            long sum = 0;
            for (long num : numbers) {
                sum += num;
            }
            return sum / numbers.length;
        });

        // 初值优先：
        put(AverageAlgorithm.FIRST_VALUE_FIRST,CalculateUtils::firstValueFirstAverage);

        // 末值优先：
        put(AverageAlgorithm.LAST_VALUE_FIRST, CalculateUtils::lastValueFirstAverage);

        // 仅限末值：
        put(AverageAlgorithm.ONLY_LAST_VALUE,(long[] numbers) -> numbers[numbers.length-1] - numbers[numbers.length-2]);

        // 调和平均：
        put(AverageAlgorithm.HARMONIC_MEAN, CalculateUtils::calculateHarmonicMean);
    }};

    private final String description;

    AverageAlgorithm(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static AverageAlgorithm[] getAllAverageModes()
    {
        return AverageAlgorithms.keySet().toArray(new AverageAlgorithm[0]);
    }
}
