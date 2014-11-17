package com.github.neoflyingsaucer.j2dout;

import java.awt.Graphics2D;
import com.github.neoflyingsaucer.displaylist.DlInstruction;
import com.github.neoflyingsaucer.displaylist.DlInstruction.DlLine;
import com.github.neoflyingsaucer.extend.output.DisplayList;
import com.github.neoflyingsaucer.extend.output.DisplayListOuputDevice;
import com.github.neoflyingsaucer.extend.output.DlItem;

public class Java2DOut implements DisplayListOuputDevice 
{
	private final Graphics2D g2d;
	
	public Java2DOut(Graphics2D g2d)
	{
		this.g2d = g2d;
	}
	
	@Override
	public void render(DisplayList dl)
	{
		for (DlItem item : dl.getDisplayList())
		{
			if (item instanceof DlInstruction.DlLine)
			{
				DlLine obj = (DlLine) item;
				drawLine(obj);
			}
		}
		
		g2d.dispose();
	}

	private void drawLine(DlLine line)
	{
		g2d.drawLine(line.x1, line.y1, line.x2, line.y2);
	}
}
