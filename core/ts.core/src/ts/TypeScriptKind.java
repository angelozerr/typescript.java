package ts;

public enum TypeScriptKind {

	/** Primitive type. */
	PRIMITIVE_TYPE, /** Keyword. */
	KEYWORD, /** Class. */
	CLASS, /** Interface. */
	INTERFACE, /** Module. */
	MODULE, /** Property. */
	PROPERTY, /** Method. */
	METHOD, /** Constructor. */
	CONSTRUCTOR, /** Function. */
	FUNCTION, /** Variable. */
	VAR, /** Enumeration. */
	ENUM, /** Private modifier. */
	PRIVATE, /** Public modifier. */
	PUBLIC, /** Static modifier. */
	STATIC, /** Type. */
	TYPE;

	public static TypeScriptKind getKind(String kind) {
		try {
			return TypeScriptKind.valueOf(kind.toUpperCase());
		} catch (Throwable e) {
			return null;
		}
	}
}
