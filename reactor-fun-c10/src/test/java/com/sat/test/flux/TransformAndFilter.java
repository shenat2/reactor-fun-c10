package com.sat.test.flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.sat.bean.Player;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

public class TransformAndFilter {
	
	
	//忽略前几个数据项
	@Test
	public void skipAFew() {
		Flux<String> countFlux = Flux.just(
		        "one", "two", "skip a few", "ninety nine", "one hundred")
				.skip(3);//忽略前3个。也可以根据时间忽略或者最后几个
		StepVerifier.create(countFlux)
					.expectNext("ninety nine","one hundred")
					.verifyComplete();
	}
	
	//忽略多上时间之前的数据项
	@Test
	public void skipAFewSeconds() {
		Flux<String> countFlux = Flux.just(
		        "one", "two", "skip a few", "ninety nine", "one hundred")
				.delayElements(Duration.ofSeconds(1))//每个数据项发送之间有1秒延迟
				.skip(Duration.ofSeconds(4));//忽略4秒前的数据项
		
		StepVerifier.create(countFlux)
		.expectNext("ninety nine","one hundred")
		.verifyComplete();
		
	}
	
	//只取前几个数据项
	@Test
	public void take() {
		Flux<String> countFlux = Flux.just(
				"Yellowstone", "Yosemite", "Grand Canyon", "Zion", "Acadia")
				.take(3);//忽略前3个。也可以根据时间忽略或者最后几个
		StepVerifier.create(countFlux)
					.expectNext("Yellowstone","Yosemite","Grand Canyon")
					.verifyComplete();
	}
	
	//只取前一段时间的数据项
	@Test
	public void take_time() {
		Flux<String> countFlux = Flux.just(
				"Yellowstone", "Yosemite", "Grand Canyon", "Zion", "Acadia")
				.delayElements(Duration.ofSeconds(1))
				.take(Duration.ofMillis(3500));//只取3.5秒之前的数据项
		StepVerifier.create(countFlux)
					.expectNext("Yellowstone","Yosemite","Grand Canyon")
					.verifyComplete();
	}
	
	//通用删选测试（take和skip其实也属于filter）
	@Test
	public void filter() {
		Flux<String> countFlux = Flux.just(
				"Yellowstone", "Yosemite", "Grand Canyon", "Zion", "Acadia")
				.filter(np -> !np.contains(" "));//筛选掉包含空格的
		StepVerifier.create(countFlux)
			.expectNext("Yellowstone","Yosemite","Zion","Acadia")
			.verifyComplete();
				
	}
	
	//测试distinct唯一筛选
	@Test
	public void distinct() {
		Flux<String> animalFlux = Flux.just("dog", "cat", "bird", "dog", "bird", "anteater")
									.distinct();
		StepVerifier.create(animalFlux)
		.expectNext("dog", "cat", "bird", "anteater")
		.verifyComplete();
	
	}
	
	//map转换数据项类型后到新Flux发送
	@Test
	public void map() {
		Flux<Player> playerFlux = Flux.just("Michael Jordan", "Scottie Pippen", "Steve Kerr")
									.map(n -> {String[] split = n.split("\\s");
											   return new Player(split[0],split[1]);});
		
		StepVerifier.create(playerFlux)
					.expectNext(new Player("Michael", "Jordan"))
					.expectNext(new Player("Scottie", "Pippen"))
					.expectNext(new Player("Steve", "Kerr"))
					.verifyComplete();
	}
	
	//使用flatmap异步执行数据项转换，需要与subscribeOn结合使用，新生成的Flux中的数据项顺序无法保证
	@Test
	public void flatMap() {
		Flux<Player> playerFlux = Flux.just("Michael Jordan", "Scottie Pippen", "Steve Kerr")
									  .flatMap( n -> Mono.just(n)
											  		.map(p -> {
											  			String[] split = p.split("\\s");
											  			return new Player(split[0],split[1]);
											  		})
											  		//subscriberOn时在一个Scheduler上执行订阅
											  		//Schedulers.parallel是一个具有固定数量的ExecutorService线程池，用于执行同步任务
											  		.subscribeOn(Schedulers.parallel())
											  	);
		List<Player> playerList = Arrays.asList(
		        new Player("Michael", "Jordan"), 
		        new Player("Scottie", "Pippen"), 
		        new Player("Steve", "Kerr"));

	    StepVerifier.create(playerFlux)
	        .expectNextMatches(p -> playerList.contains(p))
	        .expectNextMatches(p -> playerList.contains(p))
	        .expectNextMatches(p -> playerList.contains(p))
	        .verifyComplete();
	}
	
	//检查所有数据项是否都符合条件
	@Test
	public void all() {
		Flux<String> animalFlux = Flux.just("aardvark", "elephant", "koala", "eagle", "kangaroo");
		
		Mono<Boolean> hasAMono = animalFlux.all(a -> a.contains("a"));
		StepVerifier.create(hasAMono)
					.expectNext(true)
					.verifyComplete();
		
		Mono<Boolean> hasKMono = animalFlux.all(a -> a.contains("k"));
		StepVerifier.create(hasKMono)
					.expectNext(false)
					.verifyComplete();
	}
	//检查任一数据项是否都符合条件
	@Test
	public void any() {
		Flux<String> animalFlux = Flux.just("aardvark", "elephant", "koala", "eagle", "kangaroo");
		
		Mono<Boolean> hasTMono = animalFlux.any(a -> a.contains("t"));
		StepVerifier.create(hasTMono)
		.expectNext(true)
		.verifyComplete();
		
		Mono<Boolean> hasZMono = animalFlux.all(a -> a.contains("z"));
		StepVerifier.create(hasZMono)
		.expectNext(false)
		.verifyComplete();
	}
	
	
	
	
	
	
	
}
