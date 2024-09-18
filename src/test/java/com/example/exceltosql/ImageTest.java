package com.example.exceltosql;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author :sunjian23
 * @date : 2024/8/26 4:42
 */

public class ImageTest {

    static class Point {
        double lat;
        double lon;

        Point(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }

    public static void main(String[] args) {
        // 示例经纬度数据，使用自定义 Point 类
        ArrayList<Point> coordinates = new ArrayList<>();
        coordinates.add(new Point(37.7749, -122.4194)); // San Francisco
        coordinates.add(new Point(34.0522, -118.2437)); // Los Angeles
        coordinates.add(new Point(40.7128, -74.0060));  // New York

        // 图像尺寸
        int width = 800;
        int height = 600;

        // 创建图像
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2));

        // 确定经纬度范围
        double minLat = coordinates.stream().mapToDouble(c -> c.lat).min().orElse(0);
        double maxLat = coordinates.stream().mapToDouble(c -> c.lat).max().orElse(0);
        double minLon = coordinates.stream().mapToDouble(c -> c.lon).min().orElse(0);
        double maxLon = coordinates.stream().mapToDouble(c -> c.lon).max().orElse(0);

        // 绘制路径
        for (int i = 0; i < coordinates.size() - 1; i++) {
            Point start = coordinates.get(i);
            Point end = coordinates.get(i + 1);

            int x1 = (int) ((start.lon - minLon) / (maxLon - minLon) * width);
            int y1 = (int) ((1 - (start.lat - minLat) / (maxLat - minLat)) * height);
            int x2 = (int) ((end.lon - minLon) / (maxLon - minLon) * width);
            int y2 = (int) ((1 - (end.lat - minLat) / (maxLat - minLat)) * height);

            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.dispose();

        // 保存图像
        try {
            ImageIO.write(image, "png", new File("C:\\Users\\sunjian23\\Desktop\\itrafficflow_20240826024044\\map.png"));
            System.out.println("Image saved as map.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}