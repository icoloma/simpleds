package org.simpleds.functions;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Collection;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

public class CompositeFunctionTest {

	private Collection<String> from = ImmutableList.of("1", "2", "3", "4");
	
	@Test
	public void testComposition2() {
		CompositeFunction<String, Integer> func = CompositeFunction.of(
			new StringToBigDecimalFunction(), 
			new BigDecimalToIntegerFunction()
		);
		Collection<Integer> to = Collections2.transform(from, func);
		assertEquals(4, to.size());
		assertEquals(Integer.valueOf(1), to.iterator().next());
	}
	
	@Test
	public void testComposition3() {
		CompositeFunction<String, Boolean> func = CompositeFunction.of(
				new StringToBigDecimalFunction(), 
				new BigDecimalToIntegerFunction(),
				new IntegerToBooleanFunction()
		);
		Collection<Boolean> to = Collections2.transform(from, func);
		assertEquals(4, to.size());
		assertEquals(Boolean.FALSE, to.iterator().next());
	}
	
	private class StringToBigDecimalFunction implements Function<String, BigDecimal> {

		@Override
		public BigDecimal apply(String from) {
			return new BigDecimal(from);
		}

	}
	
	private class BigDecimalToIntegerFunction implements Function<BigDecimal, Integer> {

		@Override
		public Integer apply(BigDecimal from) {
			return from.intValue();
		}
		
	}
	
	private class IntegerToBooleanFunction implements Function<Integer, Boolean> {

		@Override
		public Boolean apply(Integer from) {
			return from % 2 == 0;
		}
	}
	
}
