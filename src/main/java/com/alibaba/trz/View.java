package com.alibaba.trz;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class View extends JFrame implements ActionListener, MouseListener {
	
	private static final long serialVersionUID = 1L;
	
	private String url;
	
	private ImagePanel image;

	public View(String title, String url) {
		super(title);
		this.url = url;
		this.setSize(305, 270);
		this.setLocationRelativeTo(null);
	}

	public void captcha() {
		// This is an empty content area in the frame
		image = new ImagePanel(Action.getImage(url));
		this.add(image);

		this.getContentPane().add(image, BorderLayout.CENTER);
		
		JButton button1 = new JButton("刷新");
		JButton button2 = new JButton("确定");
		
		JPanel panel = new JPanel();
		panel.add(button1);
		panel.add(button2);
		
		this.getContentPane().add(panel, BorderLayout.PAGE_END);

		image.addMouseListener(this);
		button1.addActionListener(this);
		button2.addActionListener(this);
		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if ( e.getActionCommand().equals("刷新") ) {
			this.getContentPane().remove(image);
			image = new ImagePanel(Action.getImage(url));
			
			this.getContentPane().add(image, BorderLayout.CENTER);
			this.validate();
			image.addMouseListener(this);
		} else {
			
		}
	}

	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		
		Graphics graph = image.getGraphics();
		BufferedImage icon = Action.getImage();
		graph.drawImage(icon, x-13, y-13, null);
	}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}
}

class ImagePanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private BufferedImage image;
	
    public ImagePanel(BufferedImage image) {
        this.image = image;
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this);
    }
    
    @Override
    public Dimension getPreferredSize() {
      int width = image.getWidth();
      int height = image.getHeight();
      return new Dimension(width , height );
    }
}
