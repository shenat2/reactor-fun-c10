package com.sat.test.flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

public class FluxCreationTest {
	
	//根据对象创建Flux
	@Test
	public void createFluxByObject() {
		Flux<String> fruitFlux = Flux.just("Apple","Orange","Grape","Banana","Strawberry");
		
		StepVerifier.create(fruitFlux).expectNext("Apple")
									  .expectNext("Orange")
									  .expectNext("Grape")
									  .expectNext("Banana")
									  .expectNext("Strawberry")
									  .verifyComplete();
	}
	
	//根据数组创建Flux
	@Test
	public void createFluxByArray(){
		String[] fruits = new String[] {"Apple","Orange","Grape","Banana","Strawberry"};
		Flux<String> fruitFlux = Flux.fromArray(fruits);
		
		StepVerifier.create(fruitFlux).expectNext("Apple")
									  .expectNext("Orange")
									  .expectNext("Grape")
									  .expectNext("Banana")
									  .expectNext("Strawberry")
									  .verifyComplete();
	}
	
	//根据集合创建
	@Test
	public void createFluxByIterable() {
		List<String> fruitList = new ArrayList<String>();
		fruitList.add("Apple");
		fruitList.add("Orange");
		fruitList.add("Grape");
		fruitList.add("Banana");
		fruitList.add("Strawberry");
		
		Flux<String> fruitFlux = Flux.fromIterable(fruitList);
		
		StepVerifier.create(fruitFlux).expectNext("Apple")
		  .expectNext("Orange")
		  .expectNext("Grape")
		  .expectNext("Banana")
		  .expectNext("Strawberry")
		  .verifyComplete();
		
	}
	
	//根据stream创建
	@Test
	public void createFluxByStream() {
		Stream<String> fruitStream = Stream.of("Apple","Orange","Grape","Banana","Strawberry");
		Flux<String> fruitFlux = Flux.fromStream(fruitStream);
		
		//加了这个后Flux就关闭了，下面StepCerifier会报错
//		fruitFlux.subscribe(f -> System.out.println(f));
		
		StepVerifier.create(fruitFlux).expectNext("Apple")
		  .expectNext("Orange")
		  .expectNext("Grape")
		  .expectNext("Banana")
		  .expectNext("Strawberry")
		  .verifyComplete();
	
	}
	
	//创建范围Flux
	@Test
	public void createFlux_range() {
		Flux<Integer> intervalFlux = Flux.range(1, 5);
		
		//这变加这个下面的StepVerifier也不报错，与上面貌似不一样
		intervalFlux.subscribe(f -> System.out.println(f));
		
		StepVerifier.create(intervalFlux).expectNext(1)
		  .expectNext(2)
		  .expectNext(3)
		  .expectNext(4)
		  .expectNext(5)
		  .verifyComplete();
	
	}
	
	//创建按时间周期发送（每隔多少事件发送一次）
	//测试时通过打印方式测不出来，但是通过StepVerifier能测出来
	@Test
	public void createFlux_interval() {
		//take方法表示一共发送N次，如果参数为0表示发送一次后即终止
		Flux<Long> intervalFlux = Flux.interval(Duration.ofSeconds(1)).take(5);
//		intervalFlux.subscribe(f -> System.out.println(f));
		
		StepVerifier.create(intervalFlux).expectNext(0L)
		  .expectNext(1L)
		  .expectNext(2L)
		  .expectNext(3L)
		  .expectNext(4L)
		  .verifyComplete();
	}
	
	//merge两个Flux
	@Test
	public void mergeFluxes() {
		Flux<String> characterFlux = Flux.just("Garfield", "Kojak", "Barbossa");
//				.delayElements(Duration.ofMillis(500));
		//每500MS发布下一个条目
		characterFlux = characterFlux.delayElements(Duration.ofMillis(500));
		
		Flux<String> foodFlux = Flux.just("Lasagna", "Lollipops", "Apples");
//				.delaySubscription(Duration.ofMillis(250))
//				.delayElements(Duration.ofMillis(500));
		//订阅后250ms开始发送条目
		foodFlux = foodFlux.delaySubscription(Duration.ofMillis(250));
		//每500ms发布一次
		foodFlux = foodFlux.delayElements(Duration.ofMillis(500));
		
		
		//merge两个Flux
		Flux<String> mergedFlux = characterFlux.mergeWith(foodFlux);
		
		StepVerifier.create(mergedFlux).expectNext("Garfield")
							           .expectNext("Lasagna")
							           .expectNext("Kojak")
							           .expectNext("Lollipops")
							           .expectNext("Barbossa")
							           .expectNext("Apples")
									   .verifyComplete();
		
	}
	
	//zip两个Flux
	@Test
	public void zipFluxes() {
		Flux<String> characterFlux = Flux.just("Garfield", "Kojak", "Barbossa");
		Flux<String> foodFlux = Flux.just("Lasagna", "Lollipops", "Apples");
		
		//zip两个publisher就用Tuple2,3个就用Tuple3
		Flux<Tuple2<String,String>> zippedFlux = Flux.zip(characterFlux,foodFlux);
		StepVerifier.create(zippedFlux)
			.expectNextMatches(p -> p.getT1().equals("Garfield") &&
									p.getT2().equals("Lasagna"))
			.expectNextMatches(p -> p.getT1().equals("Kojak") &&
									p.getT2().equals("Lollipops"))
			.expectNextMatches(p -> p.getT1().equals("Barbossa") &&
									p.getT2().equals("Apples"))
			.verifyComplete();
		
	}
	
	//zip的时候提供合并函数来生成新对象
	@Test
	public void zipFluxesToObject() {
		Flux<String> characterFlux = Flux.just("Garfield", "Kojak", "Barbossa");
		Flux<String> foodFlux = Flux.just("Lasagna", "Lollipops", "Apples");
		
		Flux<String> zippedFlux = Flux.zip(characterFlux,foodFlux, (c,f) -> c + " eats " + f);
		
		StepVerifier.create(zippedFlux)
					.expectNext("Garfield eats Lasagna")
			        .expectNext("Kojak eats Lollipops")
			        .expectNext("Barbossa eats Apples")
			        .verifyComplete();
	}
	
	@Test
	public void firstFlux() {
//		Flux<String> slowFlux = Flux.just("tortoise", "snail", "sloth").delaySubscription(Duration.ofMillis(300));
//		Flux<String> slowFlux = Flux.just("tortoise", "snail", "sloth");
		Flux<String> fastFlux = Flux.just("hare", "cheetah", "squirrel");
		Flux<String> slowFlux = Flux.just("tortoise", "snail", "sloth");
		
		//经测试加了延迟的肯定不会被打印出来，如果不加延迟，同时订阅，貌似第一个参数的subscriber处理比较快，总是被打印出来
		//总的来说还是哪个流优先被处理完成，然后会一值固定从这个流中取
//		Flux<String> firstFlux = Flux.first(slowFlux,fastFlux);
		Flux<String> firstFlux = Flux.first(fastFlux,slowFlux);
		firstFlux.subscribe( t -> System.out.println(t));
		
		
		
		
	}
	
	
	
	
	
}	
