package ma.enset.service;

import ma.enset.entities.PageEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Date;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Configuration
public class PageEventService {
    //@Bean
    public Consumer<PageEvent> pageEventConsumer(){
        return (pageEvent -> {
            System.out.println("******------************");
            System.out.println(pageEvent.toString());
            System.out.println("*******-----**********");
        });
    }
    //@KafkaListener(topics = "R41", groupId = "myGroup")
    public void consumer(ConsumerRecord<String, PageEvent> record){
        System.out.println("***********");
        System.out.println(record.key());
        System.out.println(record.value().toString());
        System.out.println("***********");
    }
    @Bean
    public Supplier<PageEvent> pageEventSupplier(){
        return ()->{
            return PageEvent.builder()
                    .name((Math.random()>0.5)?"P1":"P2")
                    .user((Math.random()>0.5)?"U1":"U2")
                    .date(new Date())
                    .duration(new Random().nextInt(1000))
                    .build();
        };
    }

    /*@Bean
    public Function<PageEvent,PageEvent> pageEventFunction(){
        return (input)->{
            input.setName("L:"+input.getName().length());
            input.setUser("UUUUU");
            return input;
        };
    }*/

    /*@Bean
    public Function<KStream<String,PageEvent>,KStream<String,Long>> kStreamFunction(){
        return (input)->{
            return input.filter((k,v)->v.getDuration()>1000)
                    .map((k,v)->new KeyValue<>(v.getName(),0L))
                    .groupBy((k,v)->k,Grouped.with(Serdes.String(),Serdes.Long()))
                    .windowedBy(TimeWindows.of(Duration.ofDays(5000)))
                    .count(Materialized.as("page-count")).toStream()
                    .map((k,v)->new KeyValue<>("=>"+k.window().startTime()+k.window().endTime()+k.key(),v));
        };
    }*/

    //@Bean
    public Consumer<KStream<String,PageEvent>> pageStreamConsumer(){
        return (pageEvent -> pageEvent
                .filter((k,v)->v.getDuration()>100)
                .map((k,v)->new KeyValue<>(v.getName(),v))
                .peek((k,v)-> System.out.println(k+"=>"+v))
                .groupByKey(Grouped.with(Serdes.String(), AppSerdes.PageEventSerdes()))
                .reduce((acc,v)->{
                   v.setDuration(v.getDuration()+acc.getDuration());
                    return v;
                })
                .toStream()
                .peek((k,v)-> System.out.println(k+"=>"+v)));
    }
}
