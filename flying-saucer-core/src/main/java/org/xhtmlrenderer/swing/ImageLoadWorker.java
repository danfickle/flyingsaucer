/*
 * {{{ header & license
 * Copyright (c) 2009 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.util.ImageUtil;
import java.awt.*;
import java.awt.image.BufferedImage;


/**
 * A background thread (daemon, low priority) which reads BackgroundImageLoaderItem from a BackgroundImageQueue
 * and loads the images into memory. Once images have loaded, the item's MutableFSImage will receive the newly loaded
 * image via setImage(newImage). Images, once loaded, are always BufferedImages and will always be compatible with
 * the current screen's graphics configuration. If an image cannot be loaded (network failure), a 1 x 1 pixel image
 * will be returned instead and the problem will be logged.
 */
class ImageLoadWorker extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageLoadWorker.class);
    private static volatile int counter = 0;
    private final ImageLoadQueue queue;

    public ImageLoadWorker(final ImageLoadQueue queue) {
        this.queue = queue;
        setDaemon(true);
        setPriority(Thread.MIN_PRIORITY);
        setName("ImageLoadWorker(" + counter++ + ")");
    }

    public void run() {
        try {
            while (true) {
                final ImageLoadItem loadItem = queue.getTask();

                if (ImageLoadQueue.isKillSwitch(loadItem)) {
                    break;
                }
                final ImageResource ir = ImageResourceLoader.loadImageResourceFromUri(loadItem._uri);
                final FSImage awtfsImage = ir.getImage();
                BufferedImage newImg = (BufferedImage) ((AWTFSImage) awtfsImage).getImage();
                LOGGER.debug(this + ", loaded " + loadItem._uri);

                loadItem._imageResourceLoader.loaded(ir, newImg.getWidth(), newImg.getHeight());
                final boolean wasScaled;
                if (loadItem.haveTargetDimensions() && !ir.hasDimensions(loadItem._targetWidth, loadItem._targetHeight)) {
                    LOGGER.debug(this + ", scaling " + loadItem._uri + " to " + loadItem._targetWidth + ", " + loadItem._targetHeight);
                    newImg = ImageUtil.getScaledInstance(newImg, loadItem._targetWidth, loadItem._targetHeight);
                    final ImageResource sir = new ImageResource(ir.getImageUri(), AWTFSImage.createImage(newImg));
                    loadItem._imageResourceLoader.loaded(sir, newImg.getWidth(), newImg.getHeight());
                    wasScaled = true;
                } else {
                    wasScaled = false;
                }

                // msfImage belongs to the Swing AWT thread
                final BufferedImage newImg1 = newImg;
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        loadItem._mfsImage.setImage(loadItem._uri, newImg1, wasScaled);
                    }
                });
            }
        } catch (final InterruptedException e) {
            //
        }
    }
}
