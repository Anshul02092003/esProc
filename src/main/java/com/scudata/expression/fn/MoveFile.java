package com.scudata.expression.fn;

import com.scudata.common.MessageManager;
import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.dm.FileObject;
import com.scudata.dm.Machines;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;
import com.scudata.parallel.PartitionUtil;
import com.scudata.resources.EngineMessage;

/**
 * �ƶ���ɾ������ļ�������
 * movefile(fn,path) movefile(fn:z,path)
 * ���ļ�fn�ƶ���ָ��·���ļ�path�У�pathʡ�Դ����ļ�ɾ����pathֻ���ļ���ʱ��ʾ���ļ���������
 * movefile(fn:z,h;p,hs)
 * ���ֻ�h�ϵ��ļ�fn�Ƶ�hs�ֻ���p·���£�hs�������У�hʡ�Ա���hsʡ��Ϊ������p,hsʡ��ɾ����h��pʡ�Ե�hs������ɾ��hs�µ��ļ�
 * 
 * @author runqian
 *
 */
public class MoveFile extends Function {
	public Node optimize(Context ctx) {
		if (param != null) {
			param.optimize(ctx);
		}
		
		return this;
	}

	// �����ļ��ƶ�movefile(fn:z,path)
	private static Object localFileMove(IParam param, String option, Context ctx) {
		IParam fnParam;
		String path = null;
		if (param.getType() == IParam.Comma) {
			if (param.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("movefile" + mm.getMessage("function.invalidParam"));
			}
			
			fnParam = param.getSub(0);
			if (fnParam == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("movefile" + mm.getMessage("function.invalidParam"));
			}
			
			IParam sub = param.getSub(1);
			if (sub == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("movefile" + mm.getMessage("function.invalidParam"));
			}
			
			Object obj = sub.getLeafExpression().calculate(ctx);
			if (!(obj instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("movefile" + mm.getMessage("function.paramTypeError"));
			}
			
			path = (String)obj;
		} else {
			fnParam = param;
		}
		
		FileObject file;
		if (fnParam.isLeaf()) {
			Object pathObj = fnParam.getLeafExpression().calculate(ctx);
			if (pathObj instanceof FileObject) {
				file = (FileObject)pathObj;
			} else if (pathObj instanceof String) {
				file = new FileObject((String)pathObj, null, ctx);
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("movefile" + mm.getMessage("function.paramTypeError"));
			}
		} else {
			if (fnParam.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("movefile" + mm.getMessage("function.invalidParam"));
			}

			IParam sub0 = fnParam.getSub(0);
			if (sub0 == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("movefile" + mm.getMessage("function.invalidParam"));
			}

			Object pathObj = sub0.getLeafExpression().calculate(ctx);
			if (!(pathObj instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("movefile" + mm.getMessage("function.paramTypeError"));
			}

			file = new FileObject((String)pathObj, null, ctx);
			IParam sub1 = fnParam.getSub(1);
			if (sub1 != null) {
				Object obj = sub1.getLeafExpression().calculate(ctx);
				if (obj instanceof Number) {
					int part = ((Number)obj).intValue();
					if (part > 0) {
						file.setPartition(part);
					}
				} else if (obj != null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("movefile" + mm.getMessage("function.invalidParam"));
				}
			}
		}
		
		if (path == null || path.length() == 0) {
			if (option == null || option.indexOf('y') == -1) {
				return Boolean.valueOf(file.delete());
			} else {
				return Boolean.valueOf(file.deleteDir());
			}
		} else {
			return Boolean.valueOf(file.move(path, option));
		}
	}
	
	// ��Ⱥ�ļ��ƶ�movefile(fn:z,h;p,hs)
	private static Object clusterFileMove(IParam param, String option, Context ctx) {
		if (param.getSubSize() != 2) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("movefile" + mm.getMessage("function.invalidParam"));
		}
		
		IParam leftParam = param.getSub(0);
		if (leftParam == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("movefile" + mm.getMessage("function.invalidParam"));
		}
		
		String srcFile; // Դ�ļ�
		int part = -1; // �ֱ��
		String host = null; // Դ�ļ����ڽڵ��
		int port = -1;
		IParam fnParam;
		
		if (leftParam.getType() == IParam.Comma) {
			// fn:z,h
			if (leftParam.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("movefile" + mm.getMessage("function.invalidParam"));
			}
			
			fnParam = leftParam.getSub(0);
			if (fnParam == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("movefile" + mm.getMessage("function.invalidParam"));
			}
			
			IParam sub = leftParam.getSub(1);
			if (sub == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("movefile" + mm.getMessage("function.invalidParam"));
			}
			
			Object obj = sub.getLeafExpression().calculate(ctx);
			Machines mc = new Machines();
			if (!mc.set(obj) || mc.size() != 1) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("movefile" + mm.getMessage("function.invalidParam"));
			}
			
			host = mc.getHost(0);
			port = mc.getPort(0);
		} else {
			fnParam = leftParam;
		}
		
		if (fnParam.isLeaf()) {
			Object pathObj = fnParam.getLeafExpression().calculate(ctx);
			if (pathObj instanceof String) {
				srcFile = (String)pathObj;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("movefile" + mm.getMessage("function.invalidParam"));
			}
		} else {
			if (fnParam.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("movefile" + mm.getMessage("function.invalidParam"));
			}

			IParam sub = fnParam.getSub(0);
			if (sub == null) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("movefile" + mm.getMessage("function.invalidParam"));
			}

			Object pathObj = sub.getLeafExpression().calculate(ctx);
			if (pathObj instanceof String) {
				srcFile = (String)pathObj;
			} else {
				MessageManager mm = EngineMessage.get();
				throw new RQException("movefile" + mm.getMessage("function.paramTypeError"));
			}

			sub = fnParam.getSub(1);
			if (sub != null) {
				Object csObj = sub.getLeafExpression().calculate(ctx);
				if (csObj instanceof Number) {
					part = ((Number)csObj).intValue();
				} else if (csObj != null) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("movefile" + mm.getMessage("function.invalidParam"));
				}
			}
		}
		
		String path = null; // Ŀ��·��
		Machines hs = null; // Ŀ�����
		IParam rightParam = param.getSub(1);
		if (rightParam == null) {
		} else if (rightParam.isLeaf()) {
			Object obj = rightParam.getLeafExpression().calculate(ctx);
			if (!(obj instanceof String)) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("movefile" + mm.getMessage("function.paramTypeError"));
			}
			
			path = (String)obj;
		} else {
			if (rightParam.getSubSize() != 2) {
				MessageManager mm = EngineMessage.get();
				throw new RQException("movefile" + mm.getMessage("function.invalidParam"));
			}
			
			IParam sub = rightParam.getSub(0);
			if (sub != null) {
				Object obj = sub.getLeafExpression().calculate(ctx);
				if (!(obj instanceof String)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("movefile" + mm.getMessage("function.paramTypeError"));
				}
				
				path = (String)obj;
			}
			
			sub = rightParam.getSub(1);
			if (sub != null) {
				Object obj = sub.getLeafExpression().calculate(ctx);
				hs = new Machines();
				if (!hs.set(obj)) {
					MessageManager mm = EngineMessage.get();
					throw new RQException("movefile" + mm.getMessage("function.invalidParam"));
				}
			}
		}
		
		return PartitionUtil.moveFile(host, port, srcFile, part, hs, path, option);
	}
	
	public Object calculate(Context ctx) {
		if (param == null) {
			MessageManager mm = EngineMessage.get();
			throw new RQException("movefile" + mm.getMessage("function.missingParam"));
		} else if (param.getType() == IParam.Semicolon) {
			// movefile(fn:z,h;p,hs)
			return clusterFileMove(param, option, ctx);
		} else {
			// movefile(fn:z,path)
			return localFileMove(param, option, ctx);
		}
	}
}
