/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.repository.util;

import static org.springframework.data.repository.util.ReactiveWrapperConverters.RegistryHolder.*;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import lombok.experimental.UtilityClass;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rx.Completable;
import rx.Observable;
import rx.Single;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.repository.util.ReactiveWrappers.ReactiveLibrary;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Conversion support for reactive wrapper types. This class is a reactive extension to
 * {@link QueryExecutionConverters}.
 * <p>
 * This class discovers reactive wrapper availability and their conversion support based on the class path. Reactive
 * wrapper types might be supported/on the class path but conversion may require additional dependencies.
 * 
 * @author Mark Paluch
 * @since 2.0
 * @see ReactiveWrappers
 * @see ReactiveAdapterRegistry
 */
@UtilityClass
public class ReactiveWrapperConverters {

	private static final List<ReactiveTypeWrapper<?>> REACTIVE_WRAPPERS = new ArrayList<>();
	private static final GenericConversionService GENERIC_CONVERSION_SERVICE = new GenericConversionService();

	static {

		if (ReactiveWrappers.isAvailable(ReactiveLibrary.RXJAVA1)) {

			REACTIVE_WRAPPERS.add(RxJava1SingleWrapper.INSTANCE);
			REACTIVE_WRAPPERS.add(RxJava1ObservableWrapper.INSTANCE);
		}

		if (ReactiveWrappers.isAvailable(ReactiveLibrary.RXJAVA2)) {

			REACTIVE_WRAPPERS.add(RxJava2SingleWrapper.INSTANCE);
			REACTIVE_WRAPPERS.add(RxJava2MaybeWrapper.INSTANCE);
			REACTIVE_WRAPPERS.add(RxJava2ObservableWrapper.INSTANCE);
			REACTIVE_WRAPPERS.add(RxJava2FlowableWrapper.INSTANCE);
		}

		if (ReactiveWrappers.isAvailable(ReactiveLibrary.PROJECT_REACTOR)) {

			REACTIVE_WRAPPERS.add(FluxWrapper.INSTANCE);
			REACTIVE_WRAPPERS.add(MonoWrapper.INSTANCE);
			REACTIVE_WRAPPERS.add(PublisherWrapper.INSTANCE);
		}

		registerConvertersIn(GENERIC_CONVERSION_SERVICE);
	}

