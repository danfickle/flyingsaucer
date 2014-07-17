/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
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
 * }}}
 */
package org.xhtmlrenderer.css.parser;

import org.xhtmlrenderer.util.LangId;

public class CSSParseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final LangId _msgId;
    private final Object[] _msgArguments;

    private Token _found;
    private int _line;
    private boolean _callerNotified;
    
    public CSSParseException(final LangId msg, final int line, Object... args)
    {
    	_msgId = msg;
    	_msgArguments = args;
    	_line = line;
    }

    public LangId getMessageId()
    {
    	return _msgId;
    }
    
    public Object[] getMessageArguments()
    {
    	return _msgArguments;
    }
    
    public CSSParseException(final Token found, final Token expected, final int line) {
    	this(found, new Token[] { expected }, line);
    }
    
    public CSSParseException(final Token found, final Token[] expected, final int line) {
        this(LangId.EXPECTED_TOKEN, line, descr(expected), found.getExternalName());
        _found = found;
    }
    
    private static String descr(final Token[] tokens) {
        if (tokens.length == 1) {
            return tokens[0].getExternalName();
        } else {
            final StringBuilder result = new StringBuilder();
            for (int i = 0; i < tokens.length; i++) {
                result.append(tokens[i].getExternalName());
                if (i < tokens.length - 1) {
                    result.append(", ");
                }
            }
            return result.toString();
        }
    }

    public Token getFound() {
        return _found;
    }

    public int getLine() {
        return _line;
    }
    
    public void setLine(final int i) {
        _line = i;
    }
    
    public boolean isEOF() {
        return _found == Token.TK_EOF;
    }

    public boolean isCallerNotified() {
        return _callerNotified;
    }

    public void setCallerNotified(final boolean callerNotified) {
        _callerNotified = callerNotified;
    }
}
