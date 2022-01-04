package com.scudata.common;

public final class Escape {

/**
*	�൱�ڵ��� add(str, '\\')
*/
	public static String add( String str ) {
		return add( str, '\\' );
	}

/**
*   Ϊ�������ַ�(\t,\r,\n,\',\"�������escapeChar)������ָ����ת������������²������´�
*   @param str ��Ҫ����ת������ַ���
*   @param escapeChar ת���
*   @return �´�
*/
	public static String add( String str, char escapeChar ) {
		if ( str == null )
			return null;

		int len = str.length();
		char[] sb = new char[ 2 * len + 2 ];   //str.length=1
		int j = 0;
		for ( int i = 0; i < len; i ++ ) {
			char ch = str.charAt( i );
			switch ( ch ) {
				case '\t':
					sb[ j++ ] = escapeChar;
					sb[ j++ ] = 't';
					break;
				case '\r':
					sb[ j++ ] = escapeChar;
					sb[ j++ ] = 'r';
					break;
				case '\n':
					sb[ j++ ] = escapeChar;
					sb[ j++ ] = 'n';
					break;
				case '\'':
					sb[ j++ ] = escapeChar;
					sb[ j++ ] = '\'';
					break;
				case '\"':
					sb[ j++ ] = escapeChar;
					sb[ j++ ] = '\"';
					break;
				default:
					if ( ch == escapeChar ) {
						sb[ j++ ] = escapeChar;
					}
					sb[ j++ ] = ch;
			}
		}
		return new String( sb, 0, j );
	}


/**
*   Ϊ�������ַ�������ָ����ת������������²������´�
*
*   @param str ��Ҫ����ת������ַ���
*	@param escapedChars ��Ҫ��ת����ַ�
*   @return �´�
*/
	public static String add( String str, String escapedChars ) {
		return add( str, escapedChars, '\\');
	}

/**
*   Ϊ�������ַ�������ָ����ת������������²������´�
*
*   @param str ��Ҫ����ת������ַ���
*	@param escapedChars ��Ҫ��ת����ַ�
*   @param escapeChar ת���
*   @return �´�
*/
	public static String add( String str, String escapedChars, char escapeChar ) {
		if ( str == null )
			return null;

		int len = str.length();
		char[] sb = new char[ 2 * len + 2 ];   //str.length=1
		int j = 0;
		for ( int i = 0; i < len; i ++ ) {
			char ch = str.charAt( i );
			switch ( ch ) {
				case '\t':
					sb[ j++ ] = escapeChar;
					sb[ j++ ] = 't';
					break;
				case '\r':
					sb[ j++ ] = escapeChar;
					sb[ j++ ] = 'r';
					break;
				case '\n':
					sb[ j++ ] = escapeChar;
					sb[ j++ ] = 'n';
					break;
				case '\'':
					sb[ j++ ] = escapeChar;
					sb[ j++ ] = '\'';
					break;
				case '\"':
					sb[ j++ ] = escapeChar;
					sb[ j++ ] = '\"';
					break;
				default:
					if ((ch == escapeChar) ||
						(escapedChars != null && escapedChars.indexOf(ch) >= 0))
						sb[ j++ ] = escapeChar;
					sb[ j++ ] = ch;
			}
		}
		return new String( sb, 0, j );
	}

	public static String addEscAndQuote( String str, String escapedChars, char escapeChar ) {
		return addEscAndQuote( str, true, escapedChars, escapeChar );
	}

