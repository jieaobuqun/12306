package com.alibaba.trz;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class View extends JFrame implements ActionListener, MouseListener {
	
	private static final long serialVersionUID = 1L;
	
	private String url;
	
	private BufferedImage image;

	public View(String title, String url) {
		super(title);
		this.url = url;
		this.setSize(305, 270);
		this.setLocationRelativeTo(null);
	}

	public void captcha() {
		// This is an empty content area in the frame
		BufferedImage = ImageIO.read(new File(Constant.resourcePath + "image.jpg"));
		JLabel picLabel = new JLabel(new ImageIcon(myPicture));
		add(picLabel);

		this.getContentPane().add(image, BorderLayout.CENTER);
		
		JButton button1 = new JButton("刷新");
		JButton button2 = new JButton("确定");
		button1.setBounds(0, 200, 150, 20);
		button2.setBounds(0, 200, 150, 20);
		
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
			Action.getImage(url);
			this.getContentPane().remove(image);
			image = new JLabel(new ImageIcon(Constant.resourcePath + "image.jpg"));	
			this.getContentPane().add(image, BorderLayout.CENTER);
			image.addMouseListener(this);
		} else {
			
		}
	}

	public void mouseClicked(MouseEvent e) {
		System.out.print(e.getX() + "," + e.getY() + " ");
		JLabel icon = new JLabel(new ImageIcon(Constant.resourcePath + "icon.png"));
		icon.setBounds(e.getX(), e.getY(), 26, 26);
		this.getContentPane().add(icon, BorderLayout.PAGE_START);
		this.revalidate();
		this.repaint();
	}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}
}
