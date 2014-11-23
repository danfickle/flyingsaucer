import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.swing.AWTFSImage;
import org.xhtmlrenderer.swing.ImageReplacedElement;
import org.xhtmlrenderer.swing.EmptyReplacedElement;

import com.github.neoflyingsaucer.extend.output.FSImage;
import com.github.neoflyingsaucer.extend.output.ReplacedElement;
import com.github.neoflyingsaucer.extend.useragent.ImageResourceI;
import com.github.neoflyingsaucer.extend.useragent.Optional;
import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * @author patrick
 */
public class SwingImageReplacer extends ElementReplacer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwingImageReplacer.class);
    private final Map<Element, ReplacedElement> imageComponents;

    public SwingImageReplacer() {
        imageComponents = new HashMap<Element, ReplacedElement>();
    }

    public boolean isElementNameMatch() {
        return true;
    }

    public String getElementNameMatch() {
        return "img";
    }

    public boolean accept(final LayoutContext context, final Element element) {
        return context.getNamespaceHandler().isImageElement(element);
    }

    public ReplacedElement replace(final LayoutContext context, final BlockBox box, final UserAgentCallback uac, final int cssWidth, final int cssHeight) {
        return replaceImage(uac, context, box.getElement(), cssWidth, cssHeight);
    }

    public void clear(final Element element) {
        System.out.println("*** cleared image components for element " + element);
        imageComponents.remove(element);
    }

    public void reset() {
        System.out.println("*** cleared image componentes");
        imageComponents.clear();
    }

    /**
     * Handles replacement of image elements in the document. May return the same ReplacedElement for a given image
     * on multiple calls. Image will be automatically scaled to cssWidth and cssHeight assuming these are non-zero
     * positive values. The element is assume to have a src attribute (e.g. it's an <img> element)
     *
     * @param uac       Used to retrieve images on demand from some source.
     * @param context
     * @param elem      The element with the image reference
     * @param cssWidth  Target width of the image
     * @param cssHeight Target height of the image @return A ReplacedElement for the image; will not be null.
     */
    protected ReplacedElement replaceImage(final UserAgentCallback uac, final LayoutContext context, final Element elem, final int cssWidth, final int cssHeight) {
        ReplacedElement re = null;

        // lookup in cache, or instantiate
        re = lookupImageReplacedElement(elem);
        if (re == null) {
            FSImage im = null;
            final Optional<String> oImageSrc = context.getNamespaceHandler().getImageSourceURI(elem);
            if (!oImageSrc.isPresent() || oImageSrc.get().isEmpty()) {
                LOGGER.warn("No source provided for img element.");
                //re = newIrreplaceableImageElement(cssWidth, cssHeight);
            } else {
                //FSImage is here since we need to capture a target H/W
                //for the image (as opposed to what the actual image size is).
            	final Optional<String> resolved = uac.resolveURI(context.getSharedContext().getBaseURL(), oImageSrc.get());

            	if (resolved.isPresent())
            	{
            		final Optional<ImageResourceI> resource = uac.getImageResource(resolved.get());
            		
            		if (resource.isPresent())
            		{
            			im = context.getSharedContext().resolveImage(resource.get());

            		}
            	}

                if (im != null) {
                    re = new ImageReplacedElement(im, cssWidth, cssHeight);
                } else {
                    // TODO: Should return "broken" image icon, e.g. "not found"
                    //re = newIrreplaceableImageElement(cssWidth, cssHeight);
                }
            }
            storeImageReplacedElement(elem, re);
        }
        return re;
    }

    /**
     * Adds a ReplacedElement containing an image to a cache of images for quick lookup.
     *
     * @param e  The element under which the image is keyed.
     * @param cc The replaced element containing the image, or another ReplacedElement to be used in its place
     *           (like a placeholder if the image can't be loaded).
     */
    protected void storeImageReplacedElement(final Element e, final ReplacedElement cc) {
        System.out.println("\n*** Cached image for element");
        imageComponents.put(e, cc);
    }

    /**
     * Retrieves a ReplacedElement for an image from cache, or null if not found.
     *
     * @param e The element by which the image is keyed
     * @return The ReplacedElement for the image, or null if there is none.
     */
    protected ReplacedElement lookupImageReplacedElement(final Element e) {
        if (imageComponents.size() == 0) {
            return null;
        }
        final ReplacedElement replacedElement = imageComponents.get(e);
        return replacedElement;
    }
}