	/**
	 * Registers converters for wrapper types found on the classpath.
	 *
	 * @param conversionService must not be {@literal null}.
	 */
	public static ConversionService registerConvertersIn(ConfigurableConversionService conversionService) {

		Assert.notNull(conversionService, "ConversionService must not be null!");

		if (ReactiveWrappers.isAvailable(ReactiveLibrary.PROJECT_REACTOR)) {

			if (ReactiveWrappers.isAvailable(ReactiveLibrary.RXJAVA1)) {

				conversionService.addConverter(PublisherToRxJava1CompletableConverter.INSTANCE);
				conversionService.addConverter(RxJava1CompletableToPublisherConverter.INSTANCE);
				conversionService.addConverter(RxJava1CompletableToMonoConverter.INSTANCE);

				conversionService.addConverter(PublisherToRxJava1SingleConverter.INSTANCE);
				conversionService.addConverter(RxJava1SingleToPublisherConverter.INSTANCE);
				conversionService.addConverter(RxJava1SingleToMonoConverter.INSTANCE);
				conversionService.addConverter(RxJava1SingleToFluxConverter.INSTANCE);

				conversionService.addConverter(PublisherToRxJava1ObservableConverter.INSTANCE);
				conversionService.addConverter(RxJava1ObservableToPublisherConverter.INSTANCE);
				conversionService.addConverter(RxJava1ObservableToMonoConverter.INSTANCE);
				conversionService.addConverter(RxJava1ObservableToFluxConverter.INSTANCE);
			}

			if (ReactiveWrappers.isAvailable(ReactiveLibrary.RXJAVA2)) {

				conversionService.addConverter(PublisherToRxJava2CompletableConverter.INSTANCE);
				conversionService.addConverter(RxJava2CompletableToPublisherConverter.INSTANCE);
				conversionService.addConverter(RxJava2CompletableToMonoConverter.INSTANCE);

				conversionService.addConverter(PublisherToRxJava2SingleConverter.INSTANCE);
				conversionService.addConverter(RxJava2SingleToPublisherConverter.INSTANCE);
				conversionService.addConverter(RxJava2SingleToMonoConverter.INSTANCE);
				conversionService.addConverter(RxJava2SingleToFluxConverter.INSTANCE);

				conversionService.addConverter(PublisherToRxJava2ObservableConverter.INSTANCE);
				conversionService.addConverter(RxJava2ObservableToPublisherConverter.INSTANCE);
				conversionService.addConverter(RxJava2ObservableToMonoConverter.INSTANCE);
				conversionService.addConverter(RxJava2ObservableToFluxConverter.INSTANCE);

				conversionService.addConverter(PublisherToRxJava2FlowableConverter.INSTANCE);
				conversionService.addConverter(RxJava2FlowableToPublisherConverter.INSTANCE);

				conversionService.addConverter(PublisherToRxJava2MaybeConverter.INSTANCE);
				conversionService.addConverter(RxJava2MaybeToPublisherConverter.INSTANCE);
				conversionService.addConverter(RxJava2MaybeToMonoConverter.INSTANCE);
				conversionService.addConverter(RxJava2MaybeToFluxConverter.INSTANCE);
			}

			conversionService.addConverter(PublisherToMonoConverter.INSTANCE);
			conversionService.addConverter(PublisherToFluxConverter.INSTANCE);

			if (ReactiveWrappers.isAvailable(ReactiveLibrary.RXJAVA1)) {
				conversionService.addConverter(RxJava1SingleToObservableConverter.INSTANCE);
				conversionService.addConverter(RxJava1ObservableToSingleConverter.INSTANCE);
			}

			if (ReactiveWrappers.isAvailable(ReactiveLibrary.RXJAVA2)) {
				conversionService.addConverter(RxJava2SingleToObservableConverter.INSTANCE);
				conversionService.addConverter(RxJava2ObservableToSingleConverter.INSTANCE);
				conversionService.addConverter(RxJava2ObservableToMaybeConverter.INSTANCE);
			}
		}

		return conversionService;
	}

	/**
	 * Returns whether the given type is supported for wrapper type conversion.
	 * <p>
	 * NOTE: A reactive wrapper type might be supported in general by {@link ReactiveWrappers#supports(Class)} but not
	 * necessarily for conversion using this method.
	 * </p>
	 * 
	 * @param type must not be {@literal null}.
	 * @return {@literal true} if the {@code type} is a supported reactive wrapper type.
	 */
	public static boolean supports(Class<?> type) {
		return RegistryHolder.REACTIVE_ADAPTER_REGISTRY.getAdapterFrom(type) != null;
	}

	/**
	 * Casts or converts the given wrapper type into a different wrapper type.
	 * 
	 * @param stream the stream, must not be {@literal null}.
	 * @param expectedWrapperType must not be {@literal null}.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T toWrapper(Object stream, Class<? extends T> expectedWrapperType) {

		Assert.notNull(stream, "Stream must not be null!");
		Assert.notNull(expectedWrapperType, "Converter must not be null!");

		if (expectedWrapperType.isAssignableFrom(stream.getClass())) {
			return (T) stream;
		}

		return GENERIC_CONVERSION_SERVICE.convert(stream, expectedWrapperType);
	}

	/**
	 * Maps elements of a reactive element stream to other elements.
	 * 
	 * @param reactiveObject must not be {@literal null}.
	 * @param converter must not be {@literal null}.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T map(Object reactiveObject, Function<Object, Object> converter) {

		Assert.notNull(reactiveObject, "Reactive source object must not be null!");
		Assert.notNull(converter, "Converter must not be null!");

		return REACTIVE_WRAPPERS.stream()//
				.filter(it -> ClassUtils.isAssignable(it.getWrapperClass(), reactiveObject.getClass()))//
				.findFirst()//
				.map(it -> (T) it.map(reactiveObject, converter))//
				.orElseThrow(() -> new IllegalStateException(String.format("Cannot apply converter to %s", reactiveObject)));
	}

	// -------------------------------------------------------------------------
	// Wrapper descriptors
	// -------------------------------------------------------------------------

	/**
	 * Wrapper descriptor that can apply a {@link Function} to map items inside its stream.
	 *
	 * @author Mark Paluch
	 * @author Christoph Strobl
	 */
	private interface ReactiveTypeWrapper<T> {

