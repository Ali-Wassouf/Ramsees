package com.tyche.ramsees;

import com.tyche.ramsees.strategies.BinanceDataFetcherMock_;
import java.text.SimpleDateFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RefineryUtilities;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;

class RamseesApplicationTests {

   public static void main(String... args){
       var binanceDataFetcher = new BinanceDataFetcherMock_();
       var series = binanceDataFetcher.getSeries();
       ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
       EMAIndicator avg14 = new EMAIndicator(closePrice, 20*24*4);
       StandardDeviationIndicator sd14 = new StandardDeviationIndicator(closePrice, 20*24*4);

       // Bollinger bands
       BollingerBandsMiddleIndicator middleBBand = new BollingerBandsMiddleIndicator(avg14);
       BollingerBandsLowerIndicator lowBBand = new BollingerBandsLowerIndicator(middleBBand, sd14);
       BollingerBandsUpperIndicator upBBand = new BollingerBandsUpperIndicator(middleBBand, sd14);

       /*
        * Building chart dataset
        */
       TimeSeriesCollection dataset = new TimeSeriesCollection();
       dataset.addSeries(buildChartBarSeries(series, closePrice, "ETH"));
       dataset.addSeries(buildChartBarSeries(series, lowBBand, "Low Bollinger Band"));
       dataset.addSeries(buildChartBarSeries(series, middleBBand, "middle Bollinger Band"));
       dataset.addSeries(buildChartBarSeries(series, upBBand, "High Bollinger Band"));

       /*
        * Creating the chart
        */
       JFreeChart chart = ChartFactory.createTimeSeriesChart("ETH Close Prices", // title
           "Date", // x-axis label
           "Price Per Unit", // y-axis label
           dataset, // data
           true, // create legend?
           true, // generate tooltips?
           false // generate URLs?
       );
       XYPlot plot = (XYPlot) chart.getPlot();
       DateAxis axis = (DateAxis) plot.getDomainAxis();
       axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));

       /*
        * Displaying the chart
        */
       displayChart(chart);
   }

    /**
     * Displays a chart in a frame.
     *
     * @param chart the chart to be displayed
     */
    private static void displayChart(JFreeChart chart) {
        // Chart panel
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new java.awt.Dimension(500, 270));
        // Application frame
        ApplicationFrame frame = new ApplicationFrame("Ta4j example - Indicators to chart");
        frame.setContentPane(panel);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }

    private static org.jfree.data.time.TimeSeries buildChartBarSeries(BarSeries barSeries, Indicator<Num> indicator,
        String name) {
        org.jfree.data.time.TimeSeries chartBarSeries = new org.jfree.data.time.TimeSeries(name);
        for (int i = 0; i < barSeries.getBarCount(); i++) {
            Bar bar = barSeries.getBar(i);
            chartBarSeries.add(new Millisecond(bar.getEndTime().getNano()* 1000000,bar.getEndTime().getSecond(), bar.getEndTime().getMinute(),
                    bar.getEndTime().getHour(), bar.getEndTime().getDayOfMonth(), bar.getEndTime().getMonthValue(), bar.getEndTime().getYear()),
                indicator.getValue(i).doubleValue());
        }
        return chartBarSeries;
    }

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

}
