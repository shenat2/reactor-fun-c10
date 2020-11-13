package com.sat.test.flux;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

public class FluxBufferTest {
	
	//将多个数据项分成小块传输
	@Test
	public void buffer() {
		Flux<String> fruitFlux = Flux.just("apple", "orange", "banana", "kiwi", "strawberry");
		
		//表示将数据项切分为不超过3个的小块
		Flux<List<String>> bufferedFlux = fruitFlux.buffer(3);
		StepVerifier.create(bufferedFlux)
					.expectNext(Arrays.asList("apple", "orange", "banana"))
					.expectNext(Arrays.asList("kiwi", "strawberry"))
					.verifyComplete();
	}
	
	//将buffer于FlatMap结合使用,异步处理块数据
	@Test
	public void bufferAndFlatMap() {
//		Flux.just("apple", "orange", "banana", "kiwi", "strawberry")
//			.buffer(3)
//			//x表示buffer(3)后形成的List块
//			//flatMap参数function中第一个泛型是一个T,这里就是指list。第二个泛型是一个Publisher，这里就是指Flux对象
//			.flatMap(x -> Flux.fromIterable(x)
//					.map(y -> y.toUpperCase())
//					.subscribeOn(Schedulers.parallel())
//					.log()//可以记录反应式事件
//			).subscribe(t -> System.out.println(t));
		
		//不使用StepVerifier测试的时候，会出现转换没完成就程序执行结束的情况
		Flux<String> bufferedFlux = Flux.just("apple", "orange", "banana", "kiwi", "strawberry")
				.buffer(3)
				//x表示buffer(3)后形成的List块
				//flatMap参数function中第一个泛型是一个T,这里就是指list。第二个泛型是一个Publisher，这里就是指Flux对象
				.flatMap(x -> Flux.fromIterable(x)
						.map(y -> y.toUpperCase())
						.subscribeOn(Schedulers.parallel())
						.log()//可以记录反应式事件
				);
		List<String> list = Arrays.asList("APPLE", "ORANGE", "BANANA", "KIWI", "STRAWBERRY");

	    StepVerifier.create(bufferedFlux)
	        .expectNextMatches(p -> list.contains(p))
	        .expectNextMatches(p -> list.contains(p))
	        .expectNextMatches(p -> list.contains(p))
	        .expectNextMatches(p -> list.contains(p))
	        .expectNextMatches(p -> list.contains(p))
	        .verifyComplete();
		
		
	}
	
	//将所有数据项转为一个集合生成新的Mono
	@Test
	public void collectList() {
		Flux<String> fruitFlux = Flux.just("apple", "orange", "banana", "kiwi", "strawberry");
		
		Mono<List<String>> fruitListMono = fruitFlux.collectList();
		StepVerifier.create(fruitListMono)
					.expectNext(Arrays.asList(
							"apple", "orange", "banana", "kiwi", "strawberry"))
					.verifyComplete();
					
		
	}
	
	//将所有数据转为一个map生成新的Mono,map的key值有数据项的特征决定
	@Test
	public void collectMap() {
		Flux<String> animalFlux = Flux.just("aardvark", "elephant", "koala", "eagle", "kangaroo");
		
		//a -> a.charAt(0)表示map的key时数据项的首字母
		Mono<Map<Character,String>> animalMapMono = animalFlux.collectMap( a -> a.charAt(0));
		
		StepVerifier.create(animalMapMono)
					.expectNextMatches(map -> {
										return map.size() == 3 &&
												map.get('a').equals("aardvark") &&
												map.get('e').equals("eagle") &&
												map.get('k').equals("kangaroo");
												
									})
					.verifyComplete();
					
	}
}
