package com.github.neoflyingsaucer.browser;

import java.awt.Dimension;
import java.awt.EventQueue;
import javax.swing.JFrame;

public class BrowserMain {

	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable() 
		{
			@Override
			public void run() 
			{
				BrowserFrame mainFrame = new BrowserFrame("neoFlyingSaucer Browser");

				mainFrame.createMenuBar();
				mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				mainFrame.setSize(new Dimension(800, 600));
				mainFrame.setVisible(true);
			}
		});
	}
}
