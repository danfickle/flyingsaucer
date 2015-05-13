package com.github.neoflyingsaucer.browser;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;

import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;

public class BrowserFrame extends JFrame 
{
	private static final long serialVersionUID = 1L;
	
	private final HtmlPagedPanel _htmlPanel = new HtmlPagedPanel();
	private final HtmlContinuousPanel _continuousPanel = new HtmlContinuousPanel();
	private final UserAgentCallback _demoUserAgent = new DemoUserAgent();

	private JScrollPane _scrollPane;
	private boolean _isContinuous = false;
	private String _currentDemo = "demo:demos/splash/splash.html";
	
	public BrowserFrame(String title)
	{
		super(title);
		
		_htmlPanel.prepare(_demoUserAgent, _currentDemo);
		_scrollPane = new JScrollPane(_htmlPanel);
		add(_scrollPane);
	}
	
	private void run()
	{
		if (_isContinuous)
		{
			_continuousPanel.prepare(_demoUserAgent, _currentDemo, _scrollPane.getWidth() - _scrollPane.getVerticalScrollBar().getWidth());
			_scrollPane.setViewportView(_continuousPanel);
			_scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		else
		{
			_htmlPanel.prepare(_demoUserAgent, _currentDemo);
			_scrollPane.setViewportView(_htmlPanel);
			_scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		}
	}
	
	private void setContinuous(boolean continuous)
	{
		if (continuous == _isContinuous)
		{
			return;
		}
		
		_isContinuous = continuous;
		run();
	}
	
	private static class DemoPair
	{
		private DemoPair(String title, String uri) 
		{
			this.title = title;
			this.uri = uri;
		}
		
		String title;
		String uri;
	}
	
	private List<DemoPair> allDemos = new ArrayList<DemoPair>();
	
    class LoadAction extends AbstractAction
    {
		private static final long serialVersionUID = 1L;
		private final DemoPair demo;
		
        public LoadAction(DemoPair dp) 
        {
        	super(dp.title);
        	this.demo = dp;
        }

        @Override
        public void actionPerformed(final ActionEvent evt) 
        {
        	BrowserFrame.this._currentDemo = this.demo.uri;
        	BrowserFrame.this.setTitle(this.demo.title);
        	BrowserFrame.this.run();
        	
        	_scrollPane.repaint();
        }
    }
    
    class ViewAction extends AbstractAction
    {
		private static final long serialVersionUID = 1L;
		private final boolean continuous;
    	
    	public ViewAction(boolean continuous)
    	{
    		super(continuous ? "Continous" : "Paged");
    		
    		this.continuous = continuous;
    	}

    	@Override
    	public void actionPerformed(ActionEvent e) 
    	{
    		BrowserFrame.this.setContinuous(continuous);
    	}
    }
    
	public void createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		
		JMenu view = new JMenu("View");
		view.setMnemonic(KeyEvent.VK_V);
		menuBar.add(view);
		
		view.add(new ViewAction(true));
		view.add(new ViewAction(false));
		
		JMenu demos = new JMenu("Demos");
		demos.setMnemonic(KeyEvent.VK_D);
		menuBar.add(demos);

		populateDemoList();
		
		for (DemoPair dp : allDemos)
		{
			demos.add(new LoadAction(dp));
		}

		setJMenuBar(menuBar);
	}
	
    private void populateDemoList() 
    {
        List<String> demoList = new ArrayList<String>();
        URL url = BrowserMain.class.getResource("/demos/file-list.txt");
        
        InputStream is = null;
        LineNumberReader lnr = null;

        if (url != null) {
            try {
                is = url.openStream();
                InputStreamReader reader = new InputStreamReader(is, "UTF-8");
                lnr = new LineNumberReader(reader);

                String line;
                while ((line = lnr.readLine()) != null) {
                   demoList.add(line);
                }
            }
            catch (final IOException e)
            {
            	assert(false);
                e.printStackTrace();
            }
            finally 
            {
                if (is != null) 
                {
                    try {
                        is.close();
                    } catch (final IOException e) { }
                }
                
                if (lnr != null)
                {
                	try {
						lnr.close();
					} catch (IOException e) { }
                }
            }

            for (String s : demoList) {
                String s1[] = s.split(",");
                allDemos.add(new DemoPair(s1[0], s1[1]));
            }
        }
    }
}
