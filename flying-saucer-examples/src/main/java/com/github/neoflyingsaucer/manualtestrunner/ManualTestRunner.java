/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.github.neoflyingsaucer.manualtestrunner;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.demo.browser.FSScrollPane;

import com.github.danfickle.flyingsaucer.swing.XHTMLPanel;


/**
 * Eeze is a mini-application to test the Flying Saucer renderer across a set of
 * XML/CSS files.
 *
 * @author Who?
 */
public class ManualTestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManualTestRunner.class);
    /**
     * Description of the Field
     */
    List<String> testFiles;
    /**
     * Description of the Field
     */
    JFrame eezeFrame;

    /**
     * Description of the Field
     */
    String currentDisplayed;
    /**
     * Description of the Field
     */
    Action growAction;
    /**
     * Description of the Field
     */
    Action shrinkAction;
    /**
     * Description of the Field
     */
    Action nextDemoAction;

    Action chooseDemoAction;

    /**
     * Description of the Field
     */
    Action increase_font, reset_font, decrease_font, showHelp, showGrid, saveAsImg, overlayImage;

    /**
     * Description of the Field
     */
    private XHTMLPanel html;

    private FSScrollPane scroll;

    /**
     * Description of the Field
     */
    private File directory;

    private ReloadPageAction reloadPageAction;
    private ReloadFileListAction reloadFileList;

    /**
     * Constructor for the Eeze object
     */
    private ManualTestRunner() {
    }

    /**
     * Main processing method for the Eeze object
     *
     * @param args PARAM
     * @throws IOException 
     */
    private void run() throws IOException {
        buildFrame();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    final File fontFile = new File(directory + "/support/AHEM____.TTF");
                    if (fontFile.exists()) {
                        html.getSharedContext().setFontMapping("Ahem",
                                Font.createFont(Font.TRUETYPE_FONT, fontFile.toURL().openStream()));
                    }
                } catch (final FontFormatException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (final IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
        testFiles = buildFileList();
        try {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    showHelpPage();
                }
            });
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Description of the Method
     *
     * @return Returns
     * @throws IOException 
     */
    private List<String> buildFileList() throws IOException 
    {
    	String dir = "manual_tests/tests/";
    	ClassLoader loader = getClass().getClassLoader();
    	BufferedReader index = new BufferedReader(
    	    new InputStreamReader(
    	        loader.getResourceAsStream(dir + "test-index.txt")
    	    )
    	);

    	List<String> list = new ArrayList<String>();
    	String fileName;
    	while ((fileName = index.readLine()) != null) {
    		String res = dir + fileName;
    		list.add(res);
    	}

    	return list;
    }

    /**
     * Description of the Method
     */
    private void buildFrame() {
        try {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    eezeFrame = new JFrame("FS Eeze");
                    final JFrame frame = eezeFrame;
                    frame.setExtendedState(JFrame.NORMAL);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                	
                	html = new XHTMLPanel();
                    scroll = new FSScrollPane(html);
                    frame.getContentPane().add(scroll);
                    frame.pack();
                    frame.setSize(1024, 768);
                    frame.setVisible(true);

                    frame.addComponentListener(new ComponentAdapter() {
                        public void componentResized(final ComponentEvent e) {
                            html.relayout();
                        }
                    });

                    nextDemoAction = new NextDemoAction();
                    reloadPageAction = new ReloadPageAction();
                    chooseDemoAction = new ChooseDemoAction();
                    growAction = new GrowAction();
                    shrinkAction = new ShrinkAction();

                    increase_font = new FontSizeAction(FontSizeAction.INCREMENT, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
                    reset_font = new FontSizeAction(FontSizeAction.RESET, KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_MASK));
                    decrease_font = new FontSizeAction(FontSizeAction.DECREMENT, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));

                    reloadFileList = new ReloadFileListAction();
                    showGrid = new ShowGridAction();
                    showHelp = new ShowHelpAction();

                    frame.setJMenuBar(new JMenuBar());
                    final JMenu doMenu = new JMenu("Do");
                    doMenu.add(reloadPageAction);
                    doMenu.add(nextDemoAction);
                    doMenu.add(chooseDemoAction);
                    doMenu.add(growAction);
                    doMenu.add(shrinkAction);
                    doMenu.add(increase_font);
                    doMenu.add(reset_font);
                    doMenu.add(decrease_font);
                    doMenu.add(showGrid);
                    doMenu.add(reloadFileList);
                    doMenu.add(showHelp);
                    doMenu.setVisible(true);
                    frame.getJMenuBar().add(doMenu);
                }
            });

        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Description of the Method
     *
     * @param file PARAM
     */
    private void switchPage(final String file, final boolean reload) {
        eezeFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            if (reload) {
                LOGGER.info("Reloading " + currentDisplayed);
                html.reloadDocument(getClass().getClassLoader().getResource(file).toExternalForm());
            } else {
                LOGGER.info("Loading " + currentDisplayed);
                html.setDocument(getClass().getClassLoader().getResource(file).toExternalForm());
            }
            currentDisplayed = file;
            changeTitle(file);
        } catch (final Exception ex) {
            ex.printStackTrace();
        } finally {
            eezeFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Description of the Method
     */
    private void showHelpPage() {
        eezeFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            final URL help = eezeHelp();
            html.setDocument(help.openStream(), help.toString());
            changeTitle(html.getDocumentTitle());
        } catch (final Exception ex) {
            ex.printStackTrace();
        } finally {
            eezeFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Description of the Method
     *
     * @param hdelta PARAM
     * @param vdelta PARAM
     */
    private void resizeFrame(final float hdelta, final float vdelta) {
        final Dimension d = eezeFrame.getSize();
        eezeFrame.setSize((int) (d.getWidth() * hdelta),
                (int) (d.getHeight() * vdelta));
    }

    /**
     * Description of the Method
     *
     * @param newPage PARAM
     */
    private void changeTitle(final String newPage) {
        eezeFrame.setTitle("Eeze:  " + html.getDocumentTitle() + "  (" + newPage + ")");
    }

    /**
     * Description of the Method
     *
     * @return Returns
     */
    private URL eezeHelp() {
        return this.getClass().getClassLoader().getResource("manual_tests/help.html");
    }

    /**
     * Description of the Method
     *
     * @param args PARAM
     */
    public static void main(final String args[]) {
        try {
            final ManualTestRunner eeze = new ManualTestRunner();
            eeze.run();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    static class GridGlassPane extends JPanel {
        private static final long serialVersionUID = 1L;

        /**
         * Description of the Field
         */
        private final Color mainUltraLightColor = new Color(128, 192, 255);
        /**
         * Description of the Field
         */
        private final Color mainLightColor = new Color(0, 128, 255);
        /**
         * Description of the Field
         */
        private final Color mainMidColor = new Color(0, 64, 196);
        /**
         * Description of the Field
         */
        private final Color mainDarkColor = new Color(0, 0, 128);

        /**
         * Constructor for the GridGlassPane object
         */
        public GridGlassPane() {
            // intercept mouse and keyboard events and do nothing
            this.addMouseListener(new MouseAdapter() {
            });
            this.addMouseMotionListener(new MouseMotionAdapter() {
            });
            this.addKeyListener(new KeyAdapter() {
            });
            this.setOpaque(false);
        }

        /**
         * Description of the Method
         *
         * @param g PARAM
         */
        protected void paintComponent(final Graphics g) {
            final Graphics2D graphics = (Graphics2D) g;
            final BufferedImage oddLine = createGradientLine(this.getWidth(), mainLightColor,
                    mainDarkColor, 0.6);
            final BufferedImage evenLine = createGradientLine(this
                    .getWidth(), mainUltraLightColor,
                    mainMidColor, 0.6);

            final int height = this.getHeight();
            for (int row = 0; row < height; row = row + 10) {
                if ((row % 2) == 0) {
                    graphics.drawImage(evenLine, 0, row, null);
                } else {
                    graphics.drawImage(oddLine, 0, row, null);
                }
            }
        }


        /**
         * Description of the Method
         *
         * @param width      PARAM
         * @param leftColor  PARAM
         * @param rightColor PARAM
         * @param opacity    PARAM
         * @return Returns
         */
        public BufferedImage createGradientLine(final int width, final Color leftColor,
                                                final Color rightColor, final double opacity) {
            final BufferedImage image = new BufferedImage(width, 1,
                    BufferedImage.TYPE_INT_ARGB);
            final int iOpacity = (int) (255 * opacity);

            for (int col = 0; col < width; col++) {
                final double coef = (double) col / (double) width;
                final int r = (int) (leftColor.getRed() + coef
                        * (rightColor.getRed() - leftColor.getRed()));
                final int g = (int) (leftColor.getGreen() + coef
                        * (rightColor.getGreen() - leftColor.getGreen()));
                final int b = (int) (leftColor.getBlue() + coef
                        * (rightColor.getBlue() - leftColor.getBlue()));

                final int color = (iOpacity << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(col, 0, color);
            }
            return image;
        }
    }

    /**
     * Action to trigger frame to grow in size.
     *
     * @author Who?
     */
    class GrowAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        /**
         * Description of the Field
         */
        private final float increment = 1.1F;

        /**
         * Constructor for the GrowAction object
         */
        public GrowAction() {
            super("Grow Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_G));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.ALT_MASK));
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e PARAM
         */
        public void actionPerformed(final ActionEvent e) {
            resizeFrame(increment, increment);
        }
    }

    /**
     * Action to show a grid over the current page
     *
     * @author Who?
     */
    class ShowGridAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        private boolean on;
        private Component originalGlassPane;
        private final GridGlassPane gridGlassPane;

        /**
         * Constructor for the ShowGridAction object
         */
        public ShowGridAction() {
            super("Show Grid");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_G));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.ALT_MASK));
            gridGlassPane = new GridGlassPane();
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e PARAM
         */
        public void actionPerformed(final ActionEvent e) {
            if (on) {
                eezeFrame.setGlassPane(originalGlassPane);
                gridGlassPane.setVisible(false);
            } else {
                originalGlassPane = eezeFrame.getGlassPane();
                eezeFrame.setGlassPane(gridGlassPane);
                gridGlassPane.setVisible(true);
            }
            on = !on;
        }
    }

    /**
     * Action to trigger frame to shrink in size.
     *
     * @author Who?
     */
    class ShrinkAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        /**
         * Description of the Field
         */
        private final float increment = 1 / 1.1F;

        /**
         * Constructor for the ShrinkAction object
         */
        public ShrinkAction() {
            super("Shrink Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_MASK));
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e PARAM
         */
        public void actionPerformed(final ActionEvent e) {
            resizeFrame(increment, increment);
        }
    }

    /**
     * Description of the Class
     *
     * @author Who?
     */
    class ShowHelpAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor for the ShowHelpAction object
         */
        public ShowHelpAction() {
            super("Show Help Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_H));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.ALT_MASK));
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e PARAM
         */
        public void actionPerformed(final ActionEvent e) {
            showHelpPage();
        }
    }

    /**
     * Description of the Class
     *
     * @author Who?
     */
    class NextDemoAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor for the ReloadPageAction object
         */
        public NextDemoAction() {
            super("Next Demo Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_MASK));
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e PARAM
         */
        public void actionPerformed(final ActionEvent e) {
            String nextPage = null;
            for (final Iterator<String> iter = testFiles.iterator(); iter.hasNext();) {
                final String f = iter.next();
                if (f.equals(currentDisplayed)) {
                    if (iter.hasNext()) {
                        nextPage = iter.next();
                        break;
                    }
                }
            }
            if (nextPage == null) {
                // go to first page
                final Iterator<String> iter = testFiles.iterator();
                nextPage = iter.next();
            }

            try {
                switchPage(nextPage, false);
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }
    }

     /**
     * Description of the Class
     *
     * @author Who?
     */
    class ReloadPageAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor for the ReloadPageAction object
         */
        public ReloadPageAction() {
            super("Reload Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.ALT_MASK));
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e PARAM
         */
        public void actionPerformed(final ActionEvent e) {
            try {
                switchPage(currentDisplayed, true);
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Description of the Class
     *
     * @author Who?
     */
    class ChooseDemoAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor for the ReloadPageAction object
         */
        public ChooseDemoAction() {
            super("Choose Demo Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_MASK));
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e PARAM
         */
        public void actionPerformed(final ActionEvent e) {
            final String nextPage = (String) JOptionPane.showInputDialog(eezeFrame,
                    "Choose a demo file",
                    "Choose Demo",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    testFiles.toArray(),
                    currentDisplayed);

            try {
                switchPage(nextPage, false);
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    class ReloadFileListAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ReloadFileListAction() {
            super("Reload File List Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_F));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_MASK));
        }

        public void actionPerformed(final ActionEvent e) {
            try {
				testFiles = buildFileList();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
            currentDisplayed = testFiles.get(0);
            reloadPageAction.actionPerformed(null);
        }
    }

    /**
     * Description of the Class
     *
     * @author Who?
     */
    class FontSizeAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        /**
         * Description of the Field
         */
        private final int whichDirection;

        /**
         * Description of the Field
         */
        final static int DECREMENT = 0;
        /**
         * Description of the Field
         */
        final static int INCREMENT = 1;
        /**
         * Description of the Field
         */
        final static int RESET = 2;

        /**
         * Constructor for the FontSizeAction object
         *
         * @param which PARAM
         * @param ks    PARAM
         */
        public FontSizeAction(final int which, final KeyStroke ks) {
            super("FontSize");
            this.whichDirection = which;
            this.putValue(Action.ACCELERATOR_KEY, ks);
        }

        /**
         * Constructor for the FontSizeAction object
         *
         * @param scale PARAM
         * @param which PARAM
         * @param ks    PARAM
         */
        public FontSizeAction(final float scale, final int which, final KeyStroke ks) {
            this(which, ks);
            html.setFontScalingFactor(scale);
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(final ActionEvent evt) {
            switch (whichDirection) {
                case INCREMENT:
                    html.incrementFontSize();
                    break;
                case RESET:
                    html.resetFontSize();
                    break;
                case DECREMENT:
                    html.decrementFontSize();
                    break;
            }
        }
    }
}// end class

