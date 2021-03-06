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

package timeseries;

import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;

public class TimeSeriesSpec {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void whenAggregateWithSmallerPeriodThenIllegalArgument() {
        TimeSeries timeSeries = TestData.ausbeer;
        exception.expect(IllegalArgumentException.class);
        timeSeries.aggregate(TimePeriod.oneMonth());
    }

    @Test
    public void whenBoxCoxBackTransformLambdaTooBigThenIllegalArgument() {
        TimeSeries series = TestData.ausbeer;
        exception.expect(IllegalArgumentException.class);
        series.backTransform(2.5);
    }

    @Test
    public void whenBoxCoxTransformationLambdaTooBigThenIllegalArgument() {
        TimeSeries series = TestData.ausbeer;
        exception.expect(IllegalArgumentException.class);
        series.transform(2.5);
    }

    @Test
    public void whenBoxCoxBackTransformLambdaTooSmallThenIllegalArgument() {
        TimeSeries series = TestData.ausbeer;
        exception.expect(IllegalArgumentException.class);
        series.backTransform(-1.5);
    }

    @Test
    public void whenBoxCoxTransformationLambdaTooSmallThenIllegalArgument() {
        TimeSeries series = TestData.ausbeer;
        exception.expect(IllegalArgumentException.class);
        series.transform(-1.5);
    }

    @Test
    public void whenBoxCoxTransformLogThenDataTransformedCorrectly() {
        double[] data = new double[]{3.0, 7.0, Math.E};
        double[] expected = new double[]{Math.log(3.0), Math.log(7.0), 1.0};
        TimeSeries timeSeries = new TimeSeries(data);
        assertArrayEquals(expected, timeSeries.transform(0).asArray(), 1E-4);
    }

    @Test
    public void whenBoxCoxInvTransformLogThenDataTransformedCorrectly() {
        double[] data = new double[]{Math.log(3.0), Math.log(7.0), 1.0};
        double[] expected = new double[]{3.0, 7.0, Math.E};
        TimeSeries timeSeries = new TimeSeries(data);
        assertArrayEquals(expected, timeSeries.backTransform(0).asArray(), 1E-4);
    }

    @Test
    public void whenDifferencedNoArgThenDifferencedOnce() {
        TimeSeries timeSeries = TestData.ausbeer;
        assertThat(timeSeries.difference(), is(timeSeries.difference(1)));
    }

    @Test
    public void whenStartTimeThenFirstObservationTime() {
        TimeSeries timeSeries = Ts.newQuarterlySeries(1956, 1, TestData.ausbeerArray);
        OffsetDateTime expected = OffsetDateTime.parse("1956-01-01T00:00:00Z");
        assertThat(timeSeries.startTime(), is(expected));
    }

    @Test
    public void whenDifferencedMoreThanOnceThenCorrectData() {
        TimeSeries timeSeries = TestData.ausbeer;
        TimeSeries diffedTwice = timeSeries.difference().difference();
        assertThat(timeSeries.difference(1, 2), is(diffedTwice));
    }

    @Test
    public void whenDataPointAccessedThenExpectedValueReturned() {
        TimeSeries timeSeries = TestData.debitcards;
        OffsetDateTime period = OffsetDateTime.parse("2000-01-01T00:00:00Z");
        assertThat(timeSeries.at(period), is(timeSeries.at(0)));
    }

    @Test
    public void whenTimeSeriesMeanTakenThenResultCorrect() {
        double[] data = new double[]{3.0, 7.0, 5.0};
        TimeSeries series = new TimeSeries(OffsetDateTime.now(), data);
        assertThat(series.mean(), is(equalTo(5.0)));
    }

    @Test
    public void whenAutoCovarianceNegativeLagThenIllegalArgument() {
        TimeSeries series = TestData.ausbeer;
        exception.expect(IllegalArgumentException.class);
        series.autoCovarianceAtLag(-1);
    }

    @Test
    public void whenAutoCovarianceComputedTheResultIsCorrect() {
        TimeSeries series = new TimeSeries(10.0, 5.0, 4.5, 7.7, 3.4, 6.9);
        double[] acvf = new double[]{4.889, -1.837, -0.407, 1.310, -1.917, 0.406};
        for (int i = 0; i < acvf.length; i++) {
            assertThat(series.autoCovarianceAtLag(i), is(closeTo(acvf[i], 1E-2)));
        }
    }

    @Test
    public void whenAutoCorrelationComputedTheResultIsCorrect() {
        TimeSeries series = new TimeSeries(10.0, 5.0, 4.5, 7.7, 3.4, 6.9);
        double[] acf = new double[]{1.000, -0.376, -0.083, 0.268, -0.392, 0.083};
        for (int i = 0; i < acf.length; i++) {
            assertThat(series.autoCorrelationAtLag(i), is(closeTo(acf[i], 1E-2)));
        }
    }

    @Test
    public void whenAutoCovarianceComputedUpToLagKThenResultingArrayCorrect() {
        TimeSeries series = new TimeSeries(10.0, 5.0, 4.5, 7.7, 3.4, 6.9);
        double[] expected = new double[]{4.889, -1.837, -0.407, 1.310, -1.917, 0.406};
        double[] result = series.autoCovarianceUpToLag(9);
        assertArrayEquals(expected, result, 1E-2);
    }

