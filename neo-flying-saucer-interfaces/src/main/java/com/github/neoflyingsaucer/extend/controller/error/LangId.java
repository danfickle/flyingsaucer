package com.github.neoflyingsaucer.extend.controller.error;

public enum LangId
{
	/**
	 * 3 arguments.
	 */
	VALUE_COUNT_MISMATCH,

	/**
	 * 4 arguments.
	 */
	MIN_MAX_VALUE_COUNT_MISMATCH,

	/**
	 * 1 arguments.
	 */
	MUST_BE_INT,
	
	/**
	 * 1 arguments.
	 */
	MUST_BE_LENGTH_OR_IDENTIFIER,

	/**
	 * 1 arguments.
	 */
	MUST_BE_LENGTH,

	/**
	 * 2 arguments.
	 */
	UNSUPPORTED_IDENTIFIER,

	/**
	 * 2 arguments.
	 */
	UNRECOGNIZED_IDENTIFIER,

	/**
	 * 0 arguments.
	 */
	INVALID_INHERIT,

	/**
	 * 1 arguments.
	 */
	NO_NEGATIVE,

	/**
	 * 0 arguments.
	 */
	SPAN_MUST_BE_GT_ZERO,

	/**
	 * 1 arguments.
	 */
	ONLY_AUTO_ALLOWED,

	/**
	 * 0 arguments.
	 */
	INVALID_FONT_FAMILY,

	/**
	 * 1 arguments.
	 */
	INVALID_FONT_WEIGHT,

	/**
	 * 1 arguments.
	 */
	RUNNING_NEED_IDENTIFIER, 

	/**
	 * 1 arguments.
	 */
	ONLY_RUNNING_ALLOWED, 

	/**
	 * 1 arguments.
	 */
	MUST_BE_FUNC_OR_IDENTIFIER, 

	/**
	 * 1 arguments.
	 */
	NO_NONE_VALUE, 

	/**
	 * 0 arguments.
	 */
	OPACITY_OUT_OF_RANGE, 

	/**
	 * 1 arguments.
	 */
	INVALID_KEYWORD_COMBINATION,

	/**
	 * 2 arguments.
	 */
	UNSUPPORTED_TYPE,

	/**
	 * 1 arguments.
	 */
	UNRECOGNIZED_PROPERTY,

	/**
	 * 1 arguments.
	 */
	UNIMPLEMENTED_PROPERTY,

	/**
	 * 1 arguments.
	 */
	NO_PROPERTY_BUILDER,
	
	/**
	 * 1 arguments.
	 */
	CSS_ERROR,

	/**
	 * 1 arguments.
	 */
	UNRECOGNIZED_PSEUDO,
	
	/**
	 * 1 arguments.
	 */	
	FUNCTION_NOT_SUPPORTED,

	/**
	 * 1 arguments.
	 */	
	UNSUPPORTED_CSS_UNIT,

	/**
	 * 0 arguments.
	 */		
	CMYK_NOT_SUPPORTED,

	/**
	 * 2 arguments.
	 */	
	EXACTLY_PARAMS_REQUIRED,

	/**
	 * 2 arguments.
	 */	
	COLOR_FUNC_COMPONENT_INVALID,
	
	/**
	 * 1 arguments.
	 */	
	INVALID_HEX_COLOR,
	
	/**
	 * 0 arguments.
	 */
	PAGE_NAME_NO_AUTO,
	
	/**
	 * 0 arguments.
	 */	
	ATTR_REQUIRED,

	/**
	 * 1 arguments.
	 */	
	NO_NAMESPACE_FOUND,

	/**
	 * 0 arguments.
	 */	
	NO_DUEL_PSEUDOS,

	/**
	 * 1 arguments.
	 */	
	PAGE_NAME_MUST_BE,

	/**
	 * 1 arguments.
	 */	
	MARGIN_BOX_NAME,
	
	/**
	 * 1 arguments.
	 */
	AT_RULE_NOT_SUPPORTED_HERE, 

	/**
	 * 1 arguments.
	 */
	BUILDER_BUG,

	/**
	 * 1 arguments.
	 */	
	INVALID_NTH_CHILD,

	/**
	 * 1 arguments.
	 */
	NO_TWICE,

	/**
	 * 1 arguments.
	 */
	BORDER_VALUE_INVALID,
	
