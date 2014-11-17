
import org.w3c.dom.Element;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;

import com.github.neoflyingsaucer.extend.output.ReplacedElement;
import com.github.neoflyingsaucer.extend.useragent.UserAgentCallback;

/**
 * @author patrick
*/
public abstract class ElementReplacer {
    public abstract boolean isElementNameMatch();

    public abstract String getElementNameMatch();

    public abstract boolean accept(LayoutContext context, Element element);

    public abstract ReplacedElement replace(final LayoutContext context,
                                   final BlockBox box,
                                   final UserAgentCallback uac,
                                   final int cssWidth,
                                   final int cssHeight
    );

    public abstract void clear(final Element element);

    public abstract void reset();
}