		/**
		 * @return the wrapper class.
		 */
		Class<? super T> getWrapperClass();

		/**
		 * Apply a {@link Function} to a reactive type.
		 *
		 * @param wrapper the reactive type, must not be {@literal null}.
		 * @param function the converter, must not be {@literal null}.
		 * @return the reactive type applying conversion.
		 */
		Object map(Object wrapper, Function<Object, Object> function);
	}

	/**
	 * Wrapper for Project Reactor's {@link Mono}.
	 */
	private enum MonoWrapper implements ReactiveTypeWrapper<Mono<?>> {

		INSTANCE;

		@Override
		public Class<? super Mono<?>> getWrapperClass() {
			return Mono.class;
		}

		@Override
		public Mono<?> map(Object wrapper, Function<Object, Object> function) {
			return ((Mono<?>) wrapper).map(function::apply);
		}
	}

	/**
	 * Wrapper for Project Reactor's {@link Flux}.
	 */
	private enum FluxWrapper implements ReactiveTypeWrapper<Flux<?>> {

		INSTANCE;

		@Override
		public Class<? super Flux<?>> getWrapperClass() {
			return Flux.class;
		}

		public Flux<?> map(Object wrapper, Function<Object, Object> function) {
			return ((Flux<?>) wrapper).map(function::apply);
		}
	}

	/**
	 * Wrapper for Reactive Stream's {@link Publisher}.
	 */
	private enum PublisherWrapper implements ReactiveTypeWrapper<Publisher<?>> {

		INSTANCE;

		@Override
		public Class<? super Publisher<?>> getWrapperClass() {
			return Publisher.class;
		}

		@Override
		public Publisher<?> map(Object wrapper, Function<Object, Object> function) {

			if (wrapper instanceof Mono) {
				return MonoWrapper.INSTANCE.map(wrapper, function);
			}

			if (wrapper instanceof Flux) {
				return FluxWrapper.INSTANCE.map(wrapper, function);
			}

			return FluxWrapper.INSTANCE.map(Flux.from((Publisher<?>) wrapper), function);
		}
	}

	/**
	 * Wrapper for RxJava 1's {@link Single}.
	 */
	private enum RxJava1SingleWrapper implements ReactiveTypeWrapper<Single<?>> {

		INSTANCE;

		@Override
		public Class<? super Single<?>> getWrapperClass() {
			return Single.class;
		}

		@Override
		public Single<?> map(Object wrapper, Function<Object, Object> function) {
			return ((Single<?>) wrapper).map(function::apply);
		}
	}

	/**
	 * Wrapper for RxJava 1's {@link Observable}.
	 */
	private enum RxJava1ObservableWrapper implements ReactiveTypeWrapper<Observable<?>> {

		INSTANCE;

		@Override
		public Class<? super Observable<?>> getWrapperClass() {
			return Observable.class;
		}

		@Override
		public Observable<?> map(Object wrapper, Function<Object, Object> function) {
			return ((Observable<?>) wrapper).map(function::apply);
		}
	}

	/**
	 * Wrapper for RxJava 2's {@link io.reactivex.Single}.
	 */
	private enum RxJava2SingleWrapper implements ReactiveTypeWrapper<io.reactivex.Single<?>> {

		INSTANCE;

		@Override
		public Class<? super io.reactivex.Single<?>> getWrapperClass() {
			return io.reactivex.Single.class;
		}

		@Override
		public io.reactivex.Single<?> map(Object wrapper, Function<Object, Object> function) {
			return ((io.reactivex.Single<?>) wrapper).map(function::apply);
		}
	}

	/**
	 * Wrapper for RxJava 2's {@link io.reactivex.Maybe}.
	 */
	private enum RxJava2MaybeWrapper implements ReactiveTypeWrapper<Maybe<?>> {

		INSTANCE;

