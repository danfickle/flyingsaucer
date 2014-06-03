/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci
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
package org.xhtmlrenderer.demo.browser;

import org.xhtmlrenderer.demo.browser.actions.FontSizeAction;
import org.xhtmlrenderer.demo.browser.actions.GenerateDiffAction;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.util.Uu;

import com.github.danfickle.flyingsaucer.swing.XHTMLPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Description of the Class
 *
 * @author empty
 */
public class BrowserActions {
    /**
     * Description of the Field
     */
    public Action open_file, export_pdf , quit, print;
    /**
     * Description of the Field
     */
    public Action forward, backward, refresh, reload, load, stop, print_preview, goHome;

    public Action generate_diff, usersManual, aboutPage;
    /**
     * Description of the Field
     */
    public BrowserStartup root;

    public Action increase_font, decrease_font, reset_font;

    public Action goToPage;

    /**
     * The system logger for app.browser
     */
    public static final Logger logger = Logger.getLogger("app.browser");

    /**
     * Constructor for the BrowserActions object
     *
     * @param root PARAM
     */
    public BrowserActions(final BrowserStartup root) {
        this.root = root;
    }

    /**
     * Description of the Method
     */
    public void init() {
        URL url = null;
        url = getImageUrl("images/process-stop.png");
        stop = new AbstractAction("Stop", new ImageIcon(url)) {
            public void actionPerformed(final ActionEvent evt) {
                // TODO: stop not coded
                System.out.println("stop called");
                // root.panel.view.stop();
            }
        };
        // TODO: need right API call for ESC
        //stop.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE));
        stop.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));

        open_file =
                new AbstractAction() {
                    public void actionPerformed(final ActionEvent evt) {
                        openAndShowFile();
                    }
                };
        open_file.putValue(Action.NAME, "Open File...");
        setAccel(open_file, KeyEvent.VK_O);
        setMnemonic(open_file, new Integer(KeyEvent.VK_O));

        
        export_pdf =
            new AbstractAction() {
                public void actionPerformed(final ActionEvent evt) {
                    exportToPdf();
                }
            };
        export_pdf.putValue(Action.NAME, "Export PDF...");
        //is iText in classpath? 
        try{
            Class.forName("com.lowagie.text.DocumentException");
        } catch( final ClassNotFoundException e )
        {
            export_pdf.setEnabled(false);
        }
        
        /*setAccel(export_pdf, KeyEvent.VK_E);
        setMnemonic(export_pdf, new Integer(KeyEvent.VK_E));*/

        /* printing disabled for R6
        url = getImageUrl("images/document-print.png");
        print = new PrintAction(root, new ImageIcon(url));
        setAccel(print, KeyEvent.VK_P);
        setMnemonic(print, new Integer(KeyEvent.VK_P));
        */

        quit =
                new AbstractAction() {
                    public void actionPerformed(final ActionEvent evt) {
                        System.exit(0);
                    }
                };

        setName(quit, "Quit");
        setAccel(quit, KeyEvent.VK_Q);
        setMnemonic(quit, new Integer(KeyEvent.VK_Q));
        
        url = getImageUrl("images/go-previous.png");
        backward = new EmptyAction("Back", "Go back one page", new ImageIcon(url)) {
            public void actionPerformed(final ActionEvent evt) {
                try {
                    root.panel.goBack();
                    root.panel.view.repaint();
                } catch (final Exception ex) {
                    Uu.p(ex);
                }
            }
        };

        backward.setEnabled(false);
        backward.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
                        KeyEvent.ALT_MASK));


        url = getImageUrl("images/go-next.png");
        forward = new EmptyAction("Forward", "Go forward one page", new ImageIcon(url)) {
            public void actionPerformed(final ActionEvent evt) {
                try {
                    root.panel.goForward();
                    root.panel.view.repaint();
                } catch (final Exception ex) {
                    Uu.p(ex);
                }
            }
        };
        forward.setEnabled(false);
        forward.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
                        KeyEvent.ALT_MASK));

        url = getImageUrl("images/view-refresh.png");
        refresh = new EmptyAction("Refresh", "Refresh page", new ImageIcon(url)) {
            public void actionPerformed(final ActionEvent evt) {
                try {
                    root.panel.view.invalidate();
                    root.panel.view.repaint();
                } catch (final Exception ex) {
                    Uu.p(ex);
                }
            }
        };
        refresh.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("F5"));

        url = getImageUrl("images/view-refresh.png");
        reload = new EmptyAction("Reload", "Reload page", new ImageIcon(url)) {
            public void actionPerformed(final ActionEvent evt) {
                try {
                    root.panel.reloadPage();
                    root.panel.view.repaint();
                } catch (final Exception ex) {
                    Uu.p(ex);
                }
            }
        };
        reload.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_F5,
                        InputEvent.SHIFT_MASK));
        reload.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_R));

        print_preview = new EmptyAction("Print Preview", "Print preview mode", null) {
            public void actionPerformed(final ActionEvent evt) {
                togglePrintPreview();
            }
        };
        print_preview.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_V));

        load = new AbstractAction("Load") {
            public void actionPerformed(final ActionEvent evt) {
                try {
                    final String url_text = root.panel.url.getText();
                    root.panel.loadPage(url_text);
                    root.panel.view.repaint();
                } catch (final Exception ex) {
                    Uu.p(ex);
                }
            }
        };

        url = getImageUrl("images/media-playback-start_16x16.png");
        goToPage = new EmptyAction("Go", "Go to URL in address bar", new ImageIcon(url)) {
            public void actionPerformed(final ActionEvent evt) {
                try {
                    final String url_text = root.panel.url.getText();
                    root.panel.loadPage(url_text);
                    root.panel.view.repaint();
                } catch (final Exception ex) {
                    Uu.p(ex);
                }
            }
        };

        url = getImageUrl("images/go-home.png");
        goHome = new EmptyAction("Go Home", "Browser homepage", new ImageIcon(url)) {
            public void actionPerformed(final ActionEvent evt) {
                try {
                    root.panel.loadPage(root.startPage);
                    root.panel.view.repaint();
                } catch (final Exception ex) {
                    Uu.p(ex);
                }
            }
        };

        usersManual = new EmptyAction("FS User's Guide", "Flying Saucer User's Guide", null) {
            public void actionPerformed(final ActionEvent evt) {
                try {
                    root.panel.loadPage("/users-guide-r8.html");
                    root.panel.view.repaint();
                } catch (final Exception ex) {
                    Uu.p(ex);
                }
            }
        };

        aboutPage = new EmptyAction("About", "About the Browser Demo", null) {
            public void actionPerformed(final ActionEvent evt) {
                try {
                    showAboutDialog();
                } catch (final Exception ex) {
                    Uu.p(ex);
                }
            }
        };

        generate_diff = new GenerateDiffAction(root);

        increase_font = new FontSizeAction(root, FontSizeAction.INCREMENT);
        increase_font.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_PLUS,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        increase_font.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_I));

        reset_font = new FontSizeAction(root, FontSizeAction.RESET);
        reset_font.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_0,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        reset_font.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_N));

        decrease_font = new FontSizeAction(root, FontSizeAction.DECREMENT);
        decrease_font.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        decrease_font.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_D));

        setName(increase_font, "Increase");
        setName(reset_font, "Normal");
        setName(decrease_font, "Decrease");
    }

    private void showAboutDialog() {
        final JDialog aboutDlg = new JDialog(root.frame);
        aboutDlg.setSize(new Dimension(500, 450));

        final PanelManager uac = new PanelManager();
        final XHTMLPanel panel = new XHTMLPanel(uac);
        panel.setOpaque(false);

        panel.setDocument("demo:/demos/about.xhtml");

        final JPanel outer = new JPanel(new BorderLayout());
        outer.add(panel, BorderLayout.CENTER);
        final JButton btn = new JButton(new AbstractAction("OK") {
            public void actionPerformed(final ActionEvent e) {
                aboutDlg.dispose();
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                btn.requestFocusInWindow();
            }
        });
        final JPanel control = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        control.add(btn);
        outer.add(control, BorderLayout.SOUTH);

        aboutDlg.getContentPane().setLayout(new BorderLayout());
        aboutDlg.getContentPane().add(outer, BorderLayout.CENTER);

        aboutDlg.setTitle("About the Browser Demo");

        final int xx = (root.frame.getWidth() - aboutDlg.getWidth()) / 2;
        final int yy = (root.frame.getHeight() - aboutDlg.getHeight()) / 2;
        aboutDlg.setLocation(xx, yy);
        aboutDlg.setModal(true);
        aboutDlg.setVisible(true);
    }

    private void togglePrintPreview() {
        try {
            final SharedContext sharedContext = root.panel.view.getSharedContext();

            // flip status--either we are in "print" mode (print media) or non-print (screen media)
            if (sharedContext.isPrint()) {
                sharedContext.setPrint(false);
            } else {
                sharedContext.setPrint(true);
            }
            print_preview.putValue(Action.SHORT_DESCRIPTION,
                    ! sharedContext.isPrint() ? "Print preview" : "Normal view");
            root.panel.reloadPage();
            root.panel.view.repaint();
        } catch (final Exception ex) {
            Uu.p(ex);
        }
    }

    private void openAndShowFile() {
        try {
            final FileDialog fd = new FileDialog(root.frame, "Open a local file", FileDialog.LOAD);
            fd.show();
            if (fd.getFile() != null) {
                final String url = new File(fd.getDirectory(), fd.getFile()).toURI().toURL().toString();
                root.panel.loadPage(url);
            }
        } catch (final Exception ex) {
            logger.info("error:" + ex);
        }
    }

    private void exportToPdf() {
        try {
            final FileDialog fd = new FileDialog(root.frame, "Save as PDF", FileDialog.SAVE);
            fd.setVisible( true );
            if (fd.getFile() != null) {
                final File outTarget = new File(fd.getDirectory(), fd.getFile());
                root.panel.exportToPdf(outTarget.getAbsolutePath());
            }
        } catch (final Exception ex) {
            logger.info("error:" + ex);
        }
    }


    /**
     * Sets the name attribute of the BrowserActions object
     *
     * @param act  The new name value
     * @param name The new name value
     */
    public static void setName(final Action act, final String name) {
        act.putValue(Action.NAME, name);
    }

    /**
     * Sets the accel attribute of the BrowserActions object
     *
     * @param act The new accel value
     * @param key The new accel value
     */
    public static void setAccel(final Action act, final int key) {
        act.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(key,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    /**
     * Sets the mnemonic attribute of the BrowserActions object
     *
     * @param act  The new mnemonic value
     * @param mnem The new mnemonic value
     */
    public static void setMnemonic(final Action act, final Integer mnem) {
        act.putValue(Action.MNEMONIC_KEY, mnem);
    }

    public static URL getImageUrl(final String url) {
        return BrowserActions.class.getClassLoader().getResource(url);
    }
}
