/*
 * Copyright (c) 2006 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;
import org.xhtmlrenderer.resource.HTMLResource;

import com.lowagie.text.DocumentException;

public class PDFRender {
    public static void main(final String[] args) throws IOException, DocumentException {
        if (args.length != 2) {
            System.err.println("Usage: ... [url] [pdf]");
            System.exit(1);
        }
        String url = args[0];
        if (url.indexOf("://") == -1) {
            // maybe it's a file
            final File f = new File(url);
            if (f.exists()) {
                url = f.toURI().toURL().toString();
            }
        }
        createPDF(url, args[1]);
    }

    public static void createPDF(final String url, final String pdf)
            throws IOException, DocumentException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(pdf);

            /* standard approach
            ITextRenderer renderer = new ITextRenderer();

            renderer.setDocument(url);
            renderer.layout();
            renderer.createPDF(os);
            */

            final ITextRenderer renderer = new ITextRenderer();
            final ResourceLoaderUserAgent callback = new ResourceLoaderUserAgent(renderer.getOutputDevice());
            callback.setSharedContext(renderer.getSharedContext());
            renderer.getSharedContext ().setUserAgentCallback(callback);

            // TODO
            final Document doc = HTMLResource.load("<body>TODO</body>").getDocument();

            renderer.setDocument(doc, url);
            renderer.layout();
            renderer.createPDF(os);

            os.close();
            os = null;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (final IOException e) {
                    // ignore
                }
            }
        }
    }

    private static class ResourceLoaderUserAgent extends ITextUserAgent
    {
        public ResourceLoaderUserAgent(final ITextOutputDevice outputDevice) {
            super(outputDevice);
        }

        protected InputStream resolveAndOpenStream(final String uri) {
            final InputStream is = super.resolveAndOpenStream(uri);
            System.out.println("IN resolveAndOpenStream() " + uri);
            return is;
        }
    }
}
