package com.scudata.expression.fn.string;

import java.io.UnsupportedEncodingException;
import javax.xml.bind.DatatypeConverter;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.resources.EngineMessage;

/**
 * base64(x:cs,cs')
 * ��cs�ַ���ȡ��x�е�BLOBת��base64������base64��xת��cs���ַ����Ĵ����ַ���ʡ��ʱȱʡ��BLOB����
 * @author runqian
 *
 */
public class Base64 extends Function {

	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("base64" + mm.getMessage("function.invalidParam"));
		} else if (param.isLeaf()) {
			Object val = param.getLeafExpression().calculate(ctx);
			if (val instanceof String) {
				return DatatypeConverter.parseBase64Binary((String)val);
			} else if (val instanceof byte[]) {
				return DatatypeConverter.printBase64Binary((byte[])val);
			} else if (val == null) {
				return null;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("base64" + mm.getMessage("function.paramTypeError"));
			}
		} else {
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("base64" + mm.getMessage("function.invalidParam"));
			}
			
			IParam sub0 = param.getSub(0);
			IParam sub1 = param.getSub(1);
			if (sub0 == null || sub1 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("base64" + mm.getMessage("function.invalidParam"));
			}
			
			Object obj = sub0.getLeafExpression().calculate(ctx);
			if (!(obj instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("base64" + mm.getMessage("function.paramTypeError"));
			}
			
			String value = (String)obj;
			obj = sub1.getLeafExpression().calculate(ctx);
			if (!(obj instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("base64" + mm.getMessage("function.paramTypeError"));
			}
			
			String cs = (String)obj;
			try {
				if (param.getType() == IParam.Colon) {
					byte []bytes = value.getBytes(cs);
					return DatatypeConverter.printBase64Binary(bytes);
				} else {
					byte []bytes = DatatypeConverter.parseBase64Binary(value);
					return new String(bytes, cs);
				}
			} catch (UnsupportedEncodingException e) {
				throw new RQException(e);
			}
		}
	}
}
