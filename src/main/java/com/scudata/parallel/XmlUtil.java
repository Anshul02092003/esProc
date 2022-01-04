package com.scudata.parallel;

import org.w3c.dom.*;

/**
 * XML�ļ���������
 * 
 * @author Joancy
 *
 */
public class XmlUtil {

	/**
	 * ��ȡ�ڵ�ֵ
	 * @param node �ڵ�
	 * @return ֵ
	 */
	public static String getNodeValue(org.w3c.dom.Node node) {
		if (node != null && node.getFirstChild() != null) {
			return node.getFirstChild().getNodeValue();
		}
		return null;
	}

	/**
	 * ��������sonName�����ӽڵ�
	 * @param pNode ���ڵ�
	 * @param sonName �ӽڵ�����
	 * @return �ӽڵ㣬û�ҵ�ʱ����null
	 */
	public static Node findSonNode(Node pNode, String sonName) {
		NodeList list = pNode.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			org.w3c.dom.Node subNode = list.item(i);
			if (subNode.getNodeName().equalsIgnoreCase(sonName)) {
				return subNode;
			}
		}
		return null;
	}

	/**
	 * ��ȡ�ڵ������ֵ
	 * @param node �ڵ����
	 * @param attrName ��������
	 * @return ����ֵ
	 */
	public static String getAttribute(Node node, String attrName) {
		NamedNodeMap attrs = node.getAttributes();
		int i = attrs.getLength();
		for (int j = 0; j < i; j++) {
			Node tmp = attrs.item(j);
			String sTmp = tmp.getNodeName();
			if (sTmp.equalsIgnoreCase(attrName)) {
				return tmp.getNodeValue();
			}
		}
		return null;
	}

}
