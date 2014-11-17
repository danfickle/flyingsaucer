package org.xhtmlrenderer.simple.extend;

import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;

import com.github.neoflyingsaucer.extend.output.ReplacedElement;
import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;

public class NoReplacedElementFactory implements ReplacedElementFactory {

    public ReplacedElement createReplacedElement(final LayoutContext c, final BlockBox box,
            final UserAgentCallback uac, final int cssWidth, final int cssHeight) {
        return null;
    }

    public void remove(final Element e) {

    }

    public void reset() {
    }

}
