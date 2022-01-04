package com.scudata.expression.fn;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.Reader;

import com.scudata.common.RQException;
import com.scudata.dm.Context;
import com.scudata.expression.Function;
import com.scudata.expression.IParam;
import com.scudata.expression.Node;

/**
 * ����ϵͳ���������ݣ�sʡ�����Դ���ʽ���ؼ���������
 * clipboard() clipboard(s)
 * ѡ��@e ������ֵ�ǰϵͳ��������������Excel����ȡ�����ò���¼����
 * @author RunQian
 *
 */
public class Clipboard extends Function {
	private static String m_lastBufExcelString = null;
	
	public Node optimize(Context ctx) {
		if (param != null) param.optimize(ctx);
		return this;
	}

	public Object calculate(Context ctx) {
		IParam param = this.param;
		String text = null;

		//�����ڷ�Excel����Դ
		if (this.option!=null && this.option.contains("e")){
			text = getClipboardExcelString();
			if (text!=null && !text.isEmpty()){
				return text;
			}
		}

		if (param == null) { //paster
			text = getClipboardString();
			return text;
		}
		
		boolean bLeft = param.isLeaf();
		if (bLeft){ //copy to clipboard
			Object o = param.getLeafExpression().calculate(ctx);
			if(o!=null) {
				setClipboardString(o.toString());
			}
			return true;
		}
		return null;
	}

    /**
     * ���ı����õ������壨���ƣ�
     */
    public void setClipboardString(String text) {
    	java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable trans = new StringSelection(text);
        clipboard.setContents(trans, null);
    }
    
    /**
     * �Ӽ������л�ȡ�ı���ճ����
     */
    public String getClipboardString() {
    	java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable trans = clipboard.getContents(null);
        if (trans != null) {
            if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    String text = (String) trans.getTransferData(DataFlavor.stringFlavor);
                    if (text.endsWith("\n")){
						text = text.substring(0,text.length() - 1);
					}
                    return text;
                } catch (Exception e) {
                	throw new RQException(e.getMessage(), e);
                }
            }
        }

        return null;
    }

    /**
     * ��ϵͳ���������ж��Ƿ�����Excel�����ݣ�ճ����
     */
    
    private String getClipboardExcelString() {
    	java.awt.datatransfer.Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable tf = clip.getContents(null);
		DataFlavor dfs[] = tf.getTransferDataFlavors();
		if (dfs == null) {
			return null;
		}

		try {
			String text = null;
			for (int i = 0; i < dfs.length; i++) {
				if (dfs[i].getMimeType().indexOf("text/html") > -1) {
					Reader r = dfs[i].getReaderForText(tf);					
					String txt = getReaderContent(r).toString();
					String[] ss = txt.toLowerCase().split("\n");
					int nIdx = 0;
					for(String s:ss){
						if (s.contains("office:office") ||
							s.contains("office:excel") ||
							(s.contains("name=progid") && s.contains("excel")) ||
							(s.contains("name=generator") && s.contains("excel")) ){
							nIdx++;
						}
					}
					
					if (nIdx>=3){
						text = (String) tf.getTransferData(DataFlavor.stringFlavor);
						m_lastBufExcelString = null;
						m_lastBufExcelString = new String(text);		
						//System.out.println(txt);
						break;
					}else if(i>9){ //�Ҳ������ϴε�.
						break;
					}					
				}
			}
			if (text == null){
				text = m_lastBufExcelString;
			}
			
			return text;
		} catch (Exception e) {
			throw new RQException(e.getMessage(), e);
		}
    }
    
    private StringBuilder getReaderContent(java.io.Reader r) throws Exception {
        char[] arr = new char[512];
        StringBuilder buffer = new StringBuilder();
        int numCharsRead;
        while ((numCharsRead = r.read(arr, 0, arr.length)) != -1) {
            buffer.append(arr, 0, numCharsRead);
            break;
        }
        r.close();
        return buffer;
    }
}