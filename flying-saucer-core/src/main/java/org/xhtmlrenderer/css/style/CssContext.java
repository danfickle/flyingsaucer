package org.xhtmlrenderer.css.style;

import org.xhtmlrenderer.context.StyleReference;

import com.github.neoflyingsaucer.extend.output.FSFont;
import com.github.neoflyingsaucer.extend.output.FSFontMetrics;
import com.github.neoflyingsaucer.extend.output.FontSpecificationI;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-jun-23
 * Time: 00:12:50
 * To change this template use File | Settings | File Templates.
 */
public interface CssContext {
    float getMmPerDot();
    
    int getDotsPerPixel();

    float getFontSize2D(FontSpecificationI font);

    float getXHeight(FontSpecificationI parentFont);

    FSFont getFont(FontSpecificationI font);
    
    // FIXME Doesn't really belong here, but this is
    // the only common interface of LayoutContext
    // and RenderingContext
    StyleReference getCss();
    
    FSFontMetrics getFSFontMetrics(FSFont font);
}
