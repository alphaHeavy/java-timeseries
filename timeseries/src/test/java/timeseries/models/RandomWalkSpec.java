/*
 * Copyright (c) 2017 Jacob Rachiele
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to
 * do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *
 * Jacob Rachiele
 */

package timeseries.models;

import org.hamcrest.MatcherAssert;
import timeseries.TestData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import timeseries.TimeSeries;

import static org.junit.Assert.assertArrayEquals;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class RandomWalkSpec {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void whenInstantiatedNullSeriesThenNPE() {
        exception.expect(NullPointerException.class);
        new RandomWalk(null);
    }

    @Test
    public void whenSimWithNullDistributionThenNPE() {
        exception.expect(NullPointerException.class);
        RandomWalk.simulate(null, 100);
    }

    @Test
    public void whenEmptyTimeSeriesThenIllegalArgumentException() {
        exception.expect(IllegalArgumentException.class);
        new RandomWalk(new TimeSeries());
    }

    @Test
    public void whenSimulatedThenSeriesHasGivenLength() {
        TimeSeries simulated = RandomWalk.simulate(0, 2, 10);
        assertThat(simulated.size(), is(10));
    }

    @Test
    public void whenForecastThenNotNullReturned() {
        TimeSeries series = TestData.sydneyAir;
        RandomWalk model = new RandomWalk(series);
        assertThat(model.forecast(1), is(not(nullValue())));
    }

    @Test
    public void whenPointForecastThenEqualToLastObserved() {
        TimeSeries series = TestData.sydneyAir;
        RandomWalk model = new RandomWalk(series);
        assertThat(model.pointForecast(1).asArray()[0], is(series.at(series.size() - 1)));
    }

    @Test
    public void whenForecastThenPredictionIntervalCorrect() {
        TimeSeries series = TestData.sydneyAir;
        RandomWalkForecast forecast = new RandomWalkForecast(series, 7, 0.05);
        double[] lowerValues = {0.955726, 0.735251, 0.566075, 0.423453, 0.2978, 0.184201, 0.079736};
        double[] upperValues = {2.020274, 2.240749, 2.409925, 2.552547, 2.6782, 2.791799, 2.896264};
        assertArrayEquals(lowerValues, forecast.lowerPredictionValues().asArray(), 1E-4);
        assertArrayEquals(upperValues, forecast.upperPredictionValues().asArray(), 1E-4);
        assertThat(forecast.lowerPredictionValues(), is(forecast.computeLowerPredictionBounds(7, 0.05)));
        assertThat(forecast.upperPredictionValues(), is(forecast.computeUpperPredictionBounds(7, 0.05)));
    }

    @Test
    public void testEqualsAndHashCode() {
        RandomWalk rw1 = new RandomWalk(TestData.ausbeer);
        RandomWalk rw2 = new RandomWalk(TestData.debitcards);
        RandomWalk rw3 = new RandomWalk(TestData.ausbeer);
        RandomWalk nullModel = null;
        String aNonModel = "";
        assertThat(rw1, is(rw1));
        assertThat(rw1, is(rw3));
        assertThat(rw1.hashCode(), is(rw3.hashCode()));
        MatcherAssert.assertThat(rw1, is(not(rw2)));
        MatcherAssert.assertThat(rw1, is(not(nullModel)));
        MatcherAssert.assertThat(rw1, is(not(aNonModel)));
    }
}
