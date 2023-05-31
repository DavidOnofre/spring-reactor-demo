package com.kodigo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@SpringBootApplication
public class SpringReactorDemoApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SpringReactorDemoApplication.class);
    private static List<String> dishes = new ArrayList<>();

    public void createMono() {
        //suscribirse con una variable para seguir operando.
        //Mono<String> m1 = Mono.just("Hello kodigo");
        //m1.subscribe(x -> log.info(x));

        //suscribirse sobre el flujo en ese momento
        Mono.just(5).subscribe(x -> log.info("data:" + x));
    }

    public void createFlux() {
        Flux<String> fx1 = Flux.fromIterable(dishes);
        fx1.subscribe(x -> log.info("Dish: " + x)); //elemento por elemento

        fx1.collectList().subscribe(list -> log.info("List:" + list));
    }

    public void m1doOnNext() {
        Flux<String> fx1 = Flux.fromIterable(dishes);

        fx1.doOnNext(log::info) //  e->log.info(e)   es lo mismo    log::info
                .subscribe();
    }

    public void m2map() {
        //caso 1
        //Flux<String> fx1 = Flux.fromIterable(dishes);
        //fx1.map(String::toUpperCase)
        //        .subscribe(e->log.info("Dishes: " + e));

        //caso 2
        Flux<String> fx1 = Flux.fromIterable(dishes);
        fx1.map(String::toUpperCase); //tal como está, no es tomada en cuenta para imprimir.
        fx1.subscribe(e -> log.info("Dishes: " + e));


        //como resultado del caso 2 es la lista original es decir la línea
        //fx1.map(String::toUpperCase); no la toma en cuenta
        // para que sea tomada en cuenta hay que almacenar ese resultado en una variable y suscribirse ej:
        // Flux<String> fx2 = fx1.map(String::toUpperCase);
        // fx2.subscribe(e->log.info("Dishes: " + e));

    }

    public void m3flatMap() {
        Mono.just("David Onofre").map(x -> "kodigo").subscribe(e -> log.info("King of Software: " + e));

        //No muestra el valor xq kodigo está dentro de un subflujo
        Mono.just("David Onofre").map(x -> Mono.just("kodigo")).subscribe(e -> log.info("King of Software: " + e));

        //para poder mostrar el valor del subflujo se utiliza la clase flatMap
        Mono.just("David Onofre").flatMap(x -> Mono.just("kodigo")).subscribe(e -> log.info("King of Software: " + e));

        //como la subscripción es del último valor se para cada caso se perdería el valor original "David Onofre"
        //el subscribe trabaja sobre el último valor.
    }

    public void m4range() {
        Flux<Integer> fx1 = Flux.range(0, 10);
        fx1.map(x -> x + 1).subscribe(x -> log.info(x.toString()));
    }

    public void m5delayElements() throws InterruptedException {
        Flux.range(0, 10)
                .delayElements(Duration.ofSeconds(2))
                .doOnNext(x -> log.info("Datos del arreglo:" + x))
                .subscribe();

        Thread.sleep(22000);

        //delayElements obliga que se ejecute en otro hilo, hasta el momento
        //los anteriores ejemplos se ejecutaban en el hilo principal "main".
    }

    public void m6zipWith() {
        List<String> clients = new ArrayList<>();
        clients.add("Cliente 1");
        clients.add("Cliente 2");

        Flux<String> fx1 = Flux.fromIterable(dishes);
        Flux<String> fx2 = Flux.fromIterable(clients);

        fx1.zipWith(fx2, (d, c) -> d + "-" + c)
                .subscribe(log::info);

    }

    public void m7merge() {
        List<String> clients = new ArrayList<>();
        clients.add("Cliente 1");
        clients.add("Cliente 2");
        clients.add("Cliente 3");

        Flux<String> fx1 = Flux.fromIterable(dishes);
        Flux<String> fx2 = Flux.fromIterable(clients);

        Flux.merge(fx1, fx2).subscribe(log::info);
    }

    public void m8filter() {

        //Flux<String> fx1 = Flux.fromIterable(dishes);

        //fx1.filter(d->d.startsWith("Arr"))
        //        .subscribe(log::info);

        //mismo resultado usando predicados
        Flux<String> fx1 = Flux.fromIterable(dishes);
        Predicate<String> predicate = d -> d.startsWith("Arr");

        fx1.filter(predicate)
                .subscribe(log::info);
    }

    public void m9takeLast() {
        Flux<String> fx1 = Flux.fromIterable(dishes);
        //toma el último elemento de la lista
        fx1.takeLast(1)
                .subscribe(log::info);

        //toma el los 2 elementos ultimos de la lista
        fx1.takeLast(2)
                .subscribe(log::info);
    }

    public void m10take() {
        Flux<String> fx1 = Flux.fromIterable(dishes);
        //toma el primer elemento de la lista
        fx1.take(1)
                .subscribe(log::info);

        //toma el los 2 primeros elementos de la lista
        fx1.take(2)
                .subscribe(log::info);
    }

    public void m11defaultIfEmpty() {
        dishes = new ArrayList<>();
        Flux<String> fx1 = Flux.fromIterable(dishes);
        fx1.map(e -> "P: " + e)
                .defaultIfEmpty("EMPTY FLUX")
                .subscribe(log::info);
    }

    public void m12Error() {

        Flux<String> fx1 = Flux.fromIterable(dishes);

        fx1.doOnNext(d -> {
                    throw new ArithmeticException("Bad number");
                })
                //.onErrorReturn("error please reboot your system")
                .onErrorMap(ex -> new Exception(ex.getMessage()))
                .subscribe(x -> log.info("Data " + x)); //.subscribe(log::info);

    }

    public void m13Threads() {
        final Mono<String> mono = Mono.just("hello ");

        // Esto genera un hilo Thread 1
        Thread t = new Thread(() -> mono
                .map(msg -> msg + "thread ")
                .subscribe(v -> System.out.println(v + Thread.currentThread().getName()))
        );

        // todo a partir de aquí se usara el hilo main
        System.out.println(Thread.currentThread().getName());
        t.start();
    }

    public void m14PublishOn() {
        Flux.range(1, 2)
                .publishOn(Schedulers.boundedElastic())
                .map(x -> {
                    log.info("Valor " + x + " | Thread: " + Thread.currentThread().getName());
                    return x;
                })
                //.publishOn(Schedulers.single()) // todo afecta que el proceso que este debajo de él, se ejecute en otro hilo, mejor tiempo de respuesta ya que los 2 procesos se ejecutan al mismo tiempo
                .map(x -> {
                    log.info("Valor " + x + " | Thread: " + Thread.currentThread().getName());
                    return x;
                })
                .subscribe();
    }

    // subscribeOn afecta a los procesos que estan antes y después de el
    // como salida tendremos 4 hilos boundedElastic
    // en una cadena reactiva solo deberia tener un subscribeOn ya que si
    // hubiera más estas no las tomaria en cuenta
    public void m15SubscribeOn(){
        Flux.range(1, 2)
                .map(x -> {
                    log.info("Valor " + x + " | Thread: " + Thread.currentThread().getName());
                    return x;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(x -> {
                    log.info("Valor " + x + " | Thread: " + Thread.currentThread().getName());
                    return x;
                })
                .subscribe();
    }

    public void m17runOn(){
        Flux.range(1, 8)
                .parallel(8)
                .runOn(Schedulers.parallel())
                .map(x -> {
                    log.info("Valor " + x + " | Thread: " + Thread.currentThread().getName());
                    return x;
                })
                .subscribe();
    }

    public static void main(String[] args) {
        dishes.add("Arroz con menestra");
        dishes.add("Pollo con papas");

        SpringApplication.run(SpringReactorDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        m17runOn();
    }


}
