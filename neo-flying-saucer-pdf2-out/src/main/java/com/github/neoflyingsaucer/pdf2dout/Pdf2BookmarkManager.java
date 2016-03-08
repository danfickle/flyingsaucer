package com.github.neoflyingsaucer.pdf2dout;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import com.github.neoflyingsaucer.displaylist.DlInstruction.DlBookmark;
import com.github.neoflyingsaucer.extend.controller.cancel.FSCancelController;

public class Pdf2BookmarkManager
{
	private static class Bookmark
	{
		private final DlBookmark bm;
		private final PDPageXYZDestination destination;

		private Bookmark parent;
		private PDOutlineItem actual;
		
		private Bookmark(DlBookmark bm, PDPageXYZDestination destination)
		{
			this.bm = bm;
			this.destination = destination;
		}
	}

	private final List<Bookmark> bookmarks = new ArrayList<Bookmark>();
	
	public void addBookmark(DlBookmark bm, PDPageXYZDestination destination)
	{
		bookmarks.add(new Bookmark(bm, destination));
	}

	public void outputBookmarks(PDDocument doc)
	{
		if (bookmarks.isEmpty())
			return;
		
		PDDocumentOutline outline = new PDDocumentOutline();
    	PDOutlineItem root = new PDOutlineItem();
    	outline.addFirst( root );
 
    	for (int i = 0; i < bookmarks.size(); i++)
    	{
    		FSCancelController.cancelOpportunity(Pdf2BookmarkManager.class);
    		
    		Bookmark bm = bookmarks.get(i);
        	int currentLevel = bm.bm.level;
   	
    		// First, find the parent.
    		for (int j = i - 1; j >= 0; j--)
    		{
    			if (bookmarks.get(j).bm.level < currentLevel)
    			{
    				bm.parent = bookmarks.get(j);
    				break;
    			}

    			FSCancelController.cancelOpportunity(Pdf2BookmarkManager.class);
    		}
    	}
		
		for (Bookmark book : bookmarks)
		{
			PDOutlineItem item = new PDOutlineItem();
			item.setDestination(book.destination);
			item.setTitle(book.bm.content);
			
			book.actual = item;
			
			if (book.parent != null && book.parent.actual != null)
			{
				book.parent.actual.addLast(item);
			}
			else
			{
				root.addLast( item );
			}
			
			FSCancelController.cancelOpportunity(Pdf2BookmarkManager.class);
		}
		
		doc.getDocumentCatalog().setDocumentOutline(outline);
	}
}