		@Override
		public Class<? super io.reactivex.Maybe<?>> getWrapperClass() {
			return io.reactivex.Maybe.class;
		}

		@Override
		public io.reactivex.Maybe<?> map(Object wrapper, Function<Object, Object> function) {
			return ((io.reactivex.Maybe<?>) wrapper).map(function::apply);
		}
	}

	/**
	 * Wrapper for RxJava 2's {@link io.reactivex.Observable}.
	 */
	private enum RxJava2ObservableWrapper implements ReactiveTypeWrapper<io.reactivex.Observable<?>> {

		INSTANCE;

		@Override
		public Class<? super io.reactivex.Observable<?>> getWrapperClass() {
			return io.reactivex.Observable.class;
		}

		@Override
		public io.reactivex.Observable<?> map(Object wrapper, Function<Object, Object> function) {
			return ((io.reactivex.Observable<?>) wrapper).map(function::apply);
		}
	}

	/**
	 * Wrapper for RxJava 2's {@link io.reactivex.Flowable}.
	 */
	private enum RxJava2FlowableWrapper implements ReactiveTypeWrapper<Flowable<?>> {

		INSTANCE;

		@Override
		public Class<? super Flowable<?>> getWrapperClass() {
			return io.reactivex.Flowable.class;
		}

		@Override
		public io.reactivex.Flowable<?> map(Object wrapper, Function<Object, Object> function) {
			return ((io.reactivex.Flowable<?>) wrapper).map(function::apply);
		}
	}

	// -------------------------------------------------------------------------
	// ReactiveStreams converters
	// -------------------------------------------------------------------------

