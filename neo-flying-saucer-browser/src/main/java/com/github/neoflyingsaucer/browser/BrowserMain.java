package com.github.neoflyingsaucer.browser;

import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class BrowserMain {

	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable() 
		{
			@Override
			public void run() 
			{
				JFrame mainFrame = new JFrame("neoFlyingSaucer Browser");
				HtmlPagedPanel htmlPanel = new HtmlPagedPanel();
				JScrollPane scrollPane = new JScrollPane(htmlPanel);

				htmlPanel.prepare(new DemoUserAgent(), "demo:demos/splash/splash.html");
				
				mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				mainFrame.setSize(new Dimension(800, 600));
				mainFrame.add(scrollPane);
				mainFrame.setVisible(true);
			}
		});
	}

}
