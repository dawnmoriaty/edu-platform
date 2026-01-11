package com.eduplatform.crmbase;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RxJavaConfiguration {

    @Bean("ioScheduler")
    public Scheduler ioScheduler() {
        return Schedulers.io();
    }

    @Bean("computationScheduler")
    public Scheduler computationScheduler() {
        return Schedulers.computation();
    }

    @Bean("singleScheduler")
    public Scheduler singleScheduler() {
        return Schedulers.single();
    }
}
