package com.boyi.common.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtils {
    public static ExecutorService executorService = Executors.newFixedThreadPool(5);

}