	/**
	 * 1 arguments.
	 */
	INVALID_SYNTAX,
	
	/**
	 * 1 arguments.
	 */	
	INVALID_PAGE_ORIENTATION,
	
	/**
	 * 1 arguments.
	 */	
	INVALID_PAGE_SIZE,

	/**
	 * 0 arguments.
	 */	
	FONT_SIZE_REQUIRED,
	
	/**
	 * 2 arguments.
	 */
	INVALID_VALUE,

	/**
	 * 1 arguments.
	 */	
	UNEXPECTED_OPERATOR,

	/**
	 * 0 arguments.
	 */	
	MISMATCHED_QUOTES,
	
	/**
	 * 2 arguments.
	 */
	EXPECTED_TOKEN,
	
	/**
	 * 1 argument.
	 */
	NODE_TYPE_NOT_HANDLED,
	
	/**
	 * 1 argument.
	 */
	STYLE_ELEM_MOVED_TO_HEAD,
	
	/**
	 * 1 argument.
	 */
	COULDNT_LOAD_FONT,

	/**
	 * 1 argument.
	 */
	COULDNT_LOAD_IMAGE,

	/**
	 * 0 arguments.
	 */	
	NO_IMAGE_SRC_PROVIDED,

	/**
	 * 1 argument.
	 */
	COULDNT_LOAD_CSS,

	/**
	 * 1 argument.
	 */
	COULDNT_LOAD_HTML_DOCUMENT,
	
	/**
	 * 1 argument.
	 */
	INVALID_URI,

	/**
	 * 2 arguments.
	 */
	INVALID_BASE_URI_PAIR,
	
	/**
	 * 1 argument.
	 */
	ITEM_AT_URI_NOT_FOUND,
	
	/**
	 * 1 argument.
	 */
	GENERIC_IO_EXCEPTION,
	
	/**
	 * 1 argument.
	 */
	RECEIVING_STYLESHEET,
	
	/**
	 * 1 argument.
	 */
	RESOURCE_WITH_NO_URI,
	
	/**
	 * 1 argument.
	 */
	RECEIVING_IMAGE,

	/**
	 * 2 arguments.
	 */
	DONT_UNDERSTAND_ASSIGNMENT,
	
	/**
	 * 1 argument.
	 */
	UNHANDLED_EXCEPTION,
	
	/**
	 * 2 arguments.
	 */
	UNHANDLED_EXCEPTION_WITH_CAUSE,
	
	/**
	 * 0 arguments.
	 */
	ENCOUNTERED_CSS_UNKNOWN,
	
	/**
	 * 0 arguments.
	 */
	UNABLE_TO_DECODE_URL,
	
	/**
	 * 0 arguments.
	 */
	COULDNT_LOAD_DEFAULT_CSS,
	
	/**
	 * 3 arguments.
	 */
	COULDNT_CONVERT_UNIT,
	
	/**
	 * 1 argument.
	 */
	COULDNT_CONVERT_UNIT_MEDIA_QUERY,
	
	/**
	 * 0 arguments.
	 */
	NO_CMYK_FOR_LINEAR_GRADIENT,
	
	/**
	 * 1 argument.
	 */
	COULDNT_FIND_HASH_LINK_TARGET,
	
	/**
	 * 0 arguments.
	 */
	NO_INTERNAL_LINKS_FOR_CONTINOUS,

	/**
	 * 0 arguments.
	 */
	NO_BOOKMARKS_FOR_CONTINOUS,
	
	/**
	 * 0 arguments.
	 */
	COULDNT_CONVERT_DATA_URI,
	
	/**
	 * 0 arguments.
	 */
	NO_CONTENT_LIMIT,
	
	/**
	 * 0 arguments.
	 */
	COULDNT_PARSE_STYLESHEET_NO_URI,
	
	/**
	 * 1 argument.
	 */
	TIME_TO_PARSE_STYLESHEETS,
	
	/**
	 * 1 argument.
	 */
	MATCHER_CREATED,
	
	/**
	 * 1 argument.
	 */
	APPEND_CONDITIONS_TO_PSEUDO,
	
	/**
	 * 0 arguments.
	 */
	BAD_SIBLING_AXIS,
	
	/**
	 * 0 arguments.
	 */
	SET_MORE_THAN_ONE_PSEUDO;
}
