package siat.ncu.press.util;

import org.achartengine.renderer.XYMultipleSeriesRenderer;

public class XYRenderer extends XYMultipleSeriesRenderer {
    
    public static final int TEXT_SIZE = 20;
    //图表样式构造器
    public XYRenderer() {

    }

    public XYRenderer(String title, String xTitle, String yTitle, double xMin, double xMax, double yMin, double yMax,
            int axesColor, int labelsColor, int XTitleTextSize, int ChartTitleTextSize, int LabelsTextSize,
            int LegendTextSize, int Xshow, int Yshow, boolean ShowGrid) {
        this.setXTitle(xTitle);// X轴标题
        this.setYTitle(yTitle);// Y轴标题
        this.setXAxisMin(xMin);// X最小值
        this.setXAxisMax(xMax);// X最大值
        this.setYAxisMin(yMin);// Y最小值
        this.setYAxisMax(yMax);// Y最小值
        this.setAxesColor(axesColor);// X轴颜色
        this.setLabelsColor(labelsColor);// Y轴颜色
        this.setAxisTitleTextSize(XTitleTextSize); // 坐标轴标题字体大小：20
        this.setChartTitleTextSize(ChartTitleTextSize); // 图表标题字体大小：20
        this.setLabelsTextSize(LabelsTextSize); // 轴标签字体大小：20
        this.setLegendTextSize(LegendTextSize); // 图例字体大小：20
        this.setXLabels(Xshow);//设置X轴显示的刻度标签的个数
        this.setYLabels(Yshow);//设置Y轴显示的刻度标签的个数
        this.setShowGrid(ShowGrid); // 设置网格显示
    }

}
