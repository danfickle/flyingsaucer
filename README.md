[![Build Status](https://travis-ci.org/danfickle/neoflyingsaucer.svg)](https://travis-ci.org/danfickle/neoflyingsaucer)

OVERVIEW
--------
neoFlying Saucer is a pure-Java library for rendering arbitrary HTML 
using CSS 2.1 (and some CSS3) for layout and formatting, outputting to PDF and images.

SCOPE OF THIS PROJECT
---------------------
To provide the best open-source static, paged HTML renderer around. Interactive features have and will be removed. We plan to target Java 6.

TODO
----
+ Support the WOFF font format for embedded fonts.
+ Make cancelable/interrupted support.
+ Make case insensitive and locale insensitive.
+ Replace StringBuffer with StringBuilder.
+ Remove redundant casts.
+ Update LICENSE.
+ Update Samples.
+ Support Bootstrap styles.

DONE
----
+ Lots more tests.
+ Replace iText with a new PDF library (Apache PDF-BOX).
+ Media query support.
+ Replace java.util logging with slf4j.
+ Redirects and gzip response encoding.
+ Replace XML parser with Jsoup HTML5 parser.
+ Use a better text-breaker (Java's BreakIterator) to handle more languages.
+ CSS3 Support
  + border-radius (J2D, PDF)
  + linear-gradient (J2D, PDF)
  + opacity (J2D, PDF)
  + rgba (J2D, PDF)
+ Use external libraries for non-core functionality such as commons codec for base64 and Scalr for image scaling.
+ Move to Java 1.6 including use of generic types, enums, etc.
+ Remove SWT, itext5 and docbook support.
+ Move to latest dependencies.
+ Delete extra files, jars, etc.

BROWSER
-------
1. Fork and Clone the code.
2. Set up Eclispe project with 'mvn eclipse:eclipse' command.
3. Import project in Eclipse with File -> Import -> Existing project.
3. Take the browser for a spin at:
/neo-flying-saucer-browser/src/main/java/com/github/neoflyingsaucer/browser/BrowserMain.java

HOW TO HELP
-----------
See issues.

LICENSE
-------
Flying Saucer is distributed under the LGPL.  Flying Saucer itself is licensed 
under the GNU Lesser General Public License, version 2.1 or later, available at
http://www.gnu.org/copyleft/lesser.html. You can use Flying Saucer in any
way and for any purpose you want as long as you respect the terms of the 
license. A copy of the LGPL license is included as license-lgpl-2.1.txt
in our distributions and in our source tree.

PDF TUTORIALS
-------------
As a way of getting my head around the PDF format internal details, I have started to write tutorials about the inner workings
of the PDF format. They **may** be useful to those working with the PDF format.
+ [Linear Gradients in PDFs](pdf-internals-tutorials/linear-gradients.md)


RELATED PROJECTS
----------------
+ [PHP - mpdf](http://mpdf.bpm1.com/)
+ [PHP - dompdf](https://github.com/dompdf/dompdf)
+ [Python - WeasyPrint](https://github.com/Kozea/WeasyPrint)
+ [C++ - wkHtmlToPdf](https://github.com/wkhtmltopdf/wkhtmltopdf)
+ [Java - CSSBox](http://cssbox.sourceforge.net/)
+ [Python - xhtml2pdf](https://github.com/chrisglass/xhtml2pdf)