	public static String addEscAndQuote( String str, boolean ifDblQuote, String escapedChars, char escapeChar ) {
		if ( str == null )
			return null;

		int len = str.length();
		char[] sb = new char[ 2 * len + 2 ];   //str.length=1
		sb[0] = ifDblQuote ? '\"' : '\'';
		int j = 1;
		for ( int i = 0; i < len; i ++ ) {
			char ch = str.charAt( i );
			switch ( ch ) {
				case '\t':
					sb[ j++ ] = escapeChar;
					sb[ j++ ] = 't';
					break;
				case '\r':
					sb[ j++ ] = escapeChar;
					sb[ j++ ] = 'r';
					break;
				case '\n':
					sb[ j++ ] = escapeChar;
					sb[ j++ ] = 'n';
					break;
				case '\'':
					if(!ifDblQuote)
						sb[ j++ ] = escapeChar;
					sb[ j++ ] = '\'';
					break;
				case '\"':
					if(ifDblQuote)
						sb[ j++ ] = escapeChar;
					sb[ j++ ] = '\"';
					break;
				default:
					if ((ch == escapeChar) ||
						(escapedChars != null && escapedChars.indexOf(ch) >= 0))
						sb[ j++ ] = escapeChar;
					sb[ j++ ] = ch;
			}
		}
		sb[j++] = ifDblQuote ? '\"' : '\'';
		return new String( sb, 0, j );
	}


/**
*	�൱�ڵ��� remove( str, '\\')
*/
	public static String remove( String str ) {
		return remove( str, '\\' );
	}

/**
*   ��ָ�����ַ�����ȥָ��ת������������²������´�
*
*   @param str ��Ҫ��ȥת������ַ���
*   @param escapeChar ת���ַ�
*   @return ԭ����ȥת�������´�
*/
	public static String remove( String str, char escapeChar ) {
		if ( str == null )
			return null;

		int len = str.length();
		if (len == 0) return str;
		char[] sb = new char[ len ];
		int i = 0, j = 0;
		char ch = str.charAt( i );
		for ( ; i < len; i++ ) {
			ch = str.charAt( i );
			if ( ch == escapeChar ) {
				i ++;
				if ( i == len )
					break;
				ch = str.charAt( i );
				switch ( ch ) {
					case 't':
						sb[ j++ ] = '\t';
						break;
					case 'r':
						sb[ j++ ] = '\r';
						break;
					case 'n':
						sb[ j++ ] = '\n';
						break;
					default:
						sb[ j++ ] = ch;
				}
			} else
				sb[ j++ ] =  ch;
		}
		return new String( sb, 0, j );
	}

/**
*	��ʹ�þ�ת������ַ����任ʹ����ת������ַ���
*	@param str ��Ҫ�任ת����Ĵ�
*	@param oldEscapeChar ��ת���
*	@param newEscapeChar ��ת���
*	@return �任����´�
*/
	public static String change( String str, char oldEscapeChar, char newEscapeChar ) {
		if ( str == null )
			return null;

		int len = str.length();
		if (len == 0) return str;
		char[] sb = new char[ len ];
		for (int i = 0 ; i < len; i++ ) {
			if ( str.charAt( i ) == oldEscapeChar ) {
				sb[ i++ ] = newEscapeChar;
				if ( i < len )
					sb[ i ] = str.charAt( i );
			} else
				sb[ i ] = str.charAt( i );
		}
		return new String( sb );
	}

/**
*	�൱�ڵ��� addEscAndQuote(str, true, '\\')
*/
	public static String addEscAndQuote( String str ) {
		return addEscAndQuote( str, true, '\\' );
	}

/**
*	�൱�ڵ��� addEscAndQuote(str, ifDblQuote, '\\')
*/
	public static String addEscAndQuote( String str, boolean ifDblQuote) {
		return addEscAndQuote( str, ifDblQuote, '\\' );
	}

/**
*	�൱�ڵ��� addEscapeAndQuote(str, true, '\\')
*/
	public static String addEscAndQuote( String str, char escapeChar) {
		return addEscAndQuote( str, true, escapeChar );
	}

/**
*   Ϊ�������ַ�������ָ����ת�������ǰ�������ţ��������²������´�
*
*   @param str ��Ҫ����ת������ַ���
*	@param ifDblQuote Ϊtrueʱ����˫���ţ�������ϵ�����
*   @param escapeChar ת���
*   @return �´�
*/
	public static String addEscAndQuote( String str, boolean ifDblQuote, char escapeChar ) {
		if ( str == null )
			return null;

		int len = str.length();
		char[] sb = new char[ 2 * len + 2 ];   //str.length=1
		sb[0] = ifDblQuote ? '\"' : '\'';
		int j = 1;
		for ( int i = 0; i < len; i ++ ) {
			char ch = str.charAt( i );
			switch ( ch ) {
				case '\t':
					sb[ j++ ] = escapeChar;
					sb[ j++ ] = 't';
					break;
				case '\r':
					sb[ j++ ] = escapeChar;
					sb[ j++ ] = 'r';
					break;
				case '\n':
					sb[ j++ ] = escapeChar;
					sb[ j++ ] = 'n';
					break;
				case '\'':
					if(!ifDblQuote)
						sb[ j++ ] = escapeChar;
					sb[ j++ ] = '\'';
					break;
				case '\"':
					if(ifDblQuote)
						sb[ j++ ] = escapeChar;
					sb[ j++ ] = '\"';
					break;
				default:
					if ( ch == escapeChar ) {
						sb[ j++ ] = escapeChar;
					}
					sb[ j++ ] = ch;
			}
		}
		sb[ j++ ] = ifDblQuote ? '\"' : '\'';
		return new String( sb, 0, j );
	}
	
