package com.trz.railway;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class View extends JFrame implements ActionListener, MouseListener {
	
	private static final long serialVersionUID = 1L;

	private String randCode;

	private ImagePanel image;

	public View(String title) {
		super(title);
		this.setSize(305, 270);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(View.DISPOSE_ON_CLOSE);
	}

    /**
     * 初始化验证码
     */
	public void captcha() {
		image = new ImagePanel(Command.getImage(Constant.CAPTCHA_URL));

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

    /**
     * 刷新
     */
	public void refresh() {
        this.getContentPane().remove(image);
        image = new ImagePanel(Command.getImage(Constant.CAPTCHA_URL));

        this.getContentPane().add(image, BorderLayout.CENTER);
        this.validate();
        image.addMouseListener(this);
	}

    @Override
	public void actionPerformed(ActionEvent e) {
		if ( e.getActionCommand().equals("刷新") ) {
            refresh();
		} else {
			synchronized (Command.class) {
                Command.class.notify();
			}
			this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
	}

    @Override
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();

		if (randCode == null)
			randCode = x + "," + y;
		else
			randCode += "," + x + "," + y;

		Graphics graph = image.getGraphics();
		BufferedImage icon = Command.getImage();
		graph.drawImage(icon, x-13, y-13, null);
	}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public String getRandCode() {
        return randCode;
	}
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
