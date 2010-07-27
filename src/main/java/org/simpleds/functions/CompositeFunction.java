package org.simpleds.functions;

import com.google.common.base.Function;

/**
 * Executes a series of functions
 * @author icoloma
 *
 */
public class CompositeFunction<F, G> implements Function<F, G> {

	private Function<?, ?>[] functions;

	@Override
	@SuppressWarnings("unchecked")
	public G apply(F from) {
		Object o = from;
		for (Function func : functions) {
			o = func.apply(o);
		}
		return (G) o;
	}
	
	private CompositeFunction(Function<?, ?>... functions) {
		this.functions = functions;
	}

	@SuppressWarnings("unchecked")
	public static <F, T, G> CompositeFunction<F, G> of(Function<F, T> f1, Function<T, G> f2) {
		return new CompositeFunction(f1, f2);
	}
	
	@SuppressWarnings("unchecked")
	public static <F, T1, T2, G> CompositeFunction<F, G> of(Function<F, T1> f1, Function<T1, T2> f2, Function<T2, G> f3) {
		return new CompositeFunction(f1, f2, f3);
	}
	
	@SuppressWarnings("unchecked")
	public static <F, T1, T2, T3, G> CompositeFunction<F, G> of(Function<F, T1> f1, Function<T1, T2> f2, Function<T2, T3> f3, Function<T3, G> f4) {
		return new CompositeFunction(f1, f2, f3, f4);
	}
	
}