	/**
	 * ʹ��excel��׼���˫����
	 * @param str
	 * @return
	 */
	public static String addExcelQuote(String str) {
		if ( str == null ) {
			return null;
		}
		
		int len = str.length();
		char[] sb = new char[ 2 * len + 2 ];   //str.length=1
		sb[0] = '"';
		int j = 1;
		for ( int i = 0; i < len; i ++ ) {
			char ch = str.charAt( i );
			if (ch == '"') {
				sb[ j++ ] = '"';
				sb[ j++ ] = '"';
			} else {
				sb[ j++ ] = ch;
			}
		}
		
		sb[ j++ ] = '\"';
		return new String( sb, 0, j );
	}

	/**
	*	�൱�ڵ��� removeEscAndQuote( str, '\\')
	*/
	public static String removeEscAndQuote( String str ) {
		return removeEscAndQuote( str, '\\' );
	}

	/**
	*   ��ָ�����ַ�����ȥ����ߵ����ż�ָ��ת������������²������´�
	*
	*   @param str ��Ҫ��ȥת������ַ���
	*   @param escapeChar ת���ַ�
	*   @return ԭ����ȥת�������´�
	*/
	public static String removeEscAndQuote( String str, char escapeChar ) {
		if ( str == null )
			return null;

		int len = str.length();
		if (len == 0) return str;
		char[] sb = new char[ len ];
		int i = 0, j = 0;
		char ch = str.charAt( i );
		
		if ((ch=='"' || ch=='\'') && str.charAt( len - 1 ) == ch) {
			 i++;
			 len--;
		}
		
		for ( ; i < len; i++ ) {
			ch = str.charAt( i );
			if ( ch == escapeChar ) {
				i ++;
				if ( i == len )
					break;
				ch = str.charAt( i );
				switch ( ch ) {
					case 't':
						sb[ j++ ] = '\t';
						break;
					case 'r':
						sb[ j++ ] = '\r';
						break;
					case 'n':
						sb[ j++ ] = '\n';
						break;
					default:
						sb[ j++ ] = ch;
				}
			} else {
				sb[ j++ ] =  ch;
			}
		}
		
		return new String( sb, 0, j );
	}

	public static void main(String[] args) {
		String s = "a=\"(1+1)\";b=\"��salary��+1\"";
		s = add(s, "()[]{}");
		System.out.println(s);
		s = remove(s);
		System.out.println(s);
		s = "\"'abc'\"+5";
		System.out.println( addEscAndQuote(s) );
	}
	
}
