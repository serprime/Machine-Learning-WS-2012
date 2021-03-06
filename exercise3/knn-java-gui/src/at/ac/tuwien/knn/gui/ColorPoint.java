package at.ac.tuwien.knn.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class ColorPoint extends JPanel {
	
	private Color color;
	private int radius = 5;
	
	public ColorPoint(Color color){
		this.color = color;
		this.setSize(this.radius*2+2, this.radius*2+2);
	}
	
	protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.setColor(this.color);
        g.fillOval(1, 1, this.radius*2, this.radius*2);
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}
	
	
}