	/**
	 * A {@link Converter} to convert a {@link Publisher} to {@link Flux}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum PublisherToFluxConverter implements Converter<Publisher<?>, Flux<?>> {

		INSTANCE;

		@Override
		public Flux<?> convert(Publisher<?> source) {
			return Flux.from(source);
		}
	}

	/**
	 * A {@link Converter} to convert a {@link Publisher} to {@link Mono}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum PublisherToMonoConverter implements Converter<Publisher<?>, Mono<?>> {

		INSTANCE;

		@Override
		public Mono<?> convert(Publisher<?> source) {
			return Mono.from(source);
		}
	}

	// -------------------------------------------------------------------------
	// RxJava 1 converters
	// -------------------------------------------------------------------------

	/**
	 * A {@link Converter} to convert a {@link Publisher} to {@link Single}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum PublisherToRxJava1SingleConverter implements Converter<Publisher<?>, Single<?>> {

		INSTANCE;

		@Override
		public Single<?> convert(Publisher<?> source) {
			return (Single<?>) REACTIVE_ADAPTER_REGISTRY.getAdapterTo(Single.class).fromPublisher(Mono.from(source));
		}
	}

	/**
	 * A {@link Converter} to convert a {@link Publisher} to {@link Completable}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum PublisherToRxJava1CompletableConverter implements Converter<Publisher<?>, Completable> {

		INSTANCE;

		@Override
		public Completable convert(Publisher<?> source) {
			return (Completable) REACTIVE_ADAPTER_REGISTRY.getAdapterTo(Completable.class).fromPublisher(source);
		}
	}

	/**
	 * A {@link Converter} to convert a {@link Publisher} to {@link Observable}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum PublisherToRxJava1ObservableConverter implements Converter<Publisher<?>, Observable<?>> {

		INSTANCE;

		@Override
		public Observable<?> convert(Publisher<?> source) {
			return (Observable<?>) REACTIVE_ADAPTER_REGISTRY.getAdapterTo(Observable.class).fromPublisher(Flux.from(source));
		}
	}

	/**
	 * A {@link Converter} to convert a {@link Single} to {@link Publisher}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava1SingleToPublisherConverter implements Converter<Single<?>, Publisher<?>> {

		INSTANCE;

		@Override
		public Publisher<?> convert(Single<?> source) {
			return Flux.defer(() -> REACTIVE_ADAPTER_REGISTRY.getAdapterFrom(Single.class).toPublisher(source));
		}
	}

	/**
	 * A {@link Converter} to convert a {@link Single} to {@link Mono}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava1SingleToMonoConverter implements Converter<Single<?>, Mono<?>> {

		INSTANCE;

		@Override
		public Mono<?> convert(Single<?> source) {
			return Mono.defer(() -> REACTIVE_ADAPTER_REGISTRY.getAdapterFrom(Single.class).toMono(source));
		}
	}

	/**
	 * A {@link Converter} to convert a {@link Single} to {@link Publisher}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava1SingleToFluxConverter implements Converter<Single<?>, Flux<?>> {

		INSTANCE;

		@Override
		public Flux<?> convert(Single<?> source) {
			return Flux.defer(() -> REACTIVE_ADAPTER_REGISTRY.getAdapterFrom(Single.class).toFlux(source));
		}
	}

	/**
	 * A {@link Converter} to convert a {@link Completable} to {@link Publisher}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava1CompletableToPublisherConverter implements Converter<Completable, Publisher<?>> {

		INSTANCE;

		@Override
		public Publisher<?> convert(Completable source) {
			return Flux.defer(() -> REACTIVE_ADAPTER_REGISTRY.getAdapterFrom(Completable.class).toFlux(source));
		}
	}

	/**
	 * A {@link Converter} to convert a {@link Completable} to {@link Mono}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava1CompletableToMonoConverter implements Converter<Completable, Mono<?>> {

		INSTANCE;

		@Override
		public Mono<?> convert(Completable source) {
			return Mono.from(RxJava1CompletableToPublisherConverter.INSTANCE.convert(source));
		}
	}

	/**
	 * A {@link Converter} to convert an {@link Observable} to {@link Publisher}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava1ObservableToPublisherConverter implements Converter<Observable<?>, Publisher<?>> {

		INSTANCE;

		@Override
		public Publisher<?> convert(Observable<?> source) {
			return Flux.defer(() -> REACTIVE_ADAPTER_REGISTRY.getAdapterFrom(Observable.class).toFlux(source));
		}
	}

	/**
	 * A {@link Converter} to convert a {@link Observable} to {@link Mono}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava1ObservableToMonoConverter implements Converter<Observable<?>, Mono<?>> {

		INSTANCE;

		@Override
		public Mono<?> convert(Observable<?> source) {
			return Mono.defer(() -> REACTIVE_ADAPTER_REGISTRY.getAdapterFrom(Observable.class).toMono(source));
		}
	}

	/**
	 * A {@link Converter} to convert a {@link Observable} to {@link Flux}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava1ObservableToFluxConverter implements Converter<Observable<?>, Flux<?>> {

		INSTANCE;

		@Override
		public Flux<?> convert(Observable<?> source) {
			return Flux.defer(() -> REACTIVE_ADAPTER_REGISTRY.getAdapterFrom(Observable.class).toFlux(source));
		}
	}

	/**
	 * A {@link Converter} to convert a {@link Observable} to {@link Single}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava1ObservableToSingleConverter implements Converter<Observable<?>, Single<?>> {

		INSTANCE;

		@Override
		public Single<?> convert(Observable<?> source) {
			return source.toSingle();
		}
	}

	/**
	 * A {@link Converter} to convert a {@link Single} to {@link Single}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava1SingleToObservableConverter implements Converter<Single<?>, Observable<?>> {

		INSTANCE;

		@Override
		public Observable<?> convert(Single<?> source) {
			return source.toObservable();
		}
	}

	// -------------------------------------------------------------------------
	// RxJava 2 converters
	// -------------------------------------------------------------------------

	/**
	 * A {@link Converter} to convert a {@link Publisher} to {@link io.reactivex.Single}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum PublisherToRxJava2SingleConverter implements Converter<Publisher<?>, io.reactivex.Single<?>> {

		INSTANCE;

		@Override
		public io.reactivex.Single<?> convert(Publisher<?> source) {
			return (io.reactivex.Single<?>) REACTIVE_ADAPTER_REGISTRY.getAdapterTo(io.reactivex.Single.class)
					.fromPublisher(source);
		}
	}

	/**
	 * A {@link Converter} to convert a {@link Publisher} to {@link io.reactivex.Completable}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum PublisherToRxJava2CompletableConverter implements Converter<Publisher<?>, io.reactivex.Completable> {

		INSTANCE;

		@Override
		public io.reactivex.Completable convert(Publisher<?> source) {
			return (io.reactivex.Completable) REACTIVE_ADAPTER_REGISTRY.getAdapterTo(io.reactivex.Completable.class)
					.fromPublisher(source);
		}
	}

	/**
	 * A {@link Converter} to convert a {@link Publisher} to {@link io.reactivex.Observable}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum PublisherToRxJava2ObservableConverter implements Converter<Publisher<?>, io.reactivex.Observable<?>> {

		INSTANCE;

		@Override
		public io.reactivex.Observable<?> convert(Publisher<?> source) {
			return (io.reactivex.Observable<?>) REACTIVE_ADAPTER_REGISTRY.getAdapterTo(io.reactivex.Single.class)
					.fromPublisher(source);
		}
	}

	/**
	 * A {@link Converter} to convert a {@link io.reactivex.Single} to {@link Publisher}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava2SingleToPublisherConverter implements Converter<io.reactivex.Single<?>, Publisher<?>> {

		INSTANCE;

		@Override
		public Publisher<?> convert(io.reactivex.Single<?> source) {
			return REACTIVE_ADAPTER_REGISTRY.getAdapterFrom(io.reactivex.Single.class).toMono(source);
		}
	}

	/**
	 * A {@link Converter} to convert a {@link io.reactivex.Single} to {@link Mono}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava2SingleToMonoConverter implements Converter<io.reactivex.Single<?>, Mono<?>> {

		INSTANCE;

		@Override
		public Mono<?> convert(io.reactivex.Single<?> source) {
			return REACTIVE_ADAPTER_REGISTRY.getAdapterFrom(io.reactivex.Single.class).toMono(source);
		}
	}

	/**
	 * A {@link Converter} to convert a {@link io.reactivex.Single} to {@link Publisher}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava2SingleToFluxConverter implements Converter<io.reactivex.Single<?>, Flux<?>> {

		INSTANCE;

		@Override
		public Flux<?> convert(io.reactivex.Single<?> source) {
			return REACTIVE_ADAPTER_REGISTRY.getAdapterFrom(io.reactivex.Single.class).toFlux(source);
		}
	}

	/**
	 * A {@link Converter} to convert a {@link io.reactivex.Completable} to {@link Publisher}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava2CompletableToPublisherConverter implements Converter<io.reactivex.Completable, Publisher<?>> {

		INSTANCE;

		@Override
		public Publisher<?> convert(io.reactivex.Completable source) {
			return REACTIVE_ADAPTER_REGISTRY.getAdapterFrom(io.reactivex.Completable.class).toFlux(source);
		}
	}

	/**
	 * A {@link Converter} to convert a {@link io.reactivex.Completable} to {@link Mono}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava2CompletableToMonoConverter implements Converter<io.reactivex.Completable, Mono<?>> {

		INSTANCE;

		@Override
		public Mono<?> convert(io.reactivex.Completable source) {
			return Mono.from(RxJava2CompletableToPublisherConverter.INSTANCE.convert(source));
		}
	}

	/**
	 * A {@link Converter} to convert an {@link io.reactivex.Observable} to {@link Publisher}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava2ObservableToPublisherConverter implements Converter<io.reactivex.Observable<?>, Publisher<?>> {

		INSTANCE;

		@Override
		public Publisher<?> convert(io.reactivex.Observable<?> source) {
			return source.toFlowable(BackpressureStrategy.BUFFER);
		}
	}

	/**
	 * A {@link Converter} to convert a {@link io.reactivex.Observable} to {@link Mono}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava2ObservableToMonoConverter implements Converter<io.reactivex.Observable<?>, Mono<?>> {

		INSTANCE;

		@Override
		public Mono<?> convert(io.reactivex.Observable<?> source) {
			return Mono.from(source.toFlowable(BackpressureStrategy.BUFFER));
		}
	}

	/**
	 * A {@link Converter} to convert a {@link io.reactivex.Observable} to {@link Flux}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava2ObservableToFluxConverter implements Converter<io.reactivex.Observable<?>, Flux<?>> {

		INSTANCE;

		@Override
		public Flux<?> convert(io.reactivex.Observable<?> source) {
			return Flux.from(source.toFlowable(BackpressureStrategy.BUFFER));
		}
	}

	/**
	 * A {@link Converter} to convert a {@link Publisher} to {@link io.reactivex.Flowable}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum PublisherToRxJava2FlowableConverter implements Converter<Publisher<?>, io.reactivex.Flowable<?>> {

		INSTANCE;

		@Override
		public io.reactivex.Flowable<?> convert(Publisher<?> source) {
			return Flowable.fromPublisher(source);
		}
	}

	/**
	 * A {@link Converter} to convert a {@link io.reactivex.Flowable} to {@link Publisher}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava2FlowableToPublisherConverter implements Converter<io.reactivex.Flowable<?>, Publisher<?>> {

		INSTANCE;

		@Override
		public Publisher<?> convert(io.reactivex.Flowable<?> source) {
			return source;
		}
	}

	/**
	 * A {@link Converter} to convert a {@link Publisher} to {@link io.reactivex.Flowable}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum PublisherToRxJava2MaybeConverter implements Converter<Publisher<?>, io.reactivex.Maybe<?>> {

		INSTANCE;

		@Override
		public io.reactivex.Maybe<?> convert(Publisher<?> source) {
			return (io.reactivex.Maybe<?>) REACTIVE_ADAPTER_REGISTRY.getAdapterTo(Maybe.class).fromPublisher(source);
		}
	}

	/**
	 * A {@link Converter} to convert a {@link io.reactivex.Maybe} to {@link Publisher}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava2MaybeToPublisherConverter implements Converter<io.reactivex.Maybe<?>, Publisher<?>> {

		INSTANCE;

		@Override
		public Publisher<?> convert(io.reactivex.Maybe<?> source) {
			return source.toFlowable();
		}
	}

	/**
	 * A {@link Converter} to convert a {@link io.reactivex.Maybe} to {@link Mono}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava2MaybeToMonoConverter implements Converter<io.reactivex.Maybe<?>, Mono<?>> {

		INSTANCE;

		@Override
		public Mono<?> convert(io.reactivex.Maybe<?> source) {
			return Mono.from(source.toFlowable());
		}
	}

	/**
	 * A {@link Converter} to convert a {@link io.reactivex.Maybe} to {@link Flux}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava2MaybeToFluxConverter implements Converter<io.reactivex.Maybe<?>, Flux<?>> {

		INSTANCE;

		@Override
		public Flux<?> convert(io.reactivex.Maybe<?> source) {
			return Flux.from(source.toFlowable());
		}
	}

	/**
	 * A {@link Converter} to convert a {@link Observable} to {@link Single}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava2ObservableToSingleConverter
			implements Converter<io.reactivex.Observable<?>, io.reactivex.Single<?>> {

		INSTANCE;

		@Override
		public io.reactivex.Single<?> convert(io.reactivex.Observable<?> source) {
			return source.singleOrError();
		}
	}

	/**
	 * A {@link Converter} to convert a {@link Observable} to {@link Maybe}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava2ObservableToMaybeConverter
			implements Converter<io.reactivex.Observable<?>, io.reactivex.Maybe<?>> {

		INSTANCE;

		@Override
		public io.reactivex.Maybe<?> convert(io.reactivex.Observable<?> source) {
			return source.singleElement();
		}
	}

	/**
	 * A {@link Converter} to convert a {@link Single} to {@link Single}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	public enum RxJava2SingleToObservableConverter
			implements Converter<io.reactivex.Single<?>, io.reactivex.Observable<?>> {

		INSTANCE;

		@Override
		public io.reactivex.Observable<?> convert(io.reactivex.Single<?> source) {
			return source.toObservable();
		}
	}

	/**
	 * Holder for delayed initialization of {@link ReactiveAdapterRegistry}.
	 *
	 * @author Mark Paluch
	 * @author 2.0
	 */
	static class RegistryHolder {
		static final ReactiveAdapterRegistry REACTIVE_ADAPTER_REGISTRY = new ReactiveAdapterRegistry();
	}
}