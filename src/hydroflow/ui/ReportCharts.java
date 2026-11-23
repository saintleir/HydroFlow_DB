package hydroflow.ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.Color;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ReportCharts {

    // PIE CHART: Alkaline vs Mineral share of the sales amount
    public static ChartPanel createWaterTypePie(double alkalineSales, double mineralSales, String monthYear) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Alkaline", alkalineSales);
        dataset.setValue("Mineral", mineralSales);

        JFreeChart chart = ChartFactory.createPieChart(
                "Alkaline V.S Mineral Sales - " + monthYear, dataset, true, true, false);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Alkaline", new Color(68, 114, 196));
        plot.setSectionPaint("Mineral", new Color(237, 125, 49));
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: PHP {1} ({2})"));
        plot.setNoDataMessage("No sales for this month");
        return new ChartPanel(chart);
    }

    // BAR CHART: revenue of paid orders for each month of the selected year
    
    public static ChartPanel createMonthlyBar(double[] revenueByMonth, int year) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < revenueByMonth.length; i++) {
            String monthName = Month.of(i + 1).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            dataset.addValue(revenueByMonth[i], "Revenue (PHP)", monthName);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Monthly Revenue - " + year, "Month", "PHP", dataset,
                PlotOrientation.HORIZONTAL, true, true, false);
        return new ChartPanel(chart);
    }

    // BAR CHART: revenue of paid orders for each day of the selected month
    
    public static ChartPanel createDailyBar(double[] revenueByDay, String monthYear) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < revenueByDay.length; i++) {
            dataset.addValue(revenueByDay[i], "Revenue (PHP)", String.valueOf(i + 1));
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Daily Revenue - " + monthYear, "Day", "PHP", dataset,
                PlotOrientation.VERTICAL, true, true, false);
        return new ChartPanel(chart);
    }
}