    @Test
    public void whenAutoCorrelationComputedUpToLagKThenResultingArrayCorrect() {
        TimeSeries series = new TimeSeries(10.0, 5.0, 4.5, 7.7, 3.4, 6.9);
        double[] expected = new double[]{1.000, -0.376, -0.083, 0.268, -0.392, 0.083};
        double[] result = series.autoCorrelationUpToLag(5);
        assertArrayEquals(expected, result, 1E-2);
    }

    @Test
    public void whenFivePeriodMovingAverageComputedResultCorrect() {
        TimeSeries series = TestData.elecSales;
        double[] expected = new double[]{2381.53, 2424.556, 2463.758, 2552.598, 2627.7, 2750.622, 2858.348, 3014.704,
                3077.3, 3144.52, 3188.7, 3202.32, 3216.94, 3307.296, 3398.754, 3485.434};
        double[] result = series.movingAverage(5).asArray();
        assertArrayEquals(expected, result, 1E-2);
    }

    @Test
    public void whenFourPeriodMovingAverageComputedResultCorrect() {
        TimeSeries series = TestData.elecSales;
        double[] expected = new double[]{2380.39, 2388.3275, 2435.7675, 2500.0675, 2573.5, 2688.1025, 2795.91,
                2929.005, 3077.7, 3135.5, 3180.475, 3208.85, 3163.525, 3252.25, 3338.97, 3443.0425, 3562.7425};
        double[] result = series.movingAverage(4).asArray();
        assertArrayEquals(expected, result, 1E-2);
    }

    @Test
    public void whenFourPeriodCenteredMovingAverageComputedResultCorrect() {
        TimeSeries series = TestData.elecSales;
        double[] expected = new double[]{2384.35875, 2412.0475, 2467.9175, 2536.78375, 2630.80125, 2742.00625,
                2862.4575, 3003.3525, 3106.6, 3157.9875, 3194.6625, 3186.1875, 3207.8875, 3295.61, 3391.00625,
                3502.8925};
        double[] result = series.centeredMovingAverage(4).asArray();
        assertArrayEquals(expected, result, 1E-2);
    }

    @Test
    public void whenFivePeriodCenteredMovingAverageComputedResultCorrect() {
        TimeSeries series = TestData.elecSales;
        double[] expected = new double[]{2381.53, 2424.556, 2463.758, 2552.598, 2627.7, 2750.622, 2858.348, 3014.704,
                3077.3, 3144.52, 3188.7, 3202.32, 3216.94, 3307.296, 3398.754, 3485.434};
        double[] result = series.centeredMovingAverage(5).asArray();
        assertArrayEquals(expected, result, 1E-2);
    }

    @Test
    public void whenTimeSeriesAggregatedDatesCorrect() {
        TimeSeries series = TestData.ausbeer;
        TimeSeries aggregated = series.aggregate(TimeUnit.DECADE);
        OffsetDateTime expectedStart = OffsetDateTime.of(LocalDateTime.of(1956, 1, 1, 0, 0), ZoneOffset.ofHours(0));
        OffsetDateTime expectedEnd = OffsetDateTime.of(LocalDateTime.of(1996, 1, 1, 0, 0), ZoneOffset.ofHours(0));
        assertThat(aggregated.observationTimes().get(0), is(equalTo(expectedStart)));
        assertThat(aggregated.observationTimes().get(aggregated.size() - 1), is(equalTo(expectedEnd)));
    }

    @Test
    public void whenWeeklySeriesCreatedResultCorrect() {
        TimeSeries series = TestData.sydneyAir;
        TimeSeries seriesOne = series.aggregateToYears();
        TimeSeries seriesTwo = series.aggregate(TimeUnit.YEAR);
        MatcherAssert.assertThat(seriesOne, is(equalTo(seriesTwo)));
    }

    @Test
    public void whenFromThenCorrectSliceOfDataReturned() {
        double[] ts = TestData.ausbeerArray;
        TimeSeries series = new TimeSeries(TimePeriod.oneQuarter(), "1956-01-01T00:00:00", ts);
        OffsetDateTime startSlice = OffsetDateTime.parse("1956-04-01T00:00:00Z");
        OffsetDateTime endSlice = OffsetDateTime.parse("1957-01-01T00:00:00Z");
        TimeSeries expected = series.timeSlice(2, 5);
        assertThat(series.from(startSlice, endSlice), is(expected));
        assertThat(series.from(1, 4), is(expected));
    }

    @Test
    public void testHashCodeAndEquals() {
        double[] ts = TestData.ausbeerArray;
        TimeSeries series1 = Ts.newQuarterlySeries(1956, 1, ts);
        TimeSeries series2 = Ts.newQuarterlySeries(1957, 2, ts);
        TimeSeries series3 = TestData.elecSales;
        TimeSeries series4 = Ts.newQuarterlySeries(1956, 1, ts);
        TimeSeries nullSeries = null;
        String aNonTimeSeries = "";
        assertThat(series1, is(series1));
        assertThat(series1, is(series4));
        assertThat(series1.hashCode(), is(series4.hashCode()));
        MatcherAssert.assertThat(series1, is(not(nullSeries)));
        MatcherAssert.assertThat(series1, is(not(aNonTimeSeries)));
        MatcherAssert.assertThat(series1, is(not(series2)));
        MatcherAssert.assertThat(series2, is(not(series3)));
    }
}
